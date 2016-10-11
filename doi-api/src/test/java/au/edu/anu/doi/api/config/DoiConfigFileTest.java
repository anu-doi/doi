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

package au.edu.anu.doi.api.config;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.*;

import java.io.InputStream;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rahul Khanna
 *
 */
public class DoiConfigFileTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(DoiConfigFileTest.class);
	
	private DoiConfigFile doiConfigFile;
	
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
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception{
		try (InputStream configFileStream = this.getClass().getResourceAsStream("doiconfig1.properties")) {
			doiConfigFile = new DoiConfigFile(configFileStream);
		}
		
		assertThat(doiConfigFile.getBaseUri(), is("http://baseUri/doi"));
		assertThat(doiConfigFile.getAppId(), is("appId"));
		assertThat(doiConfigFile.useTestPrefix(), is(false));
		assertThat(doiConfigFile.isDebug(), is(false));
		assertThat(doiConfigFile.getSharedSecret(), is("abc123"));
	}

}
