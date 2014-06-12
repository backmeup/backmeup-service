package org.backmeup.rest.provider;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import org.backmeup.logic.BusinessLogic;
import org.backmeup.rest.cdi.JNDIBeanManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class looks up the BusinessLogic implementation by using Weld.
 */
@Provider
public class BusinessLogicContextResolver implements ContextResolver<BusinessLogic> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private BusinessLogic logic;

    @Override
    public BusinessLogic getContext(Class<?> type) {
        if (logic == null) {
            try {
                logic = fetchLogicFromJndi();
            } catch (Exception e) {
                logger.error("", e);
            }
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
