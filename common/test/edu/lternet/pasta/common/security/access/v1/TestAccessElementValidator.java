package edu.lternet.pasta.common.security.access.v1;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import edu.lternet.pasta.common.FileUtility;
import edu.lternet.pasta.common.validate.Validator.ValidatorResults;

public class TestAccessElementValidator {
    
    private static String readFile(String fileNamePrefix) {

        String dir = "test/edu/lternet/pasta/common/security/access/v1"; 
        String fileName = fileNamePrefix + ".xml";
        File file = new File(dir, fileName);

        return FileUtility.fileToString(file);
    }
    
    private AccessElementValidator validator;
    private ValidatorResults<String> results;
    
    @Before
    public void init() {
        validator = new AccessElementValidator(true);
    }
    
    @Test
    public void testValidateWithValidString() {
        String acr = readFile("zero-flaws");
        results = validator.validate(acr);
        assertTrue(results.isValid());
    }
    
    @Test
    public void testValidateWithInvalidString() {
        results = validator.validate("blah");
        assertFalse(results.isValid());
    }
}
