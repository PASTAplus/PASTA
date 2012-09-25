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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import com.unboundid.util.Base64;

/**
 * Used to perform symmetric encryption and decryption. This class encapsulates
 * the details of private key generation and encryption/decryption of strings.
 * The encrypted strings produced by this class are Base64 encoded, therefore
 * they can be used directly in HTTP headers, for example, without further
 * modification.
 *
 */
public final class SymmetricEncrypter {

    private SymmetricEncrypter() {
        // prevents instantiation
    }

    private static final String algorithm = "AES";

    /**
     * Produces a private key that can be used by this class to encrypt and
     * decrypt strings.
     *
     * @return a private key that can be used by this class to encrypt and
     *         decrypt strings.
     */
    public static SecretKey makeKey() {
        try {
            return KeyGenerator.getInstance(algorithm).generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);  // Should never be reached
        }
    }

    /**
     * Writes the provided key to the specified file. This key can be read back
     * using {@link #readKey(File)}.
     *
     * @param key
     *            the key to be written to disk.
     * @param file
     *            the file to which the key will be written
     *
     * @throws IllegalStateException
     *             if the key cannot be written to the file.
     */
    public static void writeKey(SecretKey key, File file) {

        try {

            FileOutputStream fileOut = new FileOutputStream(file);
            ObjectOutputStream objOut = new ObjectOutputStream(fileOut);

            objOut.writeObject(key);
            objOut.close();

        }
        catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }

    }

    /**
     * Reads the key stored in the specified file. This file should have been
     * created using {@link #writeKey(SecretKey, File)}.
     *
     * @param file
     *            the file in which the key was written.
     *
     * @return the key stored in the specified file.
     *
     * @throws IllegalStateException
     *             if the key cannot be read from the specified file.
     */
    public static SecretKey readKey(File file) {

        try {

            FileInputStream fileIn = new FileInputStream(file);
            ObjectInputStream objIn = new ObjectInputStream(fileIn);

            SecretKey key = (SecretKey) objIn.readObject();
            objIn.close();

            return key;
        }
        catch (FileNotFoundException e) {
            throw new IllegalStateException(e);
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }

    }

    /**
     * Encrypts and Base64 encodes the provided string using the provided
     * private key. The key should have been obtained from {@link #makeKey()}.
     *
     * @param string
     *            the string to be encrypted.
     * @param key
     *            a key obtained from {@code makeKey()}.
     *
     * @return an encrypted and Base64 encoded version of the provided string.
     *
     * @throws IllegalArgumentException
     *             if the provided key cannot be used because it was not
     *             produced with {@code makeKey()}.
     */
    public static String encrypt(String string, SecretKey key) {

        try {

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] bytes = string.getBytes();
            byte[] encryptedBytes = cipher.doFinal(bytes);

            return Base64.encode(encryptedBytes);

        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);  // Should never be reached
        }
        catch (NoSuchPaddingException e) {
            throw new IllegalStateException(e);  // Should never be reached
        }
        catch (BadPaddingException e) {
            throw new IllegalStateException(e);  // Should never be reached
        }
        catch (IllegalBlockSizeException e) {
            throw new IllegalStateException(e);  // Should never be reached
        }
        catch (InvalidKeyException e) {
            String s = "The provided key is invalid.";
            throw new IllegalArgumentException(s, e);
        }

    }

    /**
     * Decrypts the provided encrypted string using the provided private key.
     * The provided key should be the one used to encrypt the string.
     *
     * @param encrypted
     *            the string to be decrypted.
     * @param key
     *            the key used to encrypt the provided string.
     *
     * @return the decrypted string.
     *
     * @throws IllegalArgumentException
     *             if the provided string was not encrypted by this class or
     *             with a key other than the one provided.
     */
    public static String decrypt(String encrypted, SecretKey key) {

        String badArg = "The provided string could not be decrypted. " +
                        "Ensure that it was encrypted by this class with " +
                        "the provided private key.";

        try {

            Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key);

            byte[] encryptedBytes = Base64.decode(encrypted);
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            return new String(decryptedBytes);

        }
        catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);  // Should never be reached
        }
        catch (NoSuchPaddingException e) {
            throw new IllegalStateException(e);  // Should never be reached
        }
        catch (InvalidKeyException e) {
            String s = "The provided key is invalid.";
            throw new IllegalArgumentException(s, e);
        }
        catch (BadPaddingException e) {
            throw new IllegalArgumentException(badArg, e);
        }
        catch (IllegalBlockSizeException e) {
            throw new IllegalArgumentException(badArg, e);
        }
        catch (ParseException e) {
            throw new IllegalArgumentException(badArg, e);
        }
    }

}
