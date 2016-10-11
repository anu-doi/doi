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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import javax.ws.rs.core.UriBuilder;

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
public class DoiServicePointUriBuilderTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(DoiServicePointUriBuilderTest.class);
	
	@Mock private DoiConfig doiConfig;
	
	private DoiServicePointUriBuilder doiUriBuilder;
	
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
		doiUriBuilder = new DoiServicePointUriBuilder(doiConfig); 
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testGetServiceStatusUri() {
		when(doiConfig.getBaseUri()).thenReturn("https://services.ands.org.au/doi/1.1/");
		when(doiConfig.isDebug()).thenReturn(false);
		
		UriBuilder uriBuilder = doiUriBuilder.getServiceStatusUri();
		LOGGER.trace(uriBuilder.toString());
		assertThat(uriBuilder.toString(), is("https://services.ands.org.au/doi/1.1/status.xml/"));
	}
	
	@Test
	public void testGetMintDoiUri() {
		when(doiConfig.getBaseUri()).thenReturn("https://services.ands.org.au/doi/1.1/");
		when(doiConfig.getAppId()).thenReturn("0ca474877510b101b26f9e4192bacba9ca762b15");
		when(doiConfig.useTestPrefix()).thenReturn(false, true);
		when(doiConfig.isDebug()).thenReturn(false);
		
		UriBuilder uriBuilder;
		
		uriBuilder = doiUriBuilder.getMintDoiUri("http://someurl.com/idofrecord");
		LOGGER.trace(uriBuilder.toString());
		assertThat(uriBuilder.toString(), is("https://services.ands.org.au/doi/1.1/mint.xml/?app_id=0ca474877510b101b26f9e4192bacba9ca762b15&url=http%3A%2F%2Fsomeurl.com%2Fidofrecord&debug=false"));
		
		uriBuilder = doiUriBuilder.getMintDoiUri("http://someurl.com/idofrecord");
		LOGGER.trace(uriBuilder.toString());
		assertThat(uriBuilder.toString(), is("https://services.ands.org.au/doi/1.1/mint.xml/?app_id=TEST0ca474877510b101b26f9e4192bacba9ca762b15&url=http%3A%2F%2Fsomeurl.com%2Fidofrecord&debug=false"));
		
	}
	
	@Test
	public void testGetUpdateDoiUriWResolveUrl() {
		when(doiConfig.getBaseUri()).thenReturn("https://services.ands.org.au/doi/1.1/");
		when(doiConfig.getAppId()).thenReturn("0ca474877510b101b26f9e4192bacba9ca762b15");
		when(doiConfig.useTestPrefix()).thenReturn(false, true);
		when(doiConfig.isDebug()).thenReturn(false);

		UriBuilder uriBuilder;
		
		uriBuilder = doiUriBuilder.getUpdateDoiUri("10.5072/01/4DD9C98890330", "http://someurl.com/idofrecord");
		LOGGER.trace(uriBuilder.toString());
		assertThat(uriBuilder.toString(), is("https://services.ands.org.au/doi/1.1/update.xml/?app_id=0ca474877510b101b26f9e4192bacba9ca762b15&doi=10.5072%2F01%2F4DD9C98890330&debug=false&url=http%3A%2F%2Fsomeurl.com%2Fidofrecord"));
		
		uriBuilder = doiUriBuilder.getUpdateDoiUri("10.5072/01/4DD9C98890330", "http://someurl.com/idofrecord");
		LOGGER.trace(uriBuilder.toString());
		assertThat(uriBuilder.toString(), is("https://services.ands.org.au/doi/1.1/update.xml/?app_id=TEST0ca474877510b101b26f9e4192bacba9ca762b15&doi=10.5072%2F01%2F4DD9C98890330&debug=false&url=http%3A%2F%2Fsomeurl.com%2Fidofrecord"));
	}
	
	@Test
	public void testGetUpdateDoiUriWoResolveUrl() {
		when(doiConfig.getBaseUri()).thenReturn("https://services.ands.org.au/doi/1.1/");
		when(doiConfig.getAppId()).thenReturn("0ca474877510b101b26f9e4192bacba9ca762b15");
		when(doiConfig.useTestPrefix()).thenReturn(true);
		when(doiConfig.isDebug()).thenReturn(false);

		UriBuilder uriBuilder = doiUriBuilder.getUpdateDoiUri("10.5072/01/4DD9C98890330", null);
		LOGGER.trace(uriBuilder.toString());
		assertThat(uriBuilder.toString(), is("https://services.ands.org.au/doi/1.1/update.xml/?app_id=TEST0ca474877510b101b26f9e4192bacba9ca762b15&doi=10.5072%2F01%2F4DD9C98890330&debug=false"));
	}
	
	@Test
	public void testGetUpdateDoiUriWSharedSecret() {
		when(doiConfig.getBaseUri()).thenReturn("https://services.ands.org.au/doi/1.1/");
		when(doiConfig.getAppId()).thenReturn("0ca474877510b101b26f9e4192bacba9ca762b15");
		when(doiConfig.getSharedSecret()).thenReturn("abc123");
		when(doiConfig.useTestPrefix()).thenReturn(false, true);
		when(doiConfig.isDebug()).thenReturn(false);
		
		UriBuilder uriBuilder;
		
		uriBuilder = doiUriBuilder.getUpdateDoiUri("10.5072/01/4DD9C98890330", "http://someurl.com/idofrecord");
		LOGGER.trace(uriBuilder.toString());
		assertThat(uriBuilder.toString(), is("https://services.ands.org.au/doi/1.1/update.xml/?app_id=0ca474877510b101b26f9e4192bacba9ca762b15&doi=10.5072%2F01%2F4DD9C98890330&debug=false&url=http%3A%2F%2Fsomeurl.com%2Fidofrecord"));
		
		uriBuilder = doiUriBuilder.getUpdateDoiUri("10.5072/01/4DD9C98890330", "http://someurl.com/idofrecord");
		LOGGER.trace(uriBuilder.toString());
		assertThat(uriBuilder.toString(), is("https://services.ands.org.au/doi/1.1/update.xml/?app_id=TEST0ca474877510b101b26f9e4192bacba9ca762b15&doi=10.5072%2F01%2F4DD9C98890330&debug=false&url=http%3A%2F%2Fsomeurl.com%2Fidofrecord"));
	}

	@Test
	public void testDeactivateDoiUri() {
		when(doiConfig.getBaseUri()).thenReturn("https://services.ands.org.au/doi/1.1/");
		when(doiConfig.getAppId()).thenReturn("0ca474877510b101b26f9e4192bacba9ca762b15");
		when(doiConfig.useTestPrefix()).thenReturn(true);
		when(doiConfig.isDebug()).thenReturn(false);
		
		UriBuilder uriBuilder = doiUriBuilder.getDeactivateDoiUri("10.5072/01/4DD9C98890330");
		LOGGER.trace(uriBuilder.toString());
		assertThat(uriBuilder.toString(), is("https://services.ands.org.au/doi/1.1/deactivate.xml/?app_id=TEST0ca474877510b101b26f9e4192bacba9ca762b15&doi=10.5072%2F01%2F4DD9C98890330&debug=false"));
	}

	@Test
	public void testActivateDoiUri() {
		when(doiConfig.getBaseUri()).thenReturn("https://services.ands.org.au/doi/1.1/");
		when(doiConfig.getAppId()).thenReturn("0ca474877510b101b26f9e4192bacba9ca762b15");
		when(doiConfig.useTestPrefix()).thenReturn(true);
		when(doiConfig.isDebug()).thenReturn(false);
		
		UriBuilder uriBuilder = doiUriBuilder.getActivateDoiUri("10.5072/01/4DD9C98890330");
		LOGGER.trace(uriBuilder.toString());
		assertThat(uriBuilder.toString(), is("https://services.ands.org.au/doi/1.1/activate.xml/?app_id=TEST0ca474877510b101b26f9e4192bacba9ca762b15&doi=10.5072%2F01%2F4DD9C98890330&debug=false"));
	}
	
	@Test
	public void testGetDoiMetadataUri() {
		when(doiConfig.getBaseUri()).thenReturn("https://services.ands.org.au/doi/1.1/");
		when(doiConfig.getAppId()).thenReturn("0ca474877510b101b26f9e4192bacba9ca762b15");
		when(doiConfig.useTestPrefix()).thenReturn(true);
		when(doiConfig.isDebug()).thenReturn(false);
		
		UriBuilder uriBuilder = doiUriBuilder.getDoiMetadataUri("10.5072/01/4DD9C98890330");
		LOGGER.trace(uriBuilder.toString());
		assertThat(uriBuilder.toString(), is("https://services.ands.org.au/doi/1.1/xml.xml/?doi=10.5072%2F01%2F4DD9C98890330&debug=false"));
	}
	
}
