package edu.lternet.pasta.common.security.access.v1;

import edu.lternet.pasta.common.security.access.AbstractAuthTokenAccessController;
import edu.lternet.pasta.common.security.authorization.AccessMatrix;
import edu.lternet.pasta.common.security.authorization.InvalidPermissionException;
import edu.lternet.pasta.common.security.authorization.Rule.Permission;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.validate.NullValidator;
import edu.lternet.pasta.common.validate.Validator;


public class UserAccessControllerV1 extends AbstractAuthTokenAccessController
{

    private boolean canDo(AuthToken authtoken, String acr, String resourceSubmitter, Permission action) {
        AccessMatrix matrix = makeAccessMatrix(acr);
        return matrix.isAuthorized(authtoken, resourceSubmitter, action);
    }

    private AccessMatrix makeAccessMatrix(String acr) {
        try {
            return new AccessMatrix(acr);
        }
        catch (InvalidPermissionException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    /**
     * Returns a null validator.
     * @return a null validator.
     */
    @Override @Deprecated
    public Validator<String> getAcrValidator() {
        return new NullValidator<String>();
    }

    @Override
    public boolean canRead(AuthToken authToken, String accessControlRule,
            String resourceSubmitter) {
        return canDo(authToken, accessControlRule, resourceSubmitter, Permission.read);
    }

    @Override
    public boolean canWrite(AuthToken authToken, String accessControlRule,
            String resourceSubmitter) {
        return canDo(authToken, accessControlRule, resourceSubmitter, Permission.write);
    }

    @Override
    public boolean canChangePermission(AuthToken authToken,
            String accessControlRule, String resourceSubmitter) {
        return canDo(authToken, accessControlRule, resourceSubmitter, Permission.changePermission);
    }

    @Override @Deprecated
    public boolean canAll(AuthToken authToken, String accessControlRule,
            String resourceSubmitter) {
        return false;
    }

}
