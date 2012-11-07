/*
 *
 * Copyright 2011, 2012, 2013 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative
 * Agreements #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 */

package edu.lternet.pasta.doi;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.lang.Class;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import eml.ecoinformatics_org.eml_2_1.Eml;
import eml.ecoinformatics_org.party_2_1.ResponsibleParty;
import eml.ecoinformatics_org.party_2_1.Person;

import edu.lternet.pasta.common.EmlUtility;
import edu.lternet.pasta.datapackagemanager.DataPackageManager;

import org.apache.log4j.Logger;

/**
 * @author servilla
 * @since Nov 6, 2012
 * 
 *        Provides a plain old Java object (POJO) interface to content from an
 *        EML 2.1.0 XML document.
 */
public class EmlObject {

	/*
	 * Class variables
	 */

	/*
	 * Instance variables
	 */
	
	private Logger logger = Logger.getLogger(EmlObject.class);
	
	private String emlString = null;
	private Eml eml = null;

	/*
	 * Constructors
	 */
	
	/**
	 * Create the EML 2.1.0 POJO from an EML file.
	 * 
	 * @param emlFile
	 */
	public EmlObject(File emlFile) {
		
		this.emlString = EmlUtility.getEmlDoc(emlFile);
		this.eml = EmlUtility.getEml2_1_0(emlString);
		
	}

	/*
	 * Class methods
	 */

	/*
	 * Instance methods
	 */
	
	public ArrayList<Creator> getCreators() {

		ArrayList<Creator> creators = new ArrayList<Creator>();

		List<ResponsibleParty> responsibleParties = null;
		ResponsibleParty responsibleParty = null;
		List<JAXBElement<?>> names = null;
		JAXBElement<?> name = null;

		responsibleParties = eml.getDataset().getCreator();
		Iterator<?> rpItr = responsibleParties.iterator();

		while (rpItr.hasNext()) {

			responsibleParty = (ResponsibleParty) rpItr.next();
			names = responsibleParty
			    .getIndividualNameOrOrganizationNameOrPositionName();
			Iterator<?> nItr = names.iterator();

			String nameType = null;
			Person person = null;
			Creator creator = null;

			while (nItr.hasNext()) {
				name = (JAXBElement<?>) nItr.next();
				nameType = name.getName().getLocalPart();

				if (nameType.equals(Creator.PERSON)) {
					
					creator = new Creator(Creator.PERSON);
					person = (Person) name.getValue();
					creator.setSurName(person.getSurName().trim());
					
					List<String> givenNames = person.getGivenName();
					Iterator<String> gnItr = givenNames.iterator();
					StringBuffer givenName = new StringBuffer("");
					
					while (gnItr.hasNext()) {
						String namePart = (String) gnItr.next();
						givenName.append(namePart + " ");
					}
					
					creator.setGivenName(givenName.toString().trim());
					
					creators.add(creator);
					
				} else if (nameType.equals(Creator.ORGANIZATION)) {
					
					creator = new Creator(Creator.ORGANIZATION);
					String organizationName = (String) name.getValue();
					creator.setOrganizationName(organizationName.trim()); 
					
					creators.add(creator);
					
				} else { // Creator.POSITION
					
					try {
						creator = new Creator(Creator.POSITION);

						String positionName = (String) name.getValue();
						creator.setPositionName(positionName.trim()); 
						
						creators.add(creator);
						
					} catch (IllegalStateException e) {
						logger.error(e);
					}
					
				}

			}

		}
		
		return creators;
		
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		File emlFile = new File("/Users/servilla/tmp/NIN/knb-lter-xyz.1.1.xml");

		EmlObject emlObj = new EmlObject(emlFile);
		
		ArrayList<Creator> creators = emlObj.getCreators();
		
		for (Creator creator: creators) {
			System.out.println(creator.getCreatorName());
		}

	}

}
