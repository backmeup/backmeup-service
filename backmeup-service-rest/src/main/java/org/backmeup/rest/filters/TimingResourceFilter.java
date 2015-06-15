package org.backmeup.rest.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.ext.Provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Timing filter on resources. Monitors how long it takes to process request.
 */
@Provider
public class TimingResourceFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimingResourceFilter.class);
    private static final TimerThreadLocal TIMER = new TimerThreadLocal();
    
    @Context
    ResourceInfo info;

    @Override
    public void filter(ContainerRequestContext request) {
        TIMER.start();
    }

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        try {
            long reqProcessingTimeInMs = TIMER.stop();
            
            StringBuilder sb = new StringBuilder();
            sb.append(request.getMethod());
            sb.append(" ");
            sb.append(info.getResourceClass().getName());
            sb.append(" ");
            sb.append(info.getResourceMethod().getName());
            sb.append(" ");
            sb.append(response.getStatus());
            sb.append(" ");
            sb.append(reqProcessingTimeInMs);
            
            LOGGER.info(sb.toString());
        } finally {
            TIMER.remove();
        }
    }

}

final class TimerThreadLocal extends ThreadLocal<Long> {
    public long start() {
        long value = currentTimeMillis();
        this.set(value);
        return value;
    }

    public long stop() {
        return currentTimeMillis() - get();
    }

    @Override
    protected Long initialValue() {
        return currentTimeMillis();
    }

    private long currentTimeMillis() {
        return System.currentTimeMillis();
    }
}
