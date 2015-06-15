package org.backmeup.plugin.osgi;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.backmeup.model.exceptions.BackMeUpException;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The DeployMonitor class starts a thread which will periodically check the
 * osgi.deploymentDirectory (found within plugins.properties) installing new
 * bundles found there. If a bundle gets deleted, it will also be deleted within
 * OSGi.
 * 
 * @author fschoeppl
 */
public class DeployMonitor implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(DeployMonitor.class);

  private final Map<File, Bundle> deployed = new HashMap<>();
  private final List<Bundle> newlyInstalledBundles = new LinkedList<>();
  private final List<File> toBeRemovedBundles = new LinkedList<>();
  private final BundleContext context;
  private final File deploymentDirectory;
  private final Object monitor = new Object();
  
  private ScheduledExecutorService executor;
  private boolean firstRun = false;

  public DeployMonitor(BundleContext context, File deploymentDirectory) {
    this.context = context;
    this.deploymentDirectory = deploymentDirectory;
  }

  public void start() {
    if (executor != null) {
      stop();
    }
    executor = Executors.newScheduledThreadPool(1);
    executor.scheduleAtFixedRate(this, 0, 5, TimeUnit.SECONDS);

  }

  public void waitForInitialRun() {
    try {
        synchronized (monitor) {
            while (!firstRun) {
                monitor.wait();
            }
        }
    } catch (InterruptedException e) {
    	LOGGER.error("", e);
    }
  }

  public void stop() {
    executor.shutdown();
    executor.shutdownNow();
    try {
      executor.awaitTermination(1, TimeUnit.MINUTES);
      LOGGER.error("Awaited termination of executor!");
      synchronized (monitor) {
        firstRun = true;
        monitor.notifyAll();
      }
    } catch (InterruptedException e) {
    	LOGGER.error("", e);
    }
  }

  @Override
  public void run() {
        if (!deploymentDirectory.exists() && !deploymentDirectory.mkdirs()) {
            throw new BackMeUpException("Cannot create deployment directory");
        }

    installBundles();

    removeBundles();
   
    if (!firstRun) {        
      synchronized(monitor) {
        firstRun = true;
        monitor.notifyAll();
      }
    }
  }

private void removeBundles() {
    for (File f : deployed.keySet()) {
      if (!f.exists() || deployed.get(f).getState() == Bundle.UNINSTALLED) {
        try {
          Bundle b = deployed.get(f);
          if (b.getState() == Bundle.ACTIVE) {
            b.stop();
          }
          if (b.getState() != Bundle.UNINSTALLED) {
            b.uninstall();
          }
          toBeRemovedBundles.add(f);
        } catch (Exception e) {
          LOGGER.error("", e);
        }
      }
    }

    for (File toBeRemovedFile : toBeRemovedBundles) {
      deployed.remove(toBeRemovedFile);
    }
    toBeRemovedBundles.clear();
}

private void installBundles() {
    for (File f : deploymentDirectory.listFiles()) {
      if ((f.getName().endsWith(".jar")) && (!deployed.containsKey(f))) {
          try {
            Bundle b = context.installBundle("file:" + f.getAbsolutePath());
            deployed.put(f, b);
            newlyInstalledBundles.add(b);
          } catch (Exception e) {
        	  LOGGER.error("", e);
          }
      }
    }

    for (Bundle newlyInstalledBundle : newlyInstalledBundles) {
      try {
        if (newlyInstalledBundle.getHeaders().get(Constants.FRAGMENT_HOST) == null) {
          newlyInstalledBundle.start();
        }
      } catch (Exception e) {
    	  LOGGER.error("", e);
      }
    }
    newlyInstalledBundles.clear();
}
}
