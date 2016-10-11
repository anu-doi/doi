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

import java.util.Date;

/**
 * @author Rahul Khanna
 *
 */
public interface DoiServiceEventListener {

	public enum DoiServiceEventType
	{
		SERVICE_STATUS, GET_METADATA, MINT_DOI, UPDATE_DOI, ACTIVATE_DOI, DEACTIVATE_DOI
	};
	

	public void notify(DoiServiceEvent evt);
	
	public class DoiServiceEvent {
		private Date timestamp;
		private DoiServiceEventType eventType;
		
	}
}
