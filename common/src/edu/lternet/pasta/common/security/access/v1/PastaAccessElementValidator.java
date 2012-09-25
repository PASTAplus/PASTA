package edu.lternet.pasta.common.security.access.v1;

import pasta.pasta_lternet_edu.access_0.AccessType;
import edu.lternet.pasta.common.PastaAccessUtility;
import edu.lternet.pasta.common.validate.Validator;
import edu.lternet.pasta.common.validate.ValidatorResultsImpl;

public class PastaAccessElementValidator implements Validator<String> {

    private final boolean canonicalizes;

    public PastaAccessElementValidator(boolean canonicalizes) {
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
            access = PastaAccessUtility.getPastaAccess_0(entity);
        }
        catch (IllegalArgumentException e) {
            String s = "The provided string could not be parsed " +
                       "as Pasta Access 0.1 <access> element.";
            return results.fatal(s);
        }

        // Validating the JAXB object
        PastaAccessTypeValidator validator = new PastaAccessTypeValidator();
        ValidatorResults<AccessType> r = validator.validate(access);

        results.setEntity(entity);
        results.setValid(r.isValid());
        results.addResults(r);

        if (canonicalizes) {
            results.setCanonicalEntity(PastaAccessUtility.toString(access));
        }

        return results;
    }


}
