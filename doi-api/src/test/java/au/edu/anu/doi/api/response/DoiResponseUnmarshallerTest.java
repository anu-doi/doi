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

package au.edu.anu.doi.api.response;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import au.edu.anu.doi.api.response.DoiResponse;
import au.edu.anu.doi.api.response.DoiResponseUnmarshaller;

/**
 * @author Rahul Khanna
 *
 */
public class DoiResponseUnmarshallerTest {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(DoiResponseUnmarshallerTest.class);

	private DoiResponseUnmarshaller doiRespUnmarshaller;
	
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
		doiRespUnmarshaller = new DoiResponseUnmarshaller();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testStatusResponse() throws Exception {
		Reader doc = getResourceFile("doistatusresp1.xml");
		DoiResponse doiResp = doiRespUnmarshaller.unmarshal(doc);
		assertThat(doiResp.getCode(), is("MT090"));
		assertThat(doiResp.getType(), is("success"));
		assertThat(doiResp.getMessage(), is("The rocket is ready to blast off -- all systems are go!"));
	}

	private InputStreamReader getResourceFile(String filename) {
		return new InputStreamReader(this.getClass().getResourceAsStream(filename), StandardCharsets.UTF_8);
	}

}
