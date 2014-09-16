package org.backmeup.rest.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Timing filter on resources. Monitors how long it takes to process request.
 */
public class TimingResourceFilter implements ContainerRequestFilter, ContainerResponseFilter {
	private final static Logger logger = LoggerFactory.getLogger(TimingResourceFilter.class);
	private final static TimerThreadLocal timer = new TimerThreadLocal();

	public TimingResourceFilter() {

	}

	@Override
	public void filter(ContainerRequestContext request, ContainerResponseContext response) {
		try {
			long reqProcessingTimeInMs = timer.stop();
			logger.info("Request processing time: " + reqProcessingTimeInMs + "ms");
		} finally {
			timer.remove();
		}
	}

	@Override
	public void filter(ContainerRequestContext request) {
		timer.start();
	}

	private final static class TimerThreadLocal extends ThreadLocal<Long> {
		public long start() {
			long value = System.currentTimeMillis();
			this.set(value);
			return value;
		}

		public long stop() {
			return System.currentTimeMillis() - get();
		}

		@Override
		protected Long initialValue() {
			return System.currentTimeMillis();
		}
	}
}
