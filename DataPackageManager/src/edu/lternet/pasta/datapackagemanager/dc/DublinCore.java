package edu.lternet.pasta.datapackagemanager.dc;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

public class DublinCore {

	/*
	 * Class variables
	 */

	private static final Logger logger = Logger
			.getLogger(edu.lternet.pasta.datapackagemanager.dc.DublinCore.class);
	
	
	public enum EmlVersion { eml200,  eml201, eml210, eml211};

	
	public void transformMetadata(String xslDir, String metadataChildDir) {
		String emlPath = String.format("%s/%s", metadataChildDir, "Level-1-EML.xml");
		String dcPath = String.format("%s/%s", metadataChildDir, "Level-1-DC.xml");
		File levelOneEMLFile = new File(emlPath);
		if (levelOneEMLFile.exists()) {
			try {
				logger.info(String.format("Generating Dublin Core metadata for: %s", metadataChildDir));
				String emlXml = FileUtils.readFileToString(levelOneEMLFile);
				EmlVersion v = deriveEmlVersion(emlXml);
				String xslFile = deriveXslFile(v);
				String xslPath = String.format("%s/%s", xslDir, xslFile);
				HashMap<String, String> parametersMap = null;
				String dcXml = transformXML(emlXml, xslPath, parametersMap);
				File outFile = new File(dcPath);
			    FileUtils.writeStringToFile(outFile, dcXml);
			} 
			catch (IOException e) {
				logger.error("Error reading level one EML file: " + emlPath);
				e.printStackTrace();
			}
		}
	}
	
	
	private EmlVersion deriveEmlVersion(String eml) {
		EmlVersion v = EmlVersion.eml211;
		
		if (eml != null) {
			if (eml.contains("eml://ecoinformatics.org/eml-2.0.0")) {
				v = EmlVersion.eml200;
			}
			else if (eml.contains("eml://ecoinformatics.org/eml-2.0.1")) {
				v = EmlVersion.eml201;
			}
			else if (eml.contains("eml://ecoinformatics.org/eml-2.1.0")) {
				v = EmlVersion.eml210;
			}
		}
		
		return v;
	}
	
	
	private String deriveXslFile(EmlVersion v) {
		String xslFile = "";

		switch (v) {

		case eml200:
			xslFile = "eml200toDublinCore.xsl";
			break;

		case eml201:
			xslFile = "eml201toDublinCore.xsl";
			break;

		case eml210:
			xslFile = "eml210toDublinCore.xsl";
			break;

		case eml211:
			xslFile = "eml211toDublinCore.xsl";
			break;

		default:
			xslFile = "eml211toDublinCore.xsl";
			break;
		}

		return xslFile;
	}
	
	
	public String transformXML(String xml, String xslPath,
			HashMap<String, String> parameters) {
		String outputString = null;
		File styleSheet = new File(xslPath);
		StringReader stringReader = new StringReader(xml);
		StringWriter stringWriter = new StringWriter();
		StreamSource styleSource = new StreamSource(styleSheet);
		Result result = new StreamResult(stringWriter);
		Source source = new StreamSource(stringReader);

		try {
			Transformer transformer = TransformerFactory.newInstance().newTransformer(
					styleSource);
			if (parameters != null) {
				for (String parameterName : parameters.keySet()) {
					String parameterValue = parameters.get(parameterName);
					if (parameterValue != null && !parameterValue.equals("")) {
						transformer.setParameter(parameterName, parameterValue);
					}
				}
			}
			transformer.transform(source, result);
			outputString = stringWriter.toString();
		}
		catch (TransformerConfigurationException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		catch (TransformerFactoryConfigurationError e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		catch (TransformerException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return outputString;
	}


	public static void main(String[] args) {
	    final String metadataChildDir = 
				"/home/pasta/git/NIS/DataPackageManager/test/data";
	    final String xslDir = 
				"/home/pasta/git/NIS/DataPackageManager/WebRoot/xsl";

		try {
			DublinCore dublinCore = new DublinCore();
			dublinCore.transformMetadata(xslDir, metadataChildDir);
		} 
		catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}
	
}

