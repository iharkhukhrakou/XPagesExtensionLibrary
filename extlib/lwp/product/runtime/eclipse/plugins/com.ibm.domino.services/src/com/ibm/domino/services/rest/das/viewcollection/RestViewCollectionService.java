/*
 * � Copyright IBM Corp. 2011
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or 
 * implied. See the License for the specific language governing 
 * permissions and limitations under the License.
 */

package com.ibm.domino.services.rest.das.viewcollection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ibm.domino.services.ServiceException;
import com.ibm.domino.services.rest.das.RestDominoService;

public abstract class RestViewCollectionService extends RestDominoService {

	private ViewCollectionParameters parameters;
	
	public RestViewCollectionService(HttpServletRequest httpRequest,
			HttpServletResponse httpResponse, ViewCollectionParameters parameters) {
		super(httpRequest, httpResponse);
		this.parameters = wrapViewCollectionParameters(parameters);
	}

	protected ViewCollectionParameters wrapViewCollectionParameters(ViewCollectionParameters parameters) {
		return parameters;
	}

	@Override
	public abstract void renderService() throws ServiceException;

	@Override
	public ViewCollectionParameters getParameters() {
		return parameters;
	}

}

