/*******************************************************************************
 * Australian National University Data Commons
 * Copyright (C) 2013  The Australian National University
 * 
 * This file is part of Australian National University Data Commons.
 * 
 * Australian National University Data Commons is free software: you
 * can redistribute it and/or modify it under the terms of the GNU
 * General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package au.edu.anu.doi.api;

import javax.ws.rs.core.UriBuilder;

import au.edu.anu.doi.api.config.DoiConfig;

/**
 * @author Rahul Khanna
 *
 */
public class DoiServicePointUriBuilder {

	private static final String XML = "xml";
	
	private DoiConfig doiConfig;

	public DoiServicePointUriBuilder(DoiConfig doiConfig) {
		this.doiConfig = doiConfig;
	}
	
	public UriBuilder getServiceStatusUri() {
		UriBuilder uriBuilder = getBaseUriBuilder().path("/status.{responseType}/");
		
		resolveResponseType(uriBuilder);
		
		return uriBuilder;
	}

	public UriBuilder getMintDoiUri(String doiTargetUrl) {
		UriBuilder uriBuilder = getBaseUriBuilder().path("/mint.{responseType}/").queryParam("app_id", "{appId}")
				.queryParam("url", "{url}").queryParam("debug", "{debug}");

		resolveResponseType(uriBuilder);
		resolveAppId(uriBuilder);
		resolveDoiTargetUrl(doiTargetUrl, uriBuilder);
		resolveDebugFlag(uriBuilder);

		return uriBuilder;
	}

	public UriBuilder getUpdateDoiUri(String doi, String doiResolveUrl) {
		UriBuilder uriBuilder = getBaseUriBuilder().path("/update.{responseType}/").queryParam("app_id", "{appId}")
				.queryParam("doi", "{doi}").queryParam("debug", "{debug}");
		
		if (doiResolveUrl != null) {
			uriBuilder = uriBuilder.queryParam("url", "{url}");
			resolveDoiTargetUrl(doiResolveUrl, uriBuilder);
		}
		
		resolveResponseType(uriBuilder);
		resolveAppId(uriBuilder);
		resolveDoi(doi, uriBuilder);
		resolveDebugFlag(uriBuilder);

		return uriBuilder;
	}

	private UriBuilder resolveDoi(String doi, UriBuilder uriBuilder) {
		return uriBuilder.resolveTemplate("doi", doi);
	}
	

	public UriBuilder getDeactivateDoiUri(String doi) {
		UriBuilder uriBuilder = getBaseUriBuilder().path("/deactivate.{responseType}/").queryParam("app_id", "{appId}")
				.queryParam("doi", "{doi}").queryParam("debug", "{debug}");
		
		resolveResponseType(uriBuilder);
		resolveAppId(uriBuilder);
		resolveDoi(doi, uriBuilder);
		resolveDebugFlag(uriBuilder);

		return uriBuilder;
	}
	
	public UriBuilder getActivateDoiUri(String doi) {
		UriBuilder uriBuilder = getBaseUriBuilder().path("/activate.{responseType}/").queryParam("app_id", "{appId}")
				.queryParam("doi", "{doi}").queryParam("debug", "{debug}");
		
		resolveResponseType(uriBuilder);
		resolveAppId(uriBuilder);
		resolveDoi(doi, uriBuilder);
		resolveDebugFlag(uriBuilder);

		return uriBuilder;
	}
	
	public UriBuilder getDoiMetadataUri(String doi) {
		UriBuilder uriBuilder = getBaseUriBuilder().path("/xml.{responseType}/").queryParam("doi", "{doi}")
				.queryParam("debug", "{debug}");

		resolveResponseType(uriBuilder);
		resolveDoi(doi, uriBuilder);
		resolveDebugFlag(uriBuilder);
		
		return uriBuilder;
	}

	private void resolveResponseType(UriBuilder uriBuilder) {
		uriBuilder.resolveTemplate("responseType", XML);
	}

	private void resolveAppId(UriBuilder uriBuilder) {
		String appId = doiConfig.getAppId();
		
		// prefix appId with 'TEST' if flag set
		if (doiConfig.useTestPrefix()) {
			appId = String.format("TEST%s", appId);
		}
		
		uriBuilder.resolveTemplate("appId", appId);
	}

	private void resolveDoiTargetUrl(String doiTargetUrl, UriBuilder uriBuilder) {
		uriBuilder.resolveTemplate("url", doiTargetUrl);
	}

	private void resolveDebugFlag(UriBuilder uriBuilder) {
		uriBuilder.resolveTemplate("debug", doiConfig.isDebug());
	}

	private UriBuilder getBaseUriBuilder() {
		return UriBuilder.fromPath(doiConfig.getBaseUri());
	}
}
