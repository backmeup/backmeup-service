package org.backmeup.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.backmeup.rest.filters.SecurityInterceptor;
import org.backmeup.rest.filters.TimingResourceFilter;
import org.backmeup.rest.provider.BackMeUpExceptionMapper;
import org.backmeup.rest.provider.JacksonJsonConfiguration;
import org.backmeup.rest.provider.ValidationExceptionMapper;
import org.backmeup.rest.resources.Authentication;
import org.backmeup.rest.resources.BackupJobs;
import org.backmeup.rest.resources.Collections;
import org.backmeup.rest.resources.Plugins;
import org.backmeup.rest.resources.Search;
import org.backmeup.rest.resources.Sharing;
import org.backmeup.rest.resources.Users;

public class BackmeupServiceApplication extends Application {
    private final Set<Class<?>> set = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();

    public BackmeupServiceApplication() {
        // The default life-cycle for resource class instances is per-request. 
        this.set.add(Users.class);
        this.set.add(Authentication.class);
        this.set.add(Plugins.class);
        this.set.add(BackupJobs.class);
        this.set.add(Search.class);
        this.set.add(Sharing.class);
        this.set.add(Collections.class);

        // The default life-cycle for providers (registered directly or via a feature) is singleton.
        this.set.add(JacksonJsonConfiguration.class); // provider
        this.set.add(ValidationExceptionMapper.class);
        this.set.add(BackMeUpExceptionMapper.class);
        this.set.add(TimingResourceFilter.class); // filter = provider
        this.set.add(SecurityInterceptor.class); // filter = provider
    }

    @Override
    public Set<Class<?>> getClasses() {
        return this.set;
    }

    @Override
    public Set<Object> getSingletons() {
        return this.singletons;
    }
}
