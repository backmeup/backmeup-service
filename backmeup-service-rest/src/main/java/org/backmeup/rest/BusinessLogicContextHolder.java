package org.backmeup.rest;

import javax.servlet.ServletContext;

import org.backmeup.logic.BusinessLogic;

public class BusinessLogicContextHolder {

    private static final String BACKMEUP_LOGIC_ATTR_NAME = "org.backmeup.logic";

    private final ServletContext servletContext;

    public BusinessLogicContextHolder(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    public BusinessLogic get() {
        return (BusinessLogic) servletContext.getAttribute(BACKMEUP_LOGIC_ATTR_NAME);
    }

    public void set(BusinessLogic logic) {
        servletContext.setAttribute(BACKMEUP_LOGIC_ATTR_NAME, logic);
    }

    public void remove() {
        servletContext.removeAttribute(BACKMEUP_LOGIC_ATTR_NAME);
    }
}
