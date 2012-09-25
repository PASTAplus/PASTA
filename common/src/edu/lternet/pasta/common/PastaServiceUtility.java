/*
 *
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2010 the University of New Mexico.
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

package edu.lternet.pasta.common;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;

import pasta.pasta_lternet_edu.service_0.AccessType;
import pasta.pasta_lternet_edu.service_0.Service;
import pasta.pasta_lternet_edu.service_0.ServiceMethod;

/**
 * Used to conveniently read Pasta Service documents.
 */
public final class PastaServiceUtility {

    private PastaServiceUtility() {
        // preventing instantiation
    }

    /**
     * Parses the provided Pasta Service 0.1 string and returns a
     * corresponding JAXB object.
     *
     * @param serviceString
     *            the Pasta Access string.
     * @return a JAXB object corresponding to the provided Pasta Service
     *         string.
     *
     * @throws IllegalArgumentException
     *             with a {@linkplain JAXBException} as the cause.
     */
    public static Service getPastaService_0(String serviceString) {
        try {
            String packageName = Service.class.getPackage().getName();
            JAXBContext jc = JAXBContext.newInstance(packageName);
            Unmarshaller u = jc.createUnmarshaller();
            StringReader reader = new StringReader(serviceString);
            Service service = (Service) u.unmarshal(reader);
            return service;
        }
        catch (JAXBException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static String accessTypeToString(AccessType at) {
        try {
            QName q = new QName("","access");
            JAXBElement<AccessType> jaxb =
                    new JAXBElement<AccessType>(q, AccessType.class, at);

            StringWriter writer = new StringWriter();

            String packageName = AccessType.class.getPackage().getName();
            JAXBContext jc = JAXBContext.newInstance(packageName);
            Marshaller m = jc.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            m.marshal(jaxb, writer);

            return writer.toString();
        }
        catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    private static ServiceMethod matchSM(String serviceMethod,
                                         Service service) {

        if (service == null)
            throw new IllegalStateException("No PastaService Set");
        if (serviceMethod == null || serviceMethod.isEmpty()) {
            throw new IllegalStateException("No ServiceMethod Set");
        }

        for (ServiceMethod sm : service.getServiceMethod()) {
            if (sm.getName().equals(serviceMethod)) {
                return sm;
            }
        }
        String s = "Unable to find <pasta:service-method name=\"" +
                   serviceMethod + "\"/>";
        throw new IllegalStateException(s);
    }

    public static String getAccessTypeString(String serviceMethod,
                                             String pastaService) {
        Service service = PastaServiceUtility.getPastaService_0(pastaService);
        ServiceMethod servMeth =
                PastaServiceUtility.matchSM(serviceMethod, service);
        AccessType at = servMeth.getAccess();
        return PastaServiceUtility.accessTypeToString(at);
    }

}
