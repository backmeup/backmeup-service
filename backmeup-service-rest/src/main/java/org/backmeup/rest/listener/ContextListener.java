package org.backmeup.rest.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.backmeup.logic.BusinessLogic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContextListener implements ServletContextListener {
	private final Logger logger = LoggerFactory.getLogger(ContextListener.class);
	
	@Override
	public void contextInitialized(ServletContextEvent sce) {

	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		logger.info("Shutting down business logic...");
		
		// call shutdown on exit
		BusinessLogic logic = (BusinessLogic) sce.getServletContext().getAttribute("org.backmeup.logic");
		if(logic != null) {
			logic.shutdown();
			logic = null;
		}

		logger.info("Shutdown completed");
	}
}
