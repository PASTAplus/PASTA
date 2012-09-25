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

import pasta.pasta_lternet_edu.access_0.AccessType;
import pasta.pasta_lternet_edu.access_0.ObjectFactory;

/**
 * Used to conveniently read Pasta Access documents.
 */
public final class PastaAccessUtility {

    private PastaAccessUtility() {
        // preventing instantiation
    }

    /**
     * Parses the provided Pasta Access 0.1 string and returns a corresponding
     * JAXB object.
     *
     * @param emlString
     *            the Pasta Access string.
     * @return a JAXB object corresponding to the provided Pasta Access string.
     *
     * @throws IllegalArgumentException
     *             with a {@linkplain JAXBException} as the cause.
     */
    public static AccessType getPastaAccess_0(String accessString) {

        try {
            String packageName = AccessType.class.getPackage().getName();
            JAXBContext jc = JAXBContext.newInstance(packageName);
            Unmarshaller u = jc.createUnmarshaller();
            StringReader reader = new StringReader(accessString);
            JAXBElement<AccessType> jaxb =
                (JAXBElement<AccessType>) u.unmarshal(reader);
            return jaxb.getValue();
        }
        catch (JAXBException e) {
          System.out.println(e.getMessage());
            throw new IllegalArgumentException(e);
        }

    }

    /**
     * Creates and returns an Pasta Access 0.1 {@code <access} element string
     * that corresponds to the provided JAXB object.
     *
     * @param accessType
     *            the JAXB object to be represented as a string.
     * @return an Pasta Access 0.1 {@code <access} element string that
     *         corresponds to the provided JAXB object.
     */
    public static String toString(AccessType accessType) {

        try {

            ObjectFactory factory = new ObjectFactory();
            JAXBElement<AccessType> jaxb = factory.createAccess(accessType);

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

}
