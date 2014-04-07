package org.backmeup.rest;

import javax.servlet.ServletContext;
import javax.ws.rs.core.Context;

import org.backmeup.logic.BusinessLogic;
import org.backmeup.rest.cdi.JNDIBeanManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All rest classes derive from this class to gain access to the BusinessLogic. 
 * 
 * Note: The derived classes always delegate the incoming REST call to the business logic.
 * 
 * @author fschoeppl
 */
public class Base {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private BusinessLogic logic;

    @Context
    private ServletContext context;

    protected BusinessLogic getLogic() {
        BusinessLogicContextHolder contextHolder = new BusinessLogicContextHolder(context);

        logic = contextHolder.get();

        if (logic == null) {
            // just in case we are running in an embedded server
            logic = fetchLogicFromJndi();
            contextHolder.set(logic);
        }

        return logic;
    }

    private BusinessLogic fetchLogicFromJndi() {
        try {
            JNDIBeanManager jndiManager = JNDIBeanManager.getInstance();
            return jndiManager.getBean(BusinessLogic.class);
        } catch (Exception e) {
            logger.error("", e);
            return null;
        }
    }
}
