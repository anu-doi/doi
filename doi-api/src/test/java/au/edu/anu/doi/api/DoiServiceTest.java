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

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.anu.doi.api.config.DoiConfig;
import au.edu.anu.doi.api.response.DoiResponse;
import au.edu.anu.doi.api.response.DoiResponseUnmarshaller;

/**
 * @author Rahul Khanna
 *
 */
public class DoiServiceTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DoiServiceTest.class);
	
	@Mock private DoiConfig doiConfig;
	@Mock private Client client;
	
	@InjectMocks private DoiService doiSvc;
	
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
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}
	
	@Ignore
//	@Test
	public void testGetServiceStatus() throws Exception {
		when(doiConfig.getBaseUri()).thenReturn("https://services.ands.org.au/doi/1.1/");
		
		WebTarget mockWebTarget = mock(WebTarget.class);
		when(client.target(Mockito.any(URI.class))).thenReturn(mockWebTarget);
		
		DoiResponse doiResp = doiSvc.getServiceStatus();
		LOGGER.trace("DOI Response: {}", doiResp.toString());
		assertThat(doiResp, Matchers.notNullValue());
	}

	@Ignore
//	@Test
	public void testGetMetadata() throws Exception {
		when(doiConfig.getBaseUri()).thenReturn("https://services.ands.org.au/doi/1.1/");
		when(doiConfig.getAppId()).thenReturn("0ca474877510b101b26f9e4192bacba9ca762b15");
		when(doiConfig.useTestPrefix()).thenReturn(true);
		when(doiConfig.isDebug()).thenReturn(false);
		
		String resp = doiSvc.getMetadata("10.4225/13/511C71F8612C3");
		assertThat(resp, Matchers.notNullValue());
	}

}
