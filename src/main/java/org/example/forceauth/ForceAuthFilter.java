package org.example.forceauth;

import java.net.URI;
import java.security.Principal;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

import com.force.api.ApiConfig;
import com.force.api.ApiSession;
import com.force.api.Auth;
import com.force.api.AuthorizationRequest;
import com.force.api.AuthorizationResponse;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerRequestFilter;
import com.sun.jersey.spi.container.servlet.WebConfig;

public class ForceAuthFilter implements ContainerRequestFilter {
	
	public static final String COOKIE = "force_session";
	public static final String CLIENT_ID = "org.example.forceauth.client_id";
	public static final String CLIENT_SECRET = "org.example.forceauth.client_secret";
	public static final String AUTH_PATH = "org.example.forceauth.auth_path";

	private static final Logger logger =
            Logger.getLogger(ForceAuthFilter.class.getPackage().getName());
	
	@Context
	UriInfo uriInfo;
	
	@Context
	WebConfig config;
	
	public ContainerRequest filter(ContainerRequest request) {
		logger.log(Level.FINEST,"Begin filtering request "+request.getAbsolutePath());
		if (isAuthCompletionRequest(request)) {
			logger.log(Level.FINEST,"  Request is a auto completion request");
			ApiSession session = completeAuth(request);
			setCookieAndRedirectToOriginal(session, request);
		} else {
			verifyCookieAndSetSecurityContext(request);
		}
		return request;
	}

	// checks if this is a request for completing the oauth handshake. I.e.
	// check if this is the redirect URI as specified in the oauth configuration
	// for this app
	final boolean isAuthCompletionRequest(ContainerRequest request) {
		final String path = config.getInitParameter(AUTH_PATH); 
		return request.getPath().equals(path!=null ? path : "_auth");
	}


	// Completes oauth handshake by exchanging temporary token for access token
	// and refresh token. We throw away refresh tokens btw.
	// If handshake fails it will throw 400 bad request
	final ApiSession completeAuth(ContainerRequest request) {
		return Auth.completeOAuthWebServerFlow(new AuthorizationResponse()
				.apiConfig(getApiConfig())
				.code(request.getQueryParameters().getFirst("code").toString())
				.state(request.getQueryParameters().getFirst("state").toString()));
	}

	final ApiConfig getApiConfig() {
		final String path = config.getInitParameter(AUTH_PATH); 
		return new ApiConfig()
			.setClientId(config.getInitParameter(CLIENT_ID))
			.setClientSecret(config.getInitParameter(CLIENT_SECRET))
			.setRedirectURI(uriInfo.getBaseUri().toASCIIString()+(path!=null ? path : "_auth"));
	}

	// Sets session cookie and redirects to original request before oauth
	// handshake as stored in 'state' parameter.
	final void setCookieAndRedirectToOriginal(ApiSession session, ContainerRequest request) {
		Response response = Response.temporaryRedirect(
					URI.create(request.getQueryParameters().getFirst("state").toString())
				).header("Set-Cookie",COOKIE + "=" + signedCookieFromApiSession(session)).build();
		throw new WebApplicationException(response);
	}

	// Given a valid ApiSession, returns a serialized representation suitable for cookie storage
	final String signedCookieFromApiSession(ApiSession session) {
		// TODO Not signed yet
		System.out.println("Creating signed cookie: "+session.getAccessToken()+"|"+session.getApiEndpoint());
		return session.getAccessToken()+" "+session.getApiEndpoint();
	}

	// reverse of signedCookieFromApiSession
	final ApiSession sessionFromSignedCookie(String cookieValue) {
		// TODO Not signed yet
		String[] a = cookieValue.split(" ");
		System.out.println("Looking for session in cookie value: "+cookieValue);
		System.out.println("Parsed cookie to: accessToken="+a[0]+", endpoint="+a[1]);
		return new ApiSession()
			.setAccessToken(a[0])
			.setApiEndpoint(a[1]);
	}

	// Checks that cookie exists and is valid. Then sets proper security context
	final void verifyCookieAndSetSecurityContext(ContainerRequest request) {
		Cookie cookie = request.getCookies().get(COOKIE);
		if(cookie==null) {
			Response response = Response.temporaryRedirect(URI.create(
					Auth.startOAuthWebServerFlow(new AuthorizationRequest()
						.apiConfig(getApiConfig())
						.state(request.getAbsolutePath().toASCIIString())))
				).build();
			throw new WebApplicationException(response);
		}
		else {
			request.setSecurityContext(new ForceSecurityContext(sessionFromSignedCookie(cookie.getValue())));
		}
	}


	public class ForceSecurityContext implements SecurityContext {

		private Principal principal;

		public ForceSecurityContext(final ApiSession session) {
			this.principal = new ForcePrincipal(getApiConfig(), session);
		}

		public Principal getUserPrincipal() {
			return this.principal;
		}

		public boolean isUserInRole(String role) {
			return false;
		}

		public boolean isSecure() {
			return "https".equals(uriInfo.getRequestUri().getScheme());
		}

		public String getAuthenticationScheme() {
			return "OAuth2";
		}
	}
	
	// private User authenticate(ContainerRequest request) {
	// // Extract authentication credentials
	// String authentication =
	// request.getHeaderValue(ContainerRequest.AUTHORIZATION);
	// if (authentication == null) {
	// throw new MappableContainerException
	// (new AuthenticationException("Authentication credentials are required",
	// REALM));
	// }
	// if (!authentication.startsWith("Basic ")) {
	// return null;
	// // additional checks should be done here
	// // "Only HTTP Basic authentication is supported"
	// }
	// authentication = authentication.substring("Basic ".length());
	// String[] values = new
	// String(Base64.base64Decode(authentication)).split(":");
	// if (values.length < 2) {
	// throw new WebApplicationException(400);
	// // "Invalid syntax for username and password"
	// }
	// String username = values[0];
	// String password = values[1];
	// if ((username == null) || (password == null)) {
	// throw new WebApplicationException(400);
	// // "Missing username or password"
	// }
	//
	// // Validate the extracted credentials
	// User user = null;
	//
	// if (username.equals("user") && password.equals("password")) {
	// user = new User("user", "user");
	// System.out.println("USER AUTHENTICATED");
	// // } else if (username.equals("admin") && password.equals("adminadmin"))
	// {
	// // user = new User("admin", "admin");
	// // System.out.println("ADMIN AUTHENTICATED");
	// } else {
	// System.out.println("USER NOT AUTHENTICATED");
	// throw new MappableContainerException(new
	// AuthenticationException("Invalid username or password\r\n", REALM));
	// }
	// return user;
	// }
	//


}