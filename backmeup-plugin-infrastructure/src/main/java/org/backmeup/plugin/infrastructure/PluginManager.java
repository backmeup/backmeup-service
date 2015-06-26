package org.backmeup.plugin.infrastructure;

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

import org.apache.felix.framework.FrameworkFactory;
import org.backmeup.model.exceptions.BackMeUpException;
import org.backmeup.model.exceptions.PluginException;
import org.backmeup.model.exceptions.PluginUnavailableException;
import org.backmeup.model.spi.PluginDescribable;
import org.backmeup.model.spi.PluginDescribable.PluginType;
import org.backmeup.model.spi.Validationable;
import org.backmeup.plugin.api.connectors.Action;
import org.backmeup.plugin.api.connectors.Datasink;
import org.backmeup.plugin.api.connectors.Datasource;
import org.backmeup.plugin.spi.Authorizable;
import org.backmeup.plugin.spi.Authorizable.AuthorizationType;
import org.backmeup.plugin.spi.InputBasedAuthorizable;
import org.backmeup.plugin.spi.OAuthBasedAuthorizable;
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
 * @author fschoeppl
 * 
 */
@SuppressWarnings("rawtypes")
public class PluginManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginManager.class);
    private static final String PLUGIN_FILTER_FORMAT = "(name=%s)";

    private String deploymentDirPath;

    private String tempDirPath;

    private String exportedPackages;

    private boolean started;

    private File deploymentDirectory;

    private File temporaryDirectory;

    private Framework osgiFramework;

    private DeploymentMonitor deploymentMonitor;


    public PluginManager() {

    }

    public PluginManager(String deploymentDirectory, String temporaryDirectory, String exportedPackages) {
        LOGGER.debug("Creating PluginImpl");
        this.deploymentDirPath = deploymentDirectory;
        this.tempDirPath = temporaryDirectory;
        this.exportedPackages = exportedPackages;
        this.started = false;
    }

    public void startup() {
        if (!started) {
            LOGGER.debug("Starting up PluginImpl");
            this.tempDirPath = this.tempDirPath + File.separator + Long.toString(System.nanoTime());

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
        this.deploymentMonitor = new DeploymentMonitor(bundleContext(), deploymentDirectory);
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
            config.put(Constants.FRAMEWORK_BOOTDELEGATION, exportedPackages);

            LOGGER.debug("EXPORTED PACKAGES: " + exportedPackages);

            osgiFramework = factory.newFramework(config);
            osgiFramework.start();
        } catch (Exception e) {
            LOGGER.error("", e);
            throw new BackMeUpException(e);
        }
    }

    public void stopOSGiFramework() {
        try {
            osgiFramework.stop();
            osgiFramework.waitForStop(0);
            LOGGER.debug("OsgiFramework stopped.");
        } catch (InterruptedException e) {
            LOGGER.error("", e);
        } catch (BundleException e) {
            LOGGER.error("", e);
        }
    }

    public BundleContext bundleContext() {
        return osgiFramework.getBundleContext();
    }

    public <T> T service(final Class<T> service) {
        return service(service, null);
    }

    private <T> ServiceReference getReference(final Class<T> service, final String pluginId) {
        ServiceReference ref = null;
        if (pluginId == null) {
            ref = bundleContext().getServiceReference(service.getName());
        } else {
            ServiceReference[] refs;
            String filter = String.format(PLUGIN_FILTER_FORMAT, pluginId);
            try {
                refs = bundleContext().getServiceReferences(service.getName(), filter);
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
    public <T> T service(final Class<T> service, final String pluginId) {
        ServiceReference ref = getReference(service, pluginId);
        if (ref == null) {
            throw new PluginUnavailableException(pluginId);
        }
        bundleContext().ungetService(ref);
        return (T) Proxy.newProxyInstance(PluginManager.class.getClassLoader(),
                new Class[] { service }, new InvocationHandler() {

            @Override
            public Object invoke(Object o, Method method, Object[] os) throws Throwable {
                ServiceReference serviceRef = getReference(service, pluginId);
                if (serviceRef == null) {
                    throw new PluginUnavailableException(pluginId);
                }
                Object instance = bundleContext().getService(serviceRef);
                Object ret = null;
                try {
                    ret = method.invoke(instance, os);
                } catch (Exception e) {
                    throw new PluginException(pluginId, "An exception occured during execution of the method " + method.getName(), e);
                } finally {
                    bundleContext().ungetService(serviceRef);
                }
                return ret;
            }
        });
    }

    private static class SpecialInvocationHandler implements InvocationHandler {
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
                LOGGER.error(
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
                    List<T> services = new ArrayList<>();
                    ServiceReference[] refs = bundleContext().getServiceReferences(service.getName(), filter);
                    if (refs != null) {
                        for (ServiceReference s : refs) {
                            services.add((T) Proxy.newProxyInstance(
                                    PluginManager.class.getClassLoader(),
                                    new Class[] { service },
                                    new SpecialInvocationHandler(bundleContext(), s)));
                        }
                    }
                    return services.iterator();
                } catch (Exception e) {
                    throw new BackMeUpException(e);
                }
            }

        };
    }

    public void shutdown() {
        if (started) {
            LOGGER.debug("Shutting down PluginImpl!");
            this.deploymentMonitor.stop();
            this.stopOSGiFramework();
            this.started = false;
        }
    }

    protected List<PluginDescribable> getDescribableForType(PluginType... pluginTypes) {
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

    public boolean isPluginAvailable(String pluginId) {
        ServiceReference ref = getReference(PluginDescribable.class, pluginId);
        return ref != null;
    }

    public boolean hasAuthorizable(String pluginId) {
        ServiceReference ref = getReference(Authorizable.class, pluginId);
        return ref != null;
    }

    public boolean hasValidator(String pluginId) {
        ServiceReference ref = getReference(Validationable.class, pluginId);
        return ref != null;
    }

    public List<PluginDescribable> getActions() {
        return this.getDescribableForType(PluginType.Action);
    }

    public List<PluginDescribable> getDatasinks() {
        return this.getDescribableForType(PluginType.Sink, PluginType.SourceSink);
    }

    public List<PluginDescribable> getDatasources() {
        return this.getDescribableForType(PluginType.Source, PluginType.SourceSink);
    }

    public PluginDescribable getPluginDescribableById(String sourceSinkId) {
        return service(PluginDescribable.class, sourceSinkId);
    }

    public Datasource getDatasource(String sourceId) {
        return service(Datasource.class, sourceId);
    }

    public Datasink getDatasink(String sinkId) {
        return service(Datasink.class, sinkId);
    }

    public Action getAction(String actionId) {
        return service(Action.class, actionId);
    }

    public Authorizable getAuthorizable(String sourceSinkId) {
        return service(Authorizable.class, sourceSinkId);
    }

    public Authorizable getAuthorizable(String sourceSinkId, AuthorizationType authType) {
        switch (authType) {
        case OAuth:
            return service(OAuthBasedAuthorizable.class, sourceSinkId);
        case InputBased:
            return service(InputBasedAuthorizable.class, sourceSinkId);
        default:
            throw new IllegalArgumentException("unknown authorization type " + authType);
        }
    }

    public Validationable getValidator(String sourceSinkId) {
        return service(Validationable.class, sourceSinkId);
    }
}
