package org.backmeup.rest.resources;

import javax.inject.Inject;

import org.backmeup.logic.BusinessLogic;
import org.dozer.Mapper;

/**
 * All rest classes derive from this class to gain access to the BusinessLogic.
 * Note: The derived classes always delegate the incoming REST call to the
 * business logic.
 */
public class Base {
    @Inject
    private BusinessLogic logic;

    @Inject
    private Mapper mapper;

    protected BusinessLogic getLogic() {
        return logic;
    }

    protected Mapper getMapper() {
        return mapper;
    }

}
