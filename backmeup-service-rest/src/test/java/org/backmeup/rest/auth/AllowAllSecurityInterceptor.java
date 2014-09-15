package org.backmeup.rest.auth;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.ext.Provider;

import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.FakeUser;
import org.backmeup.rest.auth.BackmeupSecurityContext;

@Provider
public class AllowAllSecurityInterceptor implements ContainerRequestFilter {

    @Override
    public void filter(ContainerRequestContext requestContext) {
        BackMeUpUser user = FakeUser.active();
        requestContext.setSecurityContext(new BackmeupSecurityContext(user));
    }

}
