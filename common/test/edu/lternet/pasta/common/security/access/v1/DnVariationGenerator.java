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

package edu.lternet.pasta.common.security.access.v1;

import com.unboundid.ldap.sdk.DN;
import com.unboundid.ldap.sdk.LDAPException;
import com.unboundid.ldap.sdk.RDN;

import net.java.quickcheck.Generator;
import net.java.quickcheck.generator.PrimitiveGenerators;

/**
 * Generates random (but equivalent) representations of distinguished names.
 */
public class DnVariationGenerator implements Generator<String> {

    private final DN dn;
    private final Generator<Boolean> booleans;
    private final Generator<String> whitespace;

    /**
     * Constructs a new random distinguished name generator.
     * 
     * @param distinguishedName
     *            the DN to be randomized.
     */
    public DnVariationGenerator(String distinguishedName) {
        try {
            dn = new DN(distinguishedName);
        } catch (LDAPException e) {
            throw new IllegalArgumentException(e);
        }
        booleans = PrimitiveGenerators.booleans();
        whitespace = PrimitiveGenerators.strings(" ", 0, 1);
    }
    
    @Override
    public String next() {
        
        StringBuilder sb = new StringBuilder();

        RDN[] rdns = dn.getRDNs();
        
        for (int i = 0; i < rdns.length; i ++) {
            
            String[] names = rdns[i].getAttributeNames();
            String[] values = rdns[i].getAttributeValues();
            
            // Assuming one and only one name=value pair exists,
            // which isn't necessarily true
            append(sb, names[0]);
            append(sb, "=");
            append(sb, values[0]);
                
            if (i < (rdns.length - 1)) {
                append(sb, ",");
            }
        }
        
        return sb.toString();
    }

    private void append(StringBuilder sb, String s) {
        sb.append(whitespace.next());
        sb.append(booleans.next() ? s.toUpperCase() : s.toLowerCase());
        sb.append(whitespace.next());
    }
    
    public static void main(String[] args) throws LDAPException {
        
        String dn = "uid=pasta,o=lter,dc=ecoinformatics,dc=org";
        
        DnVariationGenerator gen = new DnVariationGenerator(dn);
        
        for (int i = 0; i < 100; i ++) {
            String next = gen.next();
            System.out.println(next);
            //System.out.println(new DN(next).toNormalizedString());
        }
    }
}
