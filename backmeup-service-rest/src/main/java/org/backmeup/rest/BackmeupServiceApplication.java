package org.backmeup.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.backmeup.rest.filters.SecurityInterceptor;
import org.backmeup.rest.filters.TimingResourceFilter;
import org.backmeup.rest.provider.JacksonJsonConfiguration;
import org.backmeup.rest.resources.BackupJobs;
import org.backmeup.rest.resources.Plugins;
import org.backmeup.rest.resources.Users;

public class BackmeupServiceApplication extends Application {
	HashSet<Class<?>> set = new HashSet<Class<?>>();
	HashSet<Object> singletons = new HashSet<Object>();
	

	public BackmeupServiceApplication() {
		 singletons.add(new Users());
		 singletons.add(new Plugins());
		 singletons.add(new BackupJobs());
		
		 set.add(JacksonJsonConfiguration.class);
		 set.add(TimingResourceFilter.class);
		 set.add(SecurityInterceptor.class);
	}

	@Override
	public Set<Class<?>> getClasses() {
		return set;
	}

	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
