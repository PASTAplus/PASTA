package edu.lternet.pasta.common.security.access.v1;

import javax.ws.rs.core.HttpHeaders;

import edu.lternet.pasta.common.security.access.JaxRsHttpAccessController;
import edu.lternet.pasta.common.security.authorization.AccessMatrix;
import edu.lternet.pasta.common.security.authorization.InvalidPermissionException;
import edu.lternet.pasta.common.security.authorization.Rule.Permission;
import edu.lternet.pasta.common.security.token.AuthToken;
import edu.lternet.pasta.common.security.token.AuthTokenFactory;
import edu.lternet.pasta.common.validate.Validator;


public class PastaServiceAccessControllerV1 implements JaxRsHttpAccessController
{

    private boolean canDo(HttpHeaders requestHeaders,String acr,
                          String resourceSubmitter, Permission action) {
        AuthToken token =
                AuthTokenFactory.makeAuthToken(requestHeaders.getCookies());
        AccessMatrix matrix = makeAccessMatrix(acr);
        return matrix.isAuthorized(token, resourceSubmitter, action);
    }

    private AccessMatrix makeAccessMatrix(String acr) {
        try {
            return new AccessMatrix(acr);
        }
        catch (InvalidPermissionException e) {
            throw new IllegalStateException(e.getMessage());
        }
    }

    @Deprecated
    public Validator<String> getAcrValidator() {
        return null;
    }

    public boolean canRead(HttpHeaders requestHeaders, String accessControlRule,
            String resourceSubmitter) {

        return canDo(requestHeaders, accessControlRule, resourceSubmitter, Permission.read);

    }

    public boolean canWrite(HttpHeaders requestHeaders, String accessControlRule,
            String resourceSubmitter) {
        return canDo(requestHeaders, accessControlRule, resourceSubmitter, Permission.write);
    }

    public boolean canChangePermission(HttpHeaders requestHeaders,
            String accessControlRule, String resourceSubmitter) {

        return canDo(requestHeaders, accessControlRule, resourceSubmitter, Permission.changePermission);
    }

    @Deprecated
    public boolean canAll(HttpHeaders requestHeaders, String accessControlRule,
            String resourceSubmitter) {
        return false;
    }

}
