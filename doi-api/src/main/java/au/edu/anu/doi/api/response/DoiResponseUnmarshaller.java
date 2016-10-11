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

import java.io.Reader;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Rahul Khanna
 *
 */
public class DoiResponseUnmarshaller {
	private static final Logger LOGGER = LoggerFactory.getLogger(DoiResponseUnmarshaller.class);

	private static JAXBContext doiResponseContext;

	private Unmarshaller doiResponseUnmarshaller;

	static {
		try {
			doiResponseContext = JAXBContext.newInstance(DoiResponse.class);
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

	public DoiResponseUnmarshaller() {
		setupMarshaller();
	}

	public DoiResponse unmarshal(String doc) throws JAXBException {
		return unmarshal(new StringReader(doc));
	}
	
	public DoiResponse unmarshal(Reader doc) throws JAXBException {
		DoiResponse doiResponse = (DoiResponse) doiResponseUnmarshaller.unmarshal(doc);
		LOGGER.trace("Unmarshalled to DOI Response: [{}]",  doiResponse);
		return doiResponse;
	}
	
	/**
	 * Sets up the marshallers and unmarshallers required to marshal/unmarshall requests to and responses from the DOI
	 * service.
	 */
	private void setupMarshaller() {
		try {
			doiResponseUnmarshaller = doiResponseContext.createUnmarshaller();
		} catch (JAXBException e) {
			throw new RuntimeException(e);
		}
	}

}
