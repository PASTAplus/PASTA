/*
 * $Date$
 * $Author$
 * $Revision$
 *
 * Copyright 2010 the University of New Mexico.
 *
 * This work was supported by National Science Foundation Cooperative Agreements
 * #DEB-0832652 and #DEB-0936498.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package edu.lternet.pasta.common.proxy;

import javax.ws.rs.core.Response;

/**
 * Used to indicate that a proxy web service did not receive the response it
 * expected from the terminal server or web service to which it made a request.
 *
 * <p>
 * There are two basic reasons (causes) why instances of this class would be
 * thrown: 1) a request to a terminal server was attempted, but a Java exception
 * was thrown by the client class that is making the request, and therefore a
 * response from the terminal server was never received; and 2) a request was
 * successfully made to a terminal server, and a response was successfully
 * received, but the content of the response was unexpected, such as status code
 * 404 instead of 200.
 * </p>
 * <p>
 * The class that throws this type of exception is usually also the class that
 * best understands what was expected and how this error can be best described
 * to the originating client. Therefore, instances of this class also contain
 * responses that a proxy web service can send back to the originating client.
 * This produces a tight coupling between the cause and the effect, and
 * eliminates the need of catchers of these exceptions to construct their own
 * response describing the error, although they still can.
 * </p>
 *
 *
 */
public class JerseyProxyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final BufferedClientResponse<?> cause;
    private final Response effect;

    /**
     * Constructs a new proxy exception from the provided unexpected response of
     * the terminal server and the suggested response of the proxy to the
     * originating client.
     *
     * @param cause
     *            the unexpected response of the terminal server.
     *
     * @param effect
     *            a response that the proxy can make to the originating client
     *            describing the failure that occurred.
     */
    public JerseyProxyException(BufferedClientResponse<?> cause,
                                Response effect) {
        super();
        this.cause = cause;
        this.effect = effect;
    }

    /**
     * Constructs a new proxy exception from the provided exception
     * and the suggested response of the proxy to the originating client.
     *
     * @param cause
     *            an exception thrown during the request from the proxy to the
     *            terminal server.
     *
     * @param effect
     *            a response that the proxy can make to the originating client
     *            describing the failure that occurred.
     */
    public JerseyProxyException(Throwable cause, Response effect) {
        super(cause);
        this.cause = null;
        this.effect = effect;
    }

    /**
     * Indicates if a response from the terminal server was received.
     *
     * @return {@code true} if a response was received, {@code false} otherwise.
     */
    public boolean serverResponded() {
        return cause != null;
    }

    /**
     * Returns the response from the terminal server, if one was received.
     *
     * @return the response from the terminal server, or {@code null} if one was
     *         not received.
     */
    public BufferedClientResponse<?> getResponseFromServer() {
        return cause;
    }

    /**
     * Returns the exception thrown by the client code, if one was thrown.
     *
     * @return the exception thrown by the client code, or {@code null} if one
     *         was not thrown.
     */
    @Override
    public Throwable getCause() {
        return super.getCause();
    }

    /**
     * Returns a response that the proxy web service can send to the original
     * requesting client, describing the failure that occurred.
     *
     * @return a response that the proxy web service can send to the original
     * requesting client, describing the failure that occurred.
     */
    public Response getResponseToClient() {
        return effect;
    }

    /**
     * Returns a description of this exception.
     * @return a description of this exception.
     */
    @Override
    public String getMessage() {

        if (!serverResponded()) {
            return super.getMessage();
        }

        return cause.getStatus() + " " +
               cause.getClientResponseStatus() + ": " +
               cause.getEntity().toString();
    }
}
