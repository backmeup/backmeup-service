package org.backmeup.rest.filters;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.backmeup.keyserver.client.KeyserverClient;
import org.backmeup.keyserver.model.KeyserverException;
import org.backmeup.keyserver.model.Token.Kind;
import org.backmeup.keyserver.model.dto.AuthResponseDTO;
import org.backmeup.keyserver.model.dto.AuthResponseDTO.Role;
import org.backmeup.keyserver.model.dto.TokenDTO;
import org.backmeup.logic.BusinessLogic;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.WorkerInfo;
import org.backmeup.model.exceptions.UnknownUserException;
import org.backmeup.model.exceptions.UserNotActivatedException;
import org.backmeup.rest.auth.BackmeupPrincipal;
import org.backmeup.rest.auth.BackmeupSecurityContext;
import org.jboss.resteasy.core.Headers;
import org.jboss.resteasy.core.ResourceMethodInvoker;
import org.jboss.resteasy.core.ServerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class SecurityInterceptor implements ContainerRequestFilter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityInterceptor.class);

    private static final ServerResponse ACCESS_FORBIDDEN = new ServerResponse("Access forbidden", 403, new Headers<>());
    private static final ServerResponse ACCESS_DENIED = new ServerResponse("Access denied for this resource", 401, new Headers<>());
    private static final String AUTHORIZATION_PROPERTY = "Authorization";

    public static final Long BACKMEUP_WORKER_ID = -1L;
    public static final String BACKMEUP_WORKER_NAME = "BACKMEUPWORKER";

    @Inject
    private BusinessLogic logic;
    
    @Inject
    private KeyserverClient keyserverClient;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
        Method method = methodInvoker.getMethod();

        if (!method.isAnnotationPresent(PermitAll.class)) {
            if(method.isAnnotationPresent(DenyAll.class)) {
                requestContext.abortWith(ACCESS_FORBIDDEN);
                return;
            }

            // Get authorization header
            final MultivaluedMap<String, String> headers = requestContext.getHeaders();
            final List<String> authorization = headers.get(AUTHORIZATION_PROPERTY);

            // If no authorization header, deny access
            if (authorization == null || authorization.isEmpty()) {
                requestContext.abortWith(ACCESS_DENIED);
                return;
            }

            // Get token from header
            final String accessToken = authorization.get(0);

            // Verify token
            if (method.isAnnotationPresent(RolesAllowed.class)) {
                RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
                Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));

                BackmeupPrincipal principal = resolvePrincipal(accessToken);
                if(principal == null) {
                    requestContext.abortWith(ACCESS_DENIED);
                    return;
                }

                if( !isPrincipalAllowed(principal, rolesSet)) {
                    requestContext.abortWith(ACCESS_DENIED);
                    return;
                }

                requestContext.setSecurityContext(new BackmeupSecurityContext(principal));
            }
        }
    }
    
    private BackmeupPrincipal resolvePrincipal(final String accessToken) {
        try {
            
            TokenDTO token = new TokenDTO(Kind.INTERNAL, accessToken);
            AuthResponseDTO response = keyserverClient.authenticateWithInternalToken(token);
            
            if (response.getRoles().contains(Role.USER)) {
                BackMeUpUser user = logic.getUserByKeyserverUserId(response.getServiceUserId());
                return new BackmeupPrincipal(user.getUserId().toString(), user, accessToken);
            } else if (response.getRoles().contains(Role.WORKER)) {
                //ServiceUserId returned in the AuthResponse from Keyserver is equal to AppId (WorkerId)
                WorkerInfo worker = logic.getWorkerByWorkerId(response.getServiceUserId()); 
                if(worker == null) {
                    // TODO: In case of predefined workers (on keyserver), there is no worker info in db
                    // Therefore, this workaround:
                    worker = new WorkerInfo();
                    worker.setWorkerId(response.getServiceUserId());
                }
                return new BackmeupPrincipal(worker.getWorkerId(), worker, accessToken);
            }
            
        } catch (KeyserverException ke) {
            LOGGER.info("", ke);
        } catch (UnknownUserException uue) {
            LOGGER.info("", uue);
        } catch (UserNotActivatedException una) {
            LOGGER.info("", una);
        }
        return null;
    }

    private boolean isPrincipalAllowed(final BackmeupPrincipal principal, final Set<String> rolesSet) {
        return true;
    }

}
