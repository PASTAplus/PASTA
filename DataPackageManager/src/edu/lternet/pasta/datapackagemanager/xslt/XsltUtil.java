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
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashMap;

public class XsltUtil {

    private static final String DIR_PATH = "WebRoot/WEB-INF/conf";

    private static final Logger logger = Logger.getLogger(XsltUtil.class);

    public static String transformToText(
        String xml,
        String xsltName,
        HashMap<String, String> parameters
    ) {
        return transform(
            new ByteArrayInputStream(xml.getBytes()), xsltName, parameters, "text", "no"
        ).toString();
    }

    public static String transformToPrettyXml(
        String xml,
        String xsltName,
        HashMap<String, String> parameters
    ) {
        return transform(
            new ByteArrayInputStream(xml.getBytes()), xsltName, parameters, "xml", "yes"
        ).toString();
    }

    public static InputStream transformToPrettyXml(
        InputStream xml,
        String xsltName,
        HashMap<String, String> parameters
    ) {
        OutputStream os = transform(xml, xsltName, parameters, "xml", "yes");
        return new ByteArrayInputStream(os.toString().getBytes());
    }

    private static OutputStream transform(
        InputStream xml,
        String xsltName,
        HashMap<String, String> parameters,
        String outputMethod,
        String indent
    ) {
        OutputStream outputStream = new ByteArrayOutputStream();

        try {
            String xsltPath = getXsltPath(xsltName);
            File xsltFile = new File(xsltPath);
            StreamSource xsltSource = new StreamSource(xsltFile);
            Source source = new StreamSource(xml);

            Processor processor = new Processor(false);
            XsltCompiler xsltCompiler = processor.newXsltCompiler();
            XsltExecutable xsltExecutable = xsltCompiler.compile(xsltSource);
            XdmNode xdmNode = processor.newDocumentBuilder().build(source);
            Serializer out = processor.newSerializer();
            out.setOutputProperty(Serializer.Property.METHOD, outputMethod);
            out.setOutputProperty(Serializer.Property.INDENT, indent);
            out.setOutputProperty(Serializer.Property.ENCODING, "UTF-8");
            out.setOutputStream(outputStream);
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
        } catch (IOException | SaxonApiException e) {
            logger.error(e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("XSLT processing error: " + e.getMessage());
        }

        return outputStream;
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
