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

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.anu.doi.api.config.DoiConfig;
import au.edu.anu.doi.api.response.DoiResponse;
import au.edu.anu.doi.api.response.DoiResponseUnmarshaller;

/**
 * @author Rahul Khanna
 *
 */
public class DoiService {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DoiService.class);
	
	public enum ResponseFormat {
		XML, JSON, STRING;

		@Override
		public String toString() {
			return super.toString().toLowerCase();
		}
	}
	
	private Client client;
	private DoiConfig doiConfig;
	private DoiResponseUnmarshaller doiRespUnmarshaller;
	
	private DoiServicePointUriBuilder dspUriBuilder;
	
	private List<DoiServiceEventListener> doiSvcEvtListeners = new ArrayList<>();
	

	public DoiService(Client client, DoiConfig doiConfig) {
		Objects.requireNonNull(client);
		Objects.requireNonNull(doiConfig);
		
		this.client = client;
		this.doiConfig = doiConfig;
		
		this.doiRespUnmarshaller = new DoiResponseUnmarshaller();
		this.dspUriBuilder = new DoiServicePointUriBuilder(this.doiConfig);
	}
	

	public DoiResponse getServiceStatus() throws DoiException {
		// build uri
		URI doiGetServiceStatusUri = dspUriBuilder.getServiceStatusUri().build();
		LOGGER.debug("Getting DOI service status from {}", doiGetServiceStatusUri.toString());
	
		WebTarget target = client.target(doiGetServiceStatusUri);
		Response respFromAnds = target.request(MediaType.APPLICATION_XML_TYPE).get();
		
		return processResponse(respFromAnds, "MT090");
	}

	public String getMetadata(String doi) throws DoiException {
		// validate
		Objects.requireNonNull(doi);
		
		// build uri
		URI doiGetMetadataUri = dspUriBuilder.getDoiMetadataUri(doi).build();
		LOGGER.debug("Getting metadata for DOI {} from {}", doi, doiGetMetadataUri.toString());
		
		WebTarget target = client.target(doiGetMetadataUri);
		Response respFromDoiSvc = target.request(MediaType.APPLICATION_XML_TYPE).get();
		
		// process response
		String respBody = extractEntityAsString(respFromDoiSvc);
		LOGGER.debug("DOI Get Metadata response:{}{}", System.lineSeparator(), respBody);
		if (respFromDoiSvc.getStatus() != Status.OK.getStatusCode()) {
			throw new DoiException("Unexpected HTTP Status: " + respFromDoiSvc.getStatus() + ":" + respBody);
		}
		respFromDoiSvc.close();
		
		return respBody;
	}

	public DoiResponse mint(String doiUrl, String resourceDoc) throws DoiException {
		Objects.requireNonNull(doiUrl);
		Objects.requireNonNull(resourceDoc);
		
		// create doi service endpoint uri
		URI doiMintUri = dspUriBuilder.getMintDoiUri(doiUrl).build();
		LOGGER.debug("Minting DOI at {} with url={};metadata={}", doiMintUri.toString(), doiUrl, resourceDoc);

		// send http post request
		Entity<Form> resourceDocEntity = prepareEntityForm(resourceDoc);
		Builder reqBuilder = client.target(doiMintUri).request(MediaType.APPLICATION_XML_TYPE);
		reqBuilder = addSharedSecret(reqBuilder);
		
		Response respFromDoiSvc = reqBuilder.post(resourceDocEntity);
		
		return processResponse(respFromDoiSvc, "MT001");
	}
	
	public DoiResponse update(String doi, String doiUrl, String resourceDoc) throws DoiException {
		// validate
		Objects.requireNonNull(doi);
		if (Objects.isNull(doiUrl) && Objects.isNull(resourceDoc)) {
			throw new NullPointerException("doiUrl and resourceDoc both cannot be null");
		}
		
		// create doi service endpoint uri
		URI doiUpdateUri = dspUriBuilder.getUpdateDoiUri(doi, doiUrl).build();
		LOGGER.debug("Updating DOI at {} with doi={};url={};xml={}", doiUpdateUri.toString(), doi, doiUrl, resourceDoc);
		
		// submit http post request
		Entity<Form> resourceDocEntity = prepareEntityForm(resourceDoc);
		Builder reqBuilder = client.target(doiUpdateUri).request(MediaType.APPLICATION_XML_TYPE);
		reqBuilder = addSharedSecret(reqBuilder);
		
		Response respFromDoiSvc = reqBuilder.post(resourceDocEntity);

		return processResponse(respFromDoiSvc, "MT002");
	}

	public Response activate(String doi, String doiUrl) throws DoiException {
		Response resp = null;
		
		// TODO
		
		return resp;
	}

	public Response deactivate(String doi, String doiUrl) throws DoiException {
		Response resp = null;
		
		// TODO
		
		return resp;
	}
	
	public String extractEntityAsString(Response resp) {
		String doiResponseAsString = resp.readEntity(String.class);
		return doiResponseAsString;
	}
	
	public void addListener(DoiServiceEventListener listener) {
		Objects.requireNonNull(listener);
		
		if (!doiSvcEvtListeners.contains(listener)) {
			doiSvcEvtListeners.add(listener);
		}
	}
	
	public void removeListener(DoiServiceEventListener listener) {
		Objects.requireNonNull(listener);
		
		doiSvcEvtListeners.remove(listener);
	}
	
	private Builder addSharedSecret(Builder reqBuilder) {
		// add auth header only if shared secret specified
		if (doiConfig.getSharedSecret() != null && doiConfig.getSharedSecret().length() > 0) {
			
			String appId = doiConfig.getAppId();
			if (doiConfig.useTestPrefix()) {
				appId = String.format("TEST%s", appId);
			}
			String authValue = String.format("%s:%s", appId, doiConfig.getSharedSecret());
			authValue = Base64.getEncoder().encodeToString(authValue.getBytes(StandardCharsets.UTF_8));
			authValue = String.format("Basic %s", authValue);
			reqBuilder = reqBuilder.header("Authorization", authValue);
		}
		return reqBuilder;
	}

	private DoiResponse processResponse(Response respFromDoiSvc, String expectedRespCode) throws DoiException {
		Objects.requireNonNull(respFromDoiSvc);
		Objects.requireNonNull(expectedRespCode);

		String respBody = extractEntityAsString(respFromDoiSvc);
		respFromDoiSvc.close();
		LOGGER.debug("DOI Service response: {}", respBody);
		if (respFromDoiSvc.getStatus() != Status.OK.getStatusCode()) {
			throw new DoiException(String.format("Unexpected HTTP Status: %d [%s]", respFromDoiSvc.getStatus(), respBody));
		}

		// unmarshall response body into a DoiResponse object
		DoiResponse doiResponse = null;
		try {
			doiResponse = unmarshallDoiResponse(respBody);
		} catch (JAXBException e) {
			throw new DoiException(String.format("Invalid response format: %s", respBody));
		}

		// throw exception if response type is failure
		if (!doiResponse.getType().equals("success"))
		{
			throw new DoiException(String.format("Response type failure: %s. Expected %s. Body: %s",
					doiResponse.getCode(), expectedRespCode, respBody));
		}
		
		// throw exception if the request failed.
		if (!doiResponse.getCode().equals(expectedRespCode)) {
			throw new DoiException(String.format("Unexpected response code: %s. Expected %s. Body: %s",
					doiResponse.getCode(), expectedRespCode, respBody));
		}
		return doiResponse;
	}

	private Entity<Form> prepareEntityForm(String resourceDoc) {
		Form form = new Form();
		if (resourceDoc != null) {
			form.param("xml", resourceDoc);
		}
		Entity<Form> resourceDocEntity = Entity.entity(form, MediaType.APPLICATION_FORM_URLENCODED_TYPE);
		return resourceDocEntity;
	}

	private DoiResponse unmarshallDoiResponse(String str) throws JAXBException {
		return (DoiResponse) doiRespUnmarshaller.unmarshal(str);
	}
	
	
}
