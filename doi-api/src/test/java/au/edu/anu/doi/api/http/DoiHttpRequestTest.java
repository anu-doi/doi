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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import javax.ws.rs.HttpMethod;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.anu.doi.api.config.DoiConfig;

/**
 * @author Rahul Khanna
 *
 */
public class DoiHttpRequestTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DoiHttpRequestTest.class);
	
	@Mock DoiConfig doiConfig;
	
	private DoiHttpRequest httpRequest;
	
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		when(doiConfig.getBaseUri()).thenReturn("https://doiservice.org.au/api/doi/");
		when(doiConfig.getAppId()).thenReturn("applicationIdentifier");
		when(doiConfig.useTestPrefix()).thenReturn(false);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testServiceStatus() throws Exception {
		httpRequest = new DoiHttpRequest.ServiceStatusBuilder(doiConfig).build();
		
		assertThat(httpRequest.getMethod(), is(HttpMethod.GET));
		assertThat(httpRequest.getUri().toString(), is("https://doiservice.org.au/api/doi/status.xml/"));
		assertThat(httpRequest.getHeaders().entrySet(), hasSize(0));
		assertThat(httpRequest.getEntity(), is(nullValue()));
	}

	@Test
	public void testMint() throws Exception {
		String xml = readSampleXmlFile();
		httpRequest = new DoiHttpRequest.MintDoiBuilder(doiConfig, "http://abc.com", xml).build();
		
		assertThat(httpRequest.getMethod(), is(HttpMethod.POST));
		assertThat(httpRequest.getUri().toString(), startsWith("https://doiservice.org.au/api/doi/mint.xml/?"));
		assertThat(httpRequest.getUri().toString(), containsString("app_id=applicationIdentifier"));
		assertThat(httpRequest.getUri().toString(), containsString("url=http%3A%2F%2Fabc.com"));
		assertThat(httpRequest.getHeaders().entrySet(), hasSize(0));
		assertThat(httpRequest.getEntity(), is(notNullValue()));
		assertThat(httpRequest.getEntity().getEntity().asMap(), hasEntry("xml", Arrays.asList(xml)));
	}
	
	@Test
	public void testMintWithSharedSecret() throws Exception {
		when(doiConfig.getSharedSecret()).thenReturn("s3cret");
		
		String xml = readSampleXmlFile();
		DoiHttpRequest httpRequest = new DoiHttpRequest.MintDoiBuilder(doiConfig, "http://abc.com", xml).build();
		
		assertThat(httpRequest.getMethod(), is(HttpMethod.POST));
		assertThat(httpRequest.getUri().toString(), startsWith("https://doiservice.org.au/api/doi/mint.xml/?"));
		assertThat(httpRequest.getUri().toString(), containsString("app_id=applicationIdentifier"));
		assertThat(httpRequest.getUri().toString(), containsString("url=http%3A%2F%2Fabc.com"));
		assertThat(httpRequest.getHeaders().entrySet(), hasSize(1));
		assertThat(httpRequest.getHeaders(), hasEntry("Authorization", Arrays.asList("Basic YXBwbGljYXRpb25JZGVudGlmaWVyOnMzY3JldA==")));
		assertThat(httpRequest.getEntity(), is(notNullValue()));
		assertThat(httpRequest.getEntity().getEntity().asMap(), hasEntry("xml", Arrays.asList(xml)));
	}
	
	@Test
	public void testUpdateUrlOnly() throws Exception {
		httpRequest = new DoiHttpRequest.UpdateDoiBuilder(doiConfig, "10.5072/13/50639BFE25F18")
				.newUrl("https://newurl.com").build();
		
		assertThat(httpRequest.getMethod(), is(HttpMethod.POST));
		assertThat(httpRequest.getUri().toString(), startsWith("https://doiservice.org.au/api/doi/update.xml/?"));
		assertThat(httpRequest.getUri().toString(), containsString("app_id=applicationIdentifier"));
		assertThat(httpRequest.getUri().toString(), containsString("url=https%3A%2F%2Fnewurl.com"));
		assertThat(httpRequest.getHeaders().entrySet(), hasSize(0));
		assertThat(httpRequest.getEntity().getEntity().asMap().entrySet(), Matchers.hasSize(0));
	}
	
	@Test
	public void testUpdateXmlOnly() throws Exception {
		String xml = readSampleXmlFile();
		httpRequest = new DoiHttpRequest.UpdateDoiBuilder(doiConfig, "10.5072/13/50639BFE25F18").newXml(xml).build();

		assertThat(httpRequest.getMethod(), is(HttpMethod.POST));
		assertThat(httpRequest.getUri().toString(), startsWith("https://doiservice.org.au/api/doi/update.xml/?"));
		assertThat(httpRequest.getUri().toString(), containsString("app_id=applicationIdentifier"));
		assertThat(httpRequest.getUri().toString(), not(containsString("url=")));
		assertThat(httpRequest.getHeaders().entrySet(), hasSize(0));
		assertThat(httpRequest.getEntity().getEntity().asMap(), hasEntry("xml", Arrays.asList(xml)));
	}
	
	@Test
	public void testUpdateUrlAndXml() throws Exception {
		String xml = readSampleXmlFile();
		httpRequest = new DoiHttpRequest.UpdateDoiBuilder(doiConfig, "10.5072/13/50639BFE25F18")
				.newUrl("https://newurl.com").newXml(xml).build();

		assertThat(httpRequest.getMethod(), is(HttpMethod.POST));
		assertThat(httpRequest.getUri().toString(), startsWith("https://doiservice.org.au/api/doi/update.xml/?"));
		assertThat(httpRequest.getUri().toString(), containsString("app_id=applicationIdentifier"));
		assertThat(httpRequest.getUri().toString(), containsString("url=https%3A%2F%2Fnewurl.com"));
		assertThat(httpRequest.getHeaders().entrySet(), hasSize(0));
		assertThat(httpRequest.getEntity().getEntity().asMap(), hasEntry("xml", Arrays.asList(xml)));
	}
	
	@Test
	public void testUpdateUrlAndXmlWithSharedSecret() throws Exception {
		when(doiConfig.getSharedSecret()).thenReturn("s3cret");
		
		String xml = readSampleXmlFile();
		httpRequest = new DoiHttpRequest.UpdateDoiBuilder(doiConfig, "10.5072/13/50639BFE25F18")
				.newUrl("https://newurl.com").newXml(xml).build();
		
		assertThat(httpRequest.getMethod(), is(HttpMethod.POST));
		assertThat(httpRequest.getUri().toString(), startsWith("https://doiservice.org.au/api/doi/update.xml/?"));
		assertThat(httpRequest.getUri().toString(), containsString("app_id=applicationIdentifier"));
		assertThat(httpRequest.getUri().toString(), containsString("url=https%3A%2F%2Fnewurl.com"));
		assertThat(httpRequest.getHeaders().entrySet(), hasSize(1));
		assertThat(httpRequest.getHeaders(), hasEntry("Authorization", Arrays.asList("Basic YXBwbGljYXRpb25JZGVudGlmaWVyOnMzY3JldA==")));
		assertThat(httpRequest.getEntity().getEntity().asMap(), hasEntry("xml", Arrays.asList(xml)));
	}
	
	
	@Test(expected = IllegalArgumentException.class)
	public void testUpdateWithoutUrlAndDoi() throws Exception {
		httpRequest = new DoiHttpRequest.UpdateDoiBuilder(doiConfig, "10.5072/13/50639BFE25F18").build();
	}

	@Test
	public void testGetMetadata() throws Exception {
		httpRequest = new DoiHttpRequest.GetMetadataBuilder(doiConfig, "10.5072/13/50639BFE25F18").build();
		
		assertThat(httpRequest.getMethod(), is(HttpMethod.GET));
		assertThat(httpRequest.getUri().toString(), startsWith("https://doiservice.org.au/api/doi/xml.xml/?"));
		assertThat(httpRequest.getUri().toString(), containsString("doi=10.5072%2F13%2F50639BFE25F18"));
		assertThat(httpRequest.getHeaders().entrySet(), hasSize(0));
		assertThat(httpRequest.getEntity(), is(nullValue()));
	}
	
	private String readSampleXmlFile() throws IOException {
		StringWriter sw = new StringWriter();
		InputStream xmlStream = new BufferedInputStream(this.getClass().getResourceAsStream("resource-sample1.xml"));
		IOUtils.copy(xmlStream, sw, StandardCharsets.UTF_8);
		return sw.toString();
	}
}
