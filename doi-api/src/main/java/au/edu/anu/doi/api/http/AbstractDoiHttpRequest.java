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

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;

import au.edu.anu.doi.api.config.DoiConfig;

/**
 * @author Rahul Khanna
 *
 */
public abstract class AbstractDoiHttpRequest {

	protected DoiConfig doiConfig;
	
	public AbstractDoiHttpRequest(DoiConfig doiConfig) {
		this.doiConfig = doiConfig;
	}
	
	public abstract String getMethod();
	
	public abstract URI getUri();
	
	public abstract MultivaluedMap<String, String> getHeaders();
	
	public abstract Entity<Form> getEntity();

	protected UriBuilder getRootUri() {
		return UriBuilder.fromPath(doiConfig.getBaseUri());
	}
}
