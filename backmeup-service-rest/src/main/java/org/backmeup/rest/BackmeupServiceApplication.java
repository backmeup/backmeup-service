package org.backmeup.rest;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.Application;

import org.backmeup.rest.filters.SecurityInterceptor;
import org.backmeup.rest.filters.TimingResourceFilter;
import org.backmeup.rest.provider.JacksonJsonConfiguration;
import org.backmeup.rest.provider.PluginExceptionMapper;
import org.backmeup.rest.provider.ValidationExceptionMapper;
import org.backmeup.rest.resources.Authentication;
import org.backmeup.rest.resources.BackupJobs;
import org.backmeup.rest.resources.Plugins;
import org.backmeup.rest.resources.Users;
import org.backmeup.rest.resources.Search;

public class BackmeupServiceApplication extends Application {
    private final Set<Class<?>> set = new HashSet<>();
    private final Set<Object> singletons = new HashSet<>();

    public BackmeupServiceApplication() {
        // The default life-cycle for resource class instances is per-request. 
        set.add(Users.class);
        set.add(Authentication.class);
        set.add(Plugins.class);
        set.add(BackupJobs.class);
        set.add(Search.class);

        // The default life-cycle for providers (registered directly or via a feature) is singleton.
        set.add(JacksonJsonConfiguration.class); // provider
        set.add(ValidationExceptionMapper.class);
        set.add(PluginExceptionMapper.class);
        set.add(TimingResourceFilter.class); // filter = provider
        set.add(SecurityInterceptor.class); // filter = provider
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
