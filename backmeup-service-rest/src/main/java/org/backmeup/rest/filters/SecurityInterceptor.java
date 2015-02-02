package org.backmeup.rest.filters;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.security.DenyAll;
import javax.annotation.security.PermitAll;
import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

import org.backmeup.logic.BusinessLogic;
import org.backmeup.model.BackMeUpUser;
import org.backmeup.model.exceptions.UnknownUserException;
import org.backmeup.model.exceptions.UserNotActivatedException;
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
	 
	 @Context
	 private ServletContext context;
	 
	 @Inject
	 private BusinessLogic logic;

	 @Override
	 public void filter(ContainerRequestContext requestContext) {
		 ResourceMethodInvoker methodInvoker = (ResourceMethodInvoker) requestContext.getProperty("org.jboss.resteasy.core.ResourceMethodInvoker");
		 Method method = methodInvoker.getMethod();

		 if( !method.isAnnotationPresent(PermitAll.class)) {
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

			 //Split username/userId and password tokens
			 final StringTokenizer tokenizer = new StringTokenizer(accessToken, ";");
			 final String userId = tokenizer.nextToken(); // userId can be a String or an Long in this place (see resolveUser)
			 final String password = tokenizer.nextToken();

			 // Verify token
			 if (method.isAnnotationPresent(RolesAllowed.class)) {
				 RolesAllowed rolesAnnotation = method.getAnnotation(RolesAllowed.class);
				 Set<String> rolesSet = new HashSet<>(Arrays.asList(rolesAnnotation.value()));
				 
				 BackMeUpUser user = resolveUser(userId, password);
				 if(user == null) {
					 requestContext.abortWith(ACCESS_DENIED);
					 return;
				 }
				 
				 if( !isUserAllowed(user, rolesSet)) {
					 requestContext.abortWith(ACCESS_DENIED);
					 return;
				 }

				 user.setPassword(password);
				 requestContext.setSecurityContext(new BackmeupSecurityContext(user));
			 }
		 }
	 }
	 
	private BackMeUpUser resolveUser(final String userId, final String password) {
		try {
			if(userId.equals(BACKMEUP_WORKER_NAME)) {
				BackMeUpUser worker = new BackMeUpUser(BACKMEUP_WORKER_NAME, "", "", "", password);
				worker.setUserId(BACKMEUP_WORKER_ID);
				return worker;
			}
            return logic.getUserByUserId(Long.parseLong(userId));
		} catch (UnknownUserException uue) {
		    LOGGER.info("", uue);
		} catch (UserNotActivatedException una) {
		    LOGGER.info("", una);
		}
		return null;
	}

	private boolean isUserAllowed(final BackMeUpUser user, final Set<String> rolesSet) {
		 boolean isAllowed = true;

		 // Verify user role
		 //		if (rolesSet.contains(userRole)) {
		 //			isAllowed = true;
		 //		}
		 
		 return isAllowed;
	 }
	 
}
