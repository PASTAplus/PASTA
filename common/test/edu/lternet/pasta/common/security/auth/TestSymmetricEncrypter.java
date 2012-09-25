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

package edu.lternet.pasta.common.security.auth;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.Before;
import org.junit.Test;

import static net.java.quickcheck.generator.PrimitiveGeneratorsIterables.someStrings;

public class TestSymmetricEncrypter {
    
    private String x;
    private SecretKey key;
    
    @Before
    public void init() {
        x = "Hello 123\n\r\t !@$%^&*()";
        key = SymmetricEncrypter.makeKey();
    }

    @Test
    public void testMakeKeyForUniqueness() {
        
        SecretKey previousKey = null;
        
        for (int i = 0; i < 1E4; i ++) {
            assertFalse(key.equals(previousKey));
            previousKey = key;
            key = SymmetricEncrypter.makeKey();
        }
    }
    
    @Test
    public void testWriteAndReadKey() throws IOException {
        
        File file = File.createTempFile("key", "");
        SecretKey key = SymmetricEncrypter.makeKey();
        
        SymmetricEncrypter.writeKey(key, file);
        SecretKey writtenKey = SymmetricEncrypter.readKey(file);
        
        assertEquals(key, writtenKey);
    }
    
    @Test
    public void testMultipleKeyWritesAndSingleRead() throws IOException {

        File file = File.createTempFile("key", "");
        SecretKey key = SymmetricEncrypter.makeKey();
        
        SymmetricEncrypter.writeKey(key, file);
        SymmetricEncrypter.writeKey(key, file);
        SymmetricEncrypter.writeKey(key, file);
        SymmetricEncrypter.writeKey(key, file);
        SecretKey writtenKey = SymmetricEncrypter.readKey(file);
        
        assertEquals(key, writtenKey);
    }
    
    @Test
    public void testEqualityBetweenBeforeAndAfterEncryption() {
        
        for (int i = 0; i < 1E4; i ++) {
            
            // Getting a new key each time
            key = SymmetricEncrypter.makeKey();
            String encrypted = SymmetricEncrypter.encrypt(x, key);
            String decrypted = SymmetricEncrypter.decrypt(encrypted, key);
            
            assertFalse(x.equals(encrypted));
            assertTrue(x.equals(decrypted));
        }
    }
    
    //@org.testng.annotations.Test
    @Test
    public void testEqualityBetweenBeforeAndAfterEncryptionWithRandomStrings() {
        
        key = SymmetricEncrypter.makeKey();

        for (String s : someStrings()) {
            String encrypted = SymmetricEncrypter.encrypt(s, key);
            String decrypted = SymmetricEncrypter.decrypt(encrypted, key);
            assertFalse(s.equals(encrypted));
            assertTrue(s.equals(decrypted));
        }
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testEncryptionWithBadKey() throws NoSuchAlgorithmException {
        SecretKey badKey = KeyGenerator.getInstance("DES").generateKey();
        assertFalse(key.getAlgorithm().equals(badKey.getAlgorithm()));
        SymmetricEncrypter.encrypt(x, badKey);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testDecryptionWithUnencryptedString() {
        SymmetricEncrypter.decrypt(x, key);
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testDecryptionWithWrongKey() {
        String encrypted = SymmetricEncrypter.encrypt(x, key);
        key = SymmetricEncrypter.makeKey();
        SymmetricEncrypter.decrypt(encrypted, key);
    }
}
