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

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.anu.doi.api.config.DoiConfig;
import au.edu.anu.doi.api.http.DoiHttpRequest;
import au.edu.anu.doi.api.http.DoiHttpRequest.UpdateDoiBuilder;
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

	private List<DoiServiceEventListener> doiSvcEvtListeners = new ArrayList<>();

	public DoiService(Client client, DoiConfig doiConfig) {
		Objects.requireNonNull(client);
		Objects.requireNonNull(doiConfig);

		this.client = client;
		this.doiConfig = doiConfig;

		this.doiRespUnmarshaller = new DoiResponseUnmarshaller();
	}

	public DoiResponse getServiceStatus() throws DoiException {
		try {
			DoiHttpRequest httpReq = new DoiHttpRequest.ServiceStatusBuilder(doiConfig).build();
			Response respFromAnds = submitRequest(httpReq);
			return processResponse(respFromAnds, "MT090");
		} catch (Exception e) {
			throw new DoiException(e);
		}
	}

	public String getMetadata(String doi) throws DoiException {
		try {
			DoiHttpRequest httpReq = new DoiHttpRequest.GetMetadataBuilder(doiConfig, doi).build();
			Response respFromAnds = submitRequest(httpReq);
			String metadata = extractEntityAsString(respFromAnds);
			LOGGER.debug("DOI Get Metadata response:{}{}", System.lineSeparator(), metadata);
			respFromAnds.close();
			return metadata;

		} catch (Exception e) {
			throw new DoiException(e);
		}
	}

	public DoiResponse mint(String doiUrl, String resourceDoc) throws DoiException {
		try {
			DoiHttpRequest httpReq = new DoiHttpRequest.MintDoiBuilder(doiConfig, doiUrl, resourceDoc).build();
			Response respFromAnds = submitRequest(httpReq);
			return processResponse(respFromAnds, "MT001");
		} catch (Exception e) {
			throw new DoiException(e);
		}
	}

	public DoiResponse update(String doi, String doiUrl, String resourceDoc) throws DoiException {
		try {
			UpdateDoiBuilder updateDoiBuilder = new DoiHttpRequest.UpdateDoiBuilder(doiConfig, doi);
			if (doiUrl != null && doiUrl.length() > 0) {
				updateDoiBuilder = updateDoiBuilder.newUrl(doiUrl);
			}
			if (resourceDoc != null && resourceDoc.length() > 0) {
				updateDoiBuilder = updateDoiBuilder.newXml(resourceDoc);
			}
			DoiHttpRequest httpReq = updateDoiBuilder.build();
			Response respFromAnds = submitRequest(httpReq);
			return processResponse(respFromAnds, "MT002");
		} catch (Exception e) {
			throw new DoiException(e);
		}

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

	private Response submitRequest(DoiHttpRequest httpRequest) {
		// create WebTarget from URI
		WebTarget webTarget = client.target(httpRequest.getUri());
		// add http headers, if any
		Builder builder = webTarget.request();
		for (Entry<String, List<String>> headerEntry : httpRequest.getHeaders().entrySet()) {
			for (String headerValue : headerEntry.getValue()) {
				builder = builder.header(headerEntry.getKey(), headerValue);
			}
		}
		// submit request
		Response httpResponse = builder.method(httpRequest.getMethod(), httpRequest.getEntity());
		return httpResponse;
	}

	private DoiResponse processResponse(Response respFromDoiSvc, String expectedRespCode) throws DoiException {
		Objects.requireNonNull(respFromDoiSvc);
		Objects.requireNonNull(expectedRespCode);

		String respBody = extractEntityAsString(respFromDoiSvc);
		respFromDoiSvc.close();
		LOGGER.debug("DOI Service response: {}", respBody);
		if (respFromDoiSvc.getStatus() != Status.OK.getStatusCode()) {
			DoiException doiException = new DoiException(
					String.format("Unexpected HTTP Status: %d [%s]", respFromDoiSvc.getStatus(), respBody));
			doiException.setRespStr(respBody);
			throw doiException;
		}

		// unmarshall response body into a DoiResponse object
		DoiResponse doiResponse = null;
		try {
			doiResponse = unmarshallDoiResponse(respBody);
		} catch (JAXBException e) {
			DoiException doiException = new DoiException(String.format("Invalid response format: %s", respBody));
			doiException.setRespStr(respBody);
			throw doiException;
		}

		// throw exception if response type is failure
		if (!doiResponse.getType().equals("success")) {
			DoiException doiException = new DoiException(
					String.format("Response type failure: %s. Expected %s. Body: %s", doiResponse.getCode(),
							expectedRespCode, respBody));
			doiException.setResp(doiResponse);
			doiException.setRespStr(respBody);
			throw doiException;
		}

		// throw exception if the request failed.
		if (!doiResponse.getCode().equals(expectedRespCode)) {
			DoiException doiException = new DoiException(
					String.format("Unexpected response code: %s. Expected %s. Body: %s", doiResponse.getCode(),
							expectedRespCode, respBody));
			doiException.setResp(doiResponse);
			doiException.setRespStr(respBody);
			throw doiException;
		}
		return doiResponse;
	}

	private DoiResponse unmarshallDoiResponse(String str) throws JAXBException {
		return (DoiResponse) doiRespUnmarshaller.unmarshal(str);
	}

}
