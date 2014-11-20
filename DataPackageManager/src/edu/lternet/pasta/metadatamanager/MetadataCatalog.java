/*
 * 
 * $Date: 2012-02-28 11:02:03 -0700 (Tue, 28 Feb 2012) $ $Author: jmoss $ $Revision: 1725 $
 * 
 * Copyright 2010 the University of New Mexico.
 * 
 * This work was supported by National Science Foundation Cooperative Agreements
 * #DEB-0832652 and #DEB-0936498.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.lternet.pasta.metadatamanager;

import javax.ws.rs.core.UriInfo;

import edu.lternet.pasta.common.EmlPackageId;

/**
 * The MetadataCatalog Interface.
 * 
 * The Interface specifying the requirements of what is necessary for an
 * implementation of a MetadataCatalog.
 */
public interface MetadataCatalog
{

    public String createEmlDocument(EmlPackageId epid, String emlDocument);

    public String deleteEmlDocument(EmlPackageId epid);

    public String query(UriInfo uriInfo);

    public String updateEmlDocument(EmlPackageId epid, String emlDocument);

}
