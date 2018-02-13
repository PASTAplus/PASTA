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

import java.util.ArrayList;

import org.apache.log4j.Logger;

import edu.lternet.pasta.common.XmlUtility;
import edu.lternet.pasta.common.eml.ResponsibleParty;
import edu.lternet.pasta.common.eml.Title;
import edu.lternet.pasta.datapackagemanager.DataPackageRegistry;
import edu.lternet.pasta.datapackagemanager.JournalCitation;
import edu.lternet.pasta.doi.RelatedIdentifier.RelatedIdentifierType;

/**
 * @author servilla
 * @since Nov 13, 2012
 * 
 *        Manages the DataCite metadata object.
 * 
 */
public class DataCiteMetadata extends CitationMetadata {

	/*
	 * Class variables
	 */

	private static final String PUBLISHER = "Environmental Data Initiative";


	/*
	 * Instance variables
	 */

	private Logger logger = Logger.getLogger(DataCiteMetadata.class);

	private DigitalObjectIdentifier digitalObjectIdentifier = null;
	private ResourceType resourceType = null;
	private AlternateIdentifier alternateIdentifier = null;
	private String description = null;

	
	/*
	 * Constructors
	 */

	
	/*
	 * Class methods
	 */

	
	/*
	 * Instance methods
	 */
	
	
	public void setDescription(String text) {
	    this.description = text;
	}

	
	/**
	 * Set the DataCite digital object identifier for the PASTA resource.
	 * 
	 * @param doi
	 */
	public void setDigitalObjectIdentifier(DigitalObjectIdentifier doi) {
		this.digitalObjectIdentifier = doi;
	}

	
	/**
	 * Set the DataCite resource type for the PASTA resource.
	 * 
	 * @param resourceType
	 */
	public void setResourceType(ResourceType resourceType) {
		this.resourceType = resourceType;
	}

	
	/**
	 * Set the DataCite alternate identifier for the PASTA resource.
	 * 
	 * @param alternateIdentifier
	 */
	public void setAlternateIdentifier(AlternateIdentifier alternateIdentifier) {
		this.alternateIdentifier = alternateIdentifier;
	}

	
	/**
	 * Get the DataCite digital object identifier for the PASTA resource.
	 * 
	 * @return DataCite digital object identifier for the PASTA resource
	 */
	public DigitalObjectIdentifier getDigitalObjectIdentifier() {
		return this.digitalObjectIdentifier;
	}

	
	/**
	 * Get the DataCite resource type for the PASTA resource.
	 * 
	 * @return DataCite resource type for the PASTA resource
	 */
	public ResourceType getResourceType() {
		return this.resourceType;
	}

	
	/**
	 * Get the DataCite alternate identifier for the PASTA resource.
	 * 
	 * @return DataCite alternate identifier for the PASTA resource
	 */
	public AlternateIdentifier getAlternateIdentifier() {
		return this.alternateIdentifier;
	}
	
	
	
	/**
	 * Accept a list of journal citations and add them to this DataCite metadata
	 * as related identifier.
	 * 
	 * @param citations       the list of journal citation objects
	 */
	public void addJournalCitations(ArrayList<JournalCitation> citations) {
	    if (citations != null) {
	        for (JournalCitation journalCitation : citations) {
	            RelatedIdentifier relatedIdentifier = journalCitationToRelatedIdentifier(journalCitation);
	            if (relatedIdentifier != null) {
	                this.addRelatedIdentifier(relatedIdentifier);
	            }
	        }
	    }
	}
	
	
	/*
	 * Converts a journal citation to a related identifier
	 */
	private RelatedIdentifier journalCitationToRelatedIdentifier(JournalCitation journalCitation) {
	    RelatedIdentifier relatedIdentifier = null;
	    RelatedIdentifierType relatedIdentifierType = null;
	    String relatedIdentifierStr = null;
	    
	    String doi = journalCitation.getArticleDoi();
        String url = journalCitation.getArticleUrl();
	    
	    if (doi != null && !doi.isEmpty()) {
	        relatedIdentifierStr = doi;
	        relatedIdentifierType = RelatedIdentifierType.DOI;
	    }
	    else if (url != null && !url.isEmpty()) {
            relatedIdentifierStr = url;
	        relatedIdentifierType = RelatedIdentifierType.URL;
	    }
	    
	    if (relatedIdentifierType != null) {
	        relatedIdentifier = new RelatedIdentifier(relatedIdentifierStr, relatedIdentifierType);
	    }
	    
	    return relatedIdentifier;
	}

	
	/**
	 * Generate and return the DataCite metadata package as XML.
	 * 
	 * @return DataCite metadata package as XML
	 */
	public String toDataCiteXml() {

		StringBuffer sb = new StringBuffer("");

		// Pre-amble and opening tag
		sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		sb.append("<resource xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n");
		sb.append("          xmlns=\"http://datacite.org/schema/kernel-4\"\n");
		sb.append("          xsi:schemaLocation=\"http://datacite.org/schema/kernel-4 http://schema.datacite.org/meta/kernel-4.1/metadata.xsd\">\n");

		// The DOI identifier, woo-hoo
		if (this.digitalObjectIdentifier != null) {
			sb.append("    <identifier identifierType=\""
			    + this.digitalObjectIdentifier.getType() + "\">");
			sb.append(this.digitalObjectIdentifier.getIdentifier() + "</identifier>\n");
		}

		// Creators section
		sb.append("    <creators>\n");

		ArrayList<ResponsibleParty> creators = super.creators;

		if (creators != null) {
			for (ResponsibleParty creator : creators) {
			  String creatorName = creator.getCreatorName();
				sb.append("        <creator>\n");
				sb.append(String.format("            <creatorName>%s</creatorName>\n", creatorName));
				sb.append("        </creator>\n");
			}
		}

		sb.append("    </creators>\n");

		// Titles section
		sb.append("    <titles>\n");

		ArrayList<Title> titles = super.titles;

		if (titles != null) {
			for (Title title : titles) {
				sb.append("        <title>" + title.getTitle() + "</title>\n");
			}
		}

		sb.append("    </titles>\n");

		// Publisher section
		sb.append("    <publisher>" + DataCiteMetadata.PUBLISHER
		    + "</publisher>\n");

		// Publication year section
		sb.append("    <publicationYear>" + this.publicationYear
		    + "</publicationYear>\n");

		// Resource type section
		if (this.resourceType != null) {
			sb.append("    <resourceType resourceTypeGeneral=\""
			    + this.resourceType.getResourceTypeGeneral() + "\">"
			    + this.resourceType.getResourceType() + "</resourceType>\n");
		}

        sb.append("    <language>eng</language>\n");

		// Alternate identifier section
		if (this.alternateIdentifier != null) {

			String alternateIdentifier = null;
			alternateIdentifier = this.alternateIdentifier.getAlternateIdentifier();

			sb.append("    <alternateIdentifiers>\n");
			sb.append("        <alternateIdentifier alternateIdentifierType=\"");
			sb.append(this.alternateIdentifier.getAlternateIdentifierType() + "\">");
			sb.append(alternateIdentifier);
			sb.append("</alternateIdentifier>\n");
			sb.append("    </alternateIdentifiers>\n");
		}

        // Related identifiers section
        if (this.relatedIdentifiers != null && this.relatedIdentifiers.size() > 0) {
            sb.append("    <relatedIdentifiers>\n");

            for (RelatedIdentifier ri : this.relatedIdentifiers) {
                String relatedIdentifier = ri.getRelatedIdentifier();
                sb.append(String.format(
                    "        <relatedIdentifier relatedIdentifierType=\"%s\" relationType=\"%s\">",
                    ri.getRelatedIdentifierType(), 
                    ri.getRelationType()));
                sb.append(relatedIdentifier);
                sb.append("</relatedIdentifier>\n");
            }

            sb.append("    </relatedIdentifiers>\n");
        }
        
        if (this.description != null && !this.description.isEmpty()) {
            String encodedText = XmlUtility.xmlEncode(this.description);
            sb.append("    <descriptions>\n");
            sb.append("        <description xml:lang=\"en-US\" descriptionType=\"Abstract\">");
            sb.append(encodedText);
            sb.append("</description>\n");
            sb.append("    </descriptions>\n");
        }

        sb.append("</resource>\n");

		//return DataCiteMetadata.escape(xml.toString());
		String xml = sb.toString();
		return xml;
	}


	/**
	 * EZID percent decoding of reserved characters: '%', '\n', '\r', and ':'.
	 * 
	 * @param s String to decode
	 * @return Decoded string
	 */
	public static String unescape(String s) {
		
	  StringBuffer b = new StringBuffer();
	  int i;
	  while ((i = s.indexOf("%")) >= 0) {
	    b.append(s.substring(0, i));
	    b.append((char) Integer.parseInt(s.substring(i+1, i+3), 16));
	    s = s.substring(i+3);
	  }
	  b.append(s);
	  return b.toString();
	  
	}


	/**
	 * @param args
	 */
    public static void main(String[] args) {
        try {
            DOIScanner doiScanner = new DOIScanner();
            DataPackageRegistry dpr = doiScanner.getDataPackageRegistry();
            DataCiteMetadata dcm = new DataCiteMetadata();
            RelatedIdentifier ri1 = new RelatedIdentifier("http://x.y.z", RelatedIdentifierType.URL);
            RelatedIdentifier ri2 = new RelatedIdentifier("doi:blahblahblah", RelatedIdentifierType.DOI);
            dcm.addRelatedIdentifier(ri1);
            dcm.addRelatedIdentifier(ri2);

            if (dpr != null) {
                String packageId = "edi.0.3";
                ArrayList<JournalCitation> citations = dpr.listDataPackageCitations(packageId);
                dcm.addJournalCitations(citations);
            }

            String xml = dcm.toDataCiteXml();
            System.out.println(xml);
        } 
        catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

}
