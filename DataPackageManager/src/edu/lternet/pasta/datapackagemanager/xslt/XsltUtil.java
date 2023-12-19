package edu.lternet.pasta.datapackagemanager.xslt;/*
 * Copyright 2011-2013 the University of New Mexico.
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
 */

import edu.lternet.pasta.datapackagemanager.ConfigurationListener;
import edu.ucsb.nceas.utilities.Options;
import net.sf.saxon.s9api.*;
import org.apache.log4j.Logger;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;

public class XsltUtil {

    private static final String DIR_PATH = "WebRoot/WEB-INF/conf";
    private static final String SEARCH_RESULT_XML_TO_CSV_NAME = "searchResultXmlToCsv.xslt";

    private static final Logger logger = Logger.getLogger(XsltUtil.class);

    public static String transformSearchResultXmlToCsv(String searchResultXml)
        throws IOException, ParseException {
        return transform(searchResultXml, SEARCH_RESULT_XML_TO_CSV_NAME, null, "text", "no");
    }

    public static String transformToCompactHtml(
        String xml,
        String xsltName,
        HashMap<String, String> parameters
    ) throws ParseException, IOException {
        return transform(xml, xsltName, parameters, "html", "no");
    }

    public static String transformToPrettyXml(
        String xml,
        String xsltName,
        HashMap<String, String> parameters
    ) throws ParseException, IOException {
        return transform(xml, xsltName, parameters, "xml", "yes");
    }

    private static String transform(
        String xml,
        String xsltName,
        HashMap<String, String> parameters,
        String outputMethod,
        String indent
    ) throws ParseException, IOException {
        String xsltPath = getXsltPath(xsltName);
        File xsltFile = new File(xsltPath);
        StringReader stringReader = new StringReader(xml);
        StringWriter stringWriter = new StringWriter();
        StreamSource xsltSource = new StreamSource(xsltFile);
        Source source = new StreamSource(stringReader);

        String s;
        try {
            Processor processor = new Processor(false);
            XsltCompiler xsltCompiler = processor.newXsltCompiler();
            XsltExecutable xsltExecutable = xsltCompiler.compile(xsltSource);
            XdmNode xdmNode = processor.newDocumentBuilder().build(source);
            Serializer out = processor.newSerializer();
            out.setOutputProperty(Serializer.Property.METHOD, outputMethod);
            out.setOutputProperty(Serializer.Property.INDENT, indent);
            out.setOutputProperty(Serializer.Property.ENCODING, "UTF-8");
            out.setOutputWriter(stringWriter);
            XsltTransformer xsltTransformer = xsltExecutable.load();
            xsltTransformer.setInitialContextNode(xdmNode);
            if (parameters != null) {
                for (String parameterName : parameters.keySet()) {
                    String parameterValue = parameters.get(parameterName);
                    if (parameterValue != null && !parameterValue.isEmpty()) {
                        QName qName = new QName(parameterName);
                        XdmAtomicValue xdmAtomicValue =
                            new XdmAtomicValue(parameterValue, ItemType.STRING);
                        xsltTransformer.setParameter(qName, xdmAtomicValue);
                    }
                }
            }
            xsltTransformer.setDestination(out);
            xsltTransformer.transform();
            s = stringWriter.toString();
        } catch (SaxonApiException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw new ParseException("XSLT Parse Error: " + e.getMessage(), 0);
        }

        return s;
    }

    public static String loadXslt(String xsltName) throws IOException {
        String xsltPath = getXsltPath(xsltName);
        byte[] encodedBytes = Files.readAllBytes(Paths.get(xsltPath));
        return new String(encodedBytes, StandardCharsets.UTF_8);
    }

    public static String getXsltPath(String xsltName) throws IOException {
        Options options = ConfigurationListener.getOptions();
        if (options == null) {
            ConfigurationListener configurationListener = new ConfigurationListener();
            configurationListener.initialize(DIR_PATH);
            options = ConfigurationListener.getOptions();
        }
        String xsltDir = options.getOption("datapackagemanager.xslDir");
        String xsltPath = String.format("%s/%s", xsltDir, xsltName);
        return xsltPath;
    }

}
