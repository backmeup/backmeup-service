package org.backmeup.rest.listener;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.backmeup.logic.BusinessLogic;
import org.backmeup.rest.BusinessLogicContextHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shutdown the business logic when application stops.
 */
public class ContextListener implements ServletContextListener {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void contextInitialized(ServletContextEvent sce) {
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // call shutdown on exit
        shutdownBusinessLogicFacade(sce.getServletContext());
    }

    private void shutdownBusinessLogicFacade(ServletContext servletContext) {
        logger.info("Shutting down business logic...");

        BusinessLogicContextHolder contextHolder = new BusinessLogicContextHolder(servletContext);

        BusinessLogic logic = contextHolder.get();
        if (logic != null) {
            logic.shutdown();
        }
        
        contextHolder.remove();

        logger.info("Shutdown completed");
    }

}
