package org.backmeup.plugin.osgi;

import java.io.File;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.felix.framework.FrameworkFactory;
import org.backmeup.configuration.cdi.Configuration;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.PluginUnavailableException;
import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.Plugin;
import org.backmeup.plugin.api.connectors.Action;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.spi.Authorizable;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.launch.Framework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * The PluginImpl class realizes the Plugin interface by starting the Apache
 * Felix OSGi container.
 * 
 * All plug ins must therefore be a bundle which can be added and removed at
 * runtime of the BackMeUp core.
 * 
 * To achieve the capability of adding and removing plug ins at runtime, the
 * class DeployMonitor has been created which monitors a certain directory on
 * this computer adding new bundles found within it.
 * 
 * A client of the PluginImpl never works directly with the plug ins. The plug
 * ins will always be proxied with the java.lang.reflect.Proxy class.
 * 
 * The call to a proxy-method looks up a service by its ServiceReference, then
 * it invokes the method with all necessary parameters and finally it releases
 * the ServiceReference and returns the result of the method call.
 * 
 * 
 * 
 * @author fschoeppl
 * 
 */
@ApplicationScoped
@Default
@Named("plugin")
@SuppressWarnings("rawtypes")
public class PluginImpl implements Plugin {
	private static final Logger logger = LoggerFactory.getLogger(PluginImpl.class);

	@Inject
	@Configuration(key = "backmeup.osgi.deploymentDirectory")
	private String deploymentDirPath;

	@Inject
	@Configuration(key = "backmeup.osgi.temporaryDirectory")
	private String tempDirPath;
	
	@Inject
	@Configuration(key = "backmeup.osgi.exportedPackages")
	private String exportedPackages;

	private boolean started;
	
	private File deploymentDirectory;
	
	private File temporaryDirectory;

	private Framework osgiFramework;

	private DeployMonitor deploymentMonitor;

	/*
	 * public PluginImpl() { this(
	 * "C:\\Program Files (Dev)\\apache-tomcat-7.0.42\\data\\rest\\autodeploy",
	 * "osgiTemp" + Long.toString(System.nanoTime()), EXPORTED_PACKAGES ); }
	 */
	public PluginImpl() {
		
	}

	public PluginImpl(String deploymentDirectory, String temporaryDirectory,
			String exportedPackages) {
		logger.debug("********** NEW PLUGINIMPL **********");
		this.deploymentDirPath = deploymentDirectory;
		this.tempDirPath = temporaryDirectory;
		this.exportedPackages = exportedPackages;
		this.started = false;
	}

	@Override
    @PostConstruct
	public void startup() {
		if (!started) {
			logger.debug("Starting up PluginImpl!");
			this.tempDirPath = this.tempDirPath + Long.toString(System.nanoTime());
					
			this.deploymentDirectory = new File(deploymentDirPath);
			this.temporaryDirectory = new File(tempDirPath);
			
			initOSGiFramework();
			startDeploymentMonitor();
			deploymentMonitor.waitForInitialRun();
			started = true;
		}
	}

	public void waitForInitialStartup() {
		deploymentMonitor.waitForInitialRun();
	}

	private void startDeploymentMonitor() {
		this.deploymentMonitor = new DeployMonitor(bundleContext(), deploymentDirectory);
		this.deploymentMonitor.start();
	}

	public static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (String element : children) {
				boolean success = deleteDir(new File(dir, element));
				if (!success) {
					return false;
				}
			}
		}
		// The directory is now empty so delete it
		return dir.delete();
	}

	public void initOSGiFramework() {
		try {
			FrameworkFactory factory = new FrameworkFactory();
			if (temporaryDirectory.exists()) {
				deleteDir(temporaryDirectory);
			}
			Map<String, String> config = new HashMap<>();
			
			config.put(Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA, exportedPackages);
			config.put(Constants.FRAMEWORK_STORAGE, temporaryDirectory.getAbsolutePath());
			config.put(Constants.FRAMEWORK_STORAGE_CLEAN, "true");
			config.put(Constants.FRAMEWORK_BUNDLE_PARENT, Constants.FRAMEWORK_BUNDLE_PARENT_FRAMEWORK);
			// Constants.FRAMEWORK_BUNDLE_PARENT_APP);
			config.put(Constants.FRAMEWORK_BOOTDELEGATION, exportedPackages);
			
			logger.debug("EXPORTED PACKAGES: " + exportedPackages);
			// config.put(Constants.FRAMEWORK_EXECUTIONENVIRONMENT, "J2SE-1.6");
			// config.put("osgi.shell.telnet", "on");
			// config.put("osgi.shell.telnet.port", "6666");
			
			osgiFramework = factory.newFramework(config);
			osgiFramework.start();
		} catch (Exception e) {
			logger.error("", e);
			throw new RuntimeException(e);
		}
	}

	public void stopOSGiFramework() {
		try {
			osgiFramework.stop();
			osgiFramework.waitForStop(0);
			logger.debug("OsgiFramework stopped.");
		} catch (InterruptedException e) {
			logger.error("", e);
		} catch (BundleException e) {
			logger.error("", e);
		}
	}

	public BundleContext bundleContext() {
		return osgiFramework.getBundleContext();
	}

	public <T> T service(final Class<T> service) {
		return service(service, null);
	}

	private <T> ServiceReference getReference(final Class<T> service,
			final String filter) {
		ServiceReference ref = null;
		if (filter == null) {
			ref = bundleContext().getServiceReference(service.getName());

		} else {
			ServiceReference[] refs;
			try {
				refs = bundleContext().getServiceReferences(service.getName(),
						filter);
			} catch (InvalidSyntaxException e) {
				throw new IllegalArgumentException(String.format("The filter '%s' is mallformed.", filter), e);
			}
			if (refs != null && refs.length > 0) {
				ref = refs[0];
			}
		}
		return ref;
	}

	@SuppressWarnings("unchecked")
	public <T> T service(final Class<T> service, final String filter) {
		ServiceReference ref = getReference(service, filter);
		if (ref == null) {
			throw new PluginUnavailableException(filter);
		}
		bundleContext().ungetService(ref);
		return (T) Proxy.newProxyInstance(PluginImpl.class.getClassLoader(),
				new Class[] { service }, new InvocationHandler() {

					@Override
                    public Object invoke(Object o, Method method, Object[] os)
							throws Throwable {
						ServiceReference ref = getReference(service, filter);
						if (ref == null) {
							throw new PluginUnavailableException(filter);
						}
						Object instance = bundleContext().getService(ref);
						// TODO: Throw exception if instance is null! This might
						// happen if <packaging>bundle</packaging> is missing in
						// pom.xml
						Object ret = null;
						try {
							ret = method.invoke(instance, os);
						} catch (Exception e) {
							throw new PluginException(filter,
									"An exception occured during execution of the method "
											+ method.getName(), e);
						} finally {
							bundleContext().ungetService(ref);
						}
						return ret;
					}
				});
	}

	private static class SpecialInvocationHandler implements InvocationHandler {
		private final Logger logger = LoggerFactory.getLogger(SpecialInvocationHandler.class);
		private final ServiceReference reference;
		private final BundleContext context;

		public SpecialInvocationHandler(BundleContext context, ServiceReference reference) {
			this.reference = reference;
			this.context = context;
		}

		@Override
        public Object invoke(Object o, Method method, Object[] os) throws Throwable {
			ServiceReference ref = reference;
			Object ret = null;
			@SuppressWarnings("unchecked")
			Object instance = context.getService(ref);
			if (instance == null) {
				logger.error(
						"FATAL ERROR:\n\tCalling the method \"{}\" of a null-instance from bundle \"{}\"; getService returned null!\n",
						method.getName(), ref.getBundle().getSymbolicName());
			}
			try {
				boolean acc = method.isAccessible();
				method.setAccessible(true);

				if (os == null) {
					ret = method.invoke(instance);
				} else {
					ret = method.invoke(instance, os);
				}
				method.setAccessible(acc);
			} finally {
				context.ungetService(ref);
			}
			return ret;
		}
	}

	@SuppressWarnings("unchecked")
	public <T> Iterable<T> services(final Class<T> service, final String filter) {
		return new Iterable<T>() {

			@Override
            public Iterator<T> iterator() {
				try {
					ServiceReference[] refs = bundleContext()
							.getServiceReferences(service.getName(), filter);
					if (refs == null) {
						return new Iterator<T>() {
							@Override
                            public boolean hasNext() {
								return false;
							}

							@Override
                            public T next() {
								return null;
							}

							@Override
                            public void remove() {
							}
						};
					}
					List<T> services = new ArrayList<>();
					for (ServiceReference s : refs) {
						services.add((T) Proxy.newProxyInstance(
								PluginImpl.class.getClassLoader(),
								new Class[] { service },
								new SpecialInvocationHandler(bundleContext(), s)));
					}
					return services.iterator();
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
	}

	@Override
    public void shutdown() {
		if (started) {
			logger.debug("Shutting down PluginImpl!");
			this.deploymentMonitor.stop();
			this.stopOSGiFramework();
			this.started = false;
		}
	}

	protected List<PluginDescribable> getDescribableForType(PluginType...pluginTypes) {
		List<PluginType> lpluginTypes = Arrays.asList(pluginTypes);
		Iterable<PluginDescribable> descs = services(
				PluginDescribable.class, null);
		List<PluginDescribable> result = new ArrayList<>();
		for (PluginDescribable d : descs) {
			if (lpluginTypes.contains(d.getType())) {
				result.add(d);
			}
		}
		return result;
	}
	
	@Override
    public List<PluginDescribable> getActions() {
		return this.getDescribableForType(PluginType.Action);
	}
	
	@Override
    public List<PluginDescribable> getDatasinks() {
		return this.getDescribableForType(PluginType.Sink, PluginType.SourceSink);
	}
	
	@Override
    public List<PluginDescribable> getDatasources() {
		return this.getDescribableForType(PluginType.Source, PluginType.SourceSink);
	}
	
	@Override
    public PluginDescribable getPluginDescribableById(String sourceSinkId) {
		return service(PluginDescribable.class, "(name=" + sourceSinkId + ")");
	}
	
	@Override
    public Datasource getDatasource(String sourceId) {
		return service(Datasource.class, "(name=" + sourceId + ")");
	}

	@Override
    public Datasink getDatasink(String sinkId) {
		return service(Datasink.class, "(name=" + sinkId + ")");
	}
	
	@Override
    public Action getAction(String actionId) {
		return service(Action.class, "(name=" + actionId + ")");
	}
	
	@Override
    public Authorizable getAuthorizable(String sourceSinkId) {
		return service(Authorizable.class, "(name=" + sourceSinkId + ")");
	}

	@Override
	public Validationable getValidator(String sourceSinkId) {
		return service(Validationable.class, "(name=" + sourceSinkId + ")");
	}

}
