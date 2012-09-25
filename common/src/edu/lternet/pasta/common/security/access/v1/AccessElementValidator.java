package edu.lternet.pasta.common.security.access.v1;

import edu.lternet.pasta.common.EmlUtility;
import edu.lternet.pasta.common.validate.Validator;
import edu.lternet.pasta.common.validate.ValidatorResultsImpl;
import eml.ecoinformatics_org.access_2_1.AccessType;

public class AccessElementValidator implements Validator<String> {

    private final boolean canonicalizes;

    public AccessElementValidator(boolean canonicalizes) {
        this.canonicalizes = canonicalizes;
    }

    @Override
    public boolean canonicalizes() {
        return canonicalizes;
    }

    @Override
    public ValidatorResults<String> validate(String entity) {

        ValidatorResultsImpl<String> results =
            new ValidatorResultsImpl<String>();

        AccessType access = null;

        try {
            access = EmlUtility.getAccessType2_1_0(entity);
        }
        catch (IllegalArgumentException e) {
            String s = "The provided string could not be parsed " +
                       "as an EML 2.1.0 <access> element.";
            return results.fatal(s);
        }

        // Validating the JAXB object
        AccessTypeValidator validator = new AccessTypeValidator();
        ValidatorResults<AccessType> r = validator.validate(access);

        results.setEntity(entity);
        results.setValid(r.isValid());
        results.addResults(r);

        if (canonicalizes) {
            results.setCanonicalEntity(EmlUtility.toString(access));
        }

        return results;
    }


}
