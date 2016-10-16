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

package au.edu.anu.doi.api.http;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidParameterException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.anu.doi.api.config.DoiConfig;

/**
 * @author Rahul Khanna
 *
 */
public class DoiHttpRequest {

	private String method;
	private URI uri;
	private MultivaluedMap<String, String> headers = new MultivaluedHashMap<>(1);
	private Entity<Form> entity;
	
	private DoiHttpRequest() {
	}
	
	public String getMethod() {
		return this.method;
	}
	
	public URI getUri() {
		return uri;
	}
	
	public MultivaluedMap<String, String> getHeaders() {
		return headers;
	}
	
	public Entity<Form> getEntity() {
		return entity;
	}
	
	@Override
	public String toString() {
		return String.format("method=%s;uri=%s;headers=%s;body=%s", this.getMethod(), this.getUri().toString(),
				this.getHeaders(), this.getEntity().getEntity().asMap());

	}

	
	static abstract class AbstractBuilder {
		
		public enum ResponseType { JSON, XML, STRING;
			@Override
			public String toString() {
				return super.toString().toLowerCase();
			}
		};
		
		protected DoiConfig doiConfig;
		
		protected ResponseType responseType = ResponseType.XML;
		protected Map<String, Object> templateValues = new HashMap<String, Object>();

		protected String doi;
		protected String url;
		protected String resourceDoc;
		
		
		public AbstractBuilder(DoiConfig doiConfig) {
			Objects.requireNonNull(doiConfig);
			this.doiConfig = doiConfig;
		}
		

		public abstract DoiHttpRequest build();
		
		protected UriBuilder getBaseUri() {
			return UriBuilder.fromPath(doiConfig.getBaseUri());
		}

		protected void addTemplateValues() {
			templateValues.put("responseType", responseType);
			templateValues.put("appId", getApplicableAppId());
			if (url != null) {
				templateValues.put("url", url);
			}
		}
		
		protected Entity<Form> createEntity(String resourceDoc) {
			Form form = new Form();
			if (resourceDoc != null && resourceDoc.length() > 0) {
				form.param("xml", resourceDoc);
			}
			return Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
		}

		protected void addAuthHeader(MultivaluedMap<String,String> headers) {
			if (doiConfig.getSharedSecret() != null && doiConfig.getSharedSecret().length() > 0) {
				String appId = getApplicableAppId();
				String authValue = String.format("%s:%s", appId, doiConfig.getSharedSecret());
				authValue = Base64.getEncoder().encodeToString(authValue.getBytes(StandardCharsets.UTF_8));
				authValue = String.format("Basic %s", authValue);
				headers.add("Authorization", authValue);
			}
		}
		
		protected String getApplicableAppId() {
			String appId = doiConfig.getAppId();
			if (doiConfig.useTestPrefix()) {
				appId = String.format("TEST%s", appId);
			}
			return appId;
		}
	}
	
	
	public static class ServiceStatusBuilder extends AbstractBuilder {

		public ServiceStatusBuilder(DoiConfig doiConfig) {
			super(doiConfig);
		}
		
		@Override
		public DoiHttpRequest build() {
			DoiHttpRequest req = new DoiHttpRequest();
			
			// method
			req.method = HttpMethod.GET;
			
			// url
			UriBuilder ub = getBaseUri().path("/status.{responseType}/");
			addTemplateValues();
			ub.resolveTemplates(this.templateValues, true);
			req.uri = ub.build();
			
			return req;
		}
		
	}
	
	public static class MintDoiBuilder extends AbstractBuilder {
	
		public MintDoiBuilder(DoiConfig doiConfig, String url, String resourceDoc) {
			super(doiConfig);
			Objects.requireNonNull(url);
			this.url = url;
			Objects.requireNonNull(resourceDoc);
			this.resourceDoc = resourceDoc;
		}
	
		@Override
		public DoiHttpRequest build() {
			DoiHttpRequest req = new DoiHttpRequest();
			
			// method
			req.method = HttpMethod.POST;
			
			// url
			UriBuilder ub = getBaseUri().path("/mint.{responseType}/");
			ub = ub.queryParam("app_id", "{appId}");
			ub = ub.queryParam("url", "{url}");
			addTemplateValues();
			ub.resolveTemplates(this.templateValues, true);
			req.uri = ub.build();
			
			// header
			addAuthHeader(req.headers);
			
			// entity
			req.entity = createEntity(resourceDoc);
			
			return req;
		}
		
		@Override
		protected void addTemplateValues() {
			super.addTemplateValues();
		}
	}
	
	public static class UpdateDoiBuilder extends AbstractBuilder {
		public UpdateDoiBuilder(DoiConfig doiConfig, String doi) {
			super(doiConfig);
			Objects.requireNonNull(doi);
			this.doi = doi;
		}
		

		@Override
		public DoiHttpRequest build() {
			DoiHttpRequest req = new DoiHttpRequest();

			if (this.url == null && this.resourceDoc == null) {
				throw new IllegalStateException(
						"At least one new URL or XML must be specified for a DOI Update request");
			}

			// method
			req.method = HttpMethod.POST;

			// url
			UriBuilder ub = getBaseUri().path("/update.{responseType}/");
			ub = ub.queryParam("app_id", "{appId}");
			if (url != null) {
				ub = ub.queryParam("url", "{url}");
			}
			addTemplateValues();
			ub.resolveTemplates(this.templateValues, true);
			req.uri = ub.build();

			// header
			addAuthHeader(req.headers);

			// entity
			req.entity = createEntity(resourceDoc);

			return req;
		}
		
		public UpdateDoiBuilder newUrl(String url) {
			Objects.requireNonNull(url);
			this.url = url;
			return this;
		}
		
		public UpdateDoiBuilder newXml(String resourceDoc) {
			Objects.requireNonNull(resourceDoc);
			this.resourceDoc = resourceDoc;
			return this;
		}
	}
	
	public static class DeactivateDoiBuilder extends AbstractBuilder {

		public DeactivateDoiBuilder(DoiConfig doiConfig, String doi) {
			super(doiConfig);
			Objects.requireNonNull(doi);
			this.doi = doi;
		}

		@Override
		public DoiHttpRequest build() {
			// TODO: Implement
			throw new UnsupportedOperationException();
		}
		
	}
	
	public static class ActivateDoiBuilder extends AbstractBuilder {

		public ActivateDoiBuilder(DoiConfig doiConfig, String doi) {
			super(doiConfig);
			Objects.requireNonNull(doi);
			this.doi = doi;
		}

		@Override
		public DoiHttpRequest build() {
			// TODO: Implement
			throw new UnsupportedOperationException();
		}
		
	}


	public static class GetMetadataBuilder extends AbstractBuilder {

		private String doi;
		
		public GetMetadataBuilder(DoiConfig doiConfig, String doi) {
			super(doiConfig);
			Objects.requireNonNull(doi);
			this.doi = doi;
		}

		@Override
		public DoiHttpRequest build() {
			DoiHttpRequest req = new DoiHttpRequest();
			
			// method
			req.method = HttpMethod.GET;
			
			// url
			UriBuilder ub = getBaseUri().path("/xml.{responseType}/");
			ub = ub.queryParam("doi", "{doi}");
			addTemplateValues();
			ub.resolveTemplates(this.templateValues, true);
			req.uri = ub.build();
			
			return req;
		}
		
		@Override
		protected void addTemplateValues() {
			super.addTemplateValues();
			templateValues.put("doi", this.doi);
		}
	}
}
