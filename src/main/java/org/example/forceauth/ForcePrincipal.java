package org.example.forceauth;

import java.security.Principal;

import com.force.api.ApiConfig;
import com.force.api.ApiSession;
import com.force.api.DataApi;

public class ForcePrincipal implements Principal {

	private ApiSession apiSession;
	private ApiConfig apiConfig;

	public ForcePrincipal(ApiConfig apiConfig,ApiSession apiSession) {
		this.apiSession = apiSession;
		this.apiConfig = apiConfig;
	}
	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public ApiSession getApiSession() {
		return apiSession;
	}
	
	public DataApi getDataApi() {
		return new DataApi(apiConfig,apiSession);
	}

}
