/**
 * The AuthenticationDetails class is designed to be inherited, storing general authentication details for
 * APIs using OAuth 2.0. The main purpose of this class is being able to be Serialized, storing the refresh key data.
 *
 * @author The Guardians of Java
 * @since 2018-10-26
 */

public class AuthenticationDetails {

    protected String token;
    protected String refreshToken;
    protected Long lastRefreshedDate;
    protected Long expiryDate;

    /**
     * @param token             The session token, used only during use.
     * @param refreshToken      The refresh token, generated during each session, which should be stored.
     * @param lastRefreshedDate The UNIX epoch describing when the key was last refreshed.
     * @param expiryDate        The UNIX epoch describing when the token will expire.
     */
    public AuthenticationDetails(String token, String refreshToken, Long lastRefreshedDate, Long expiryDate) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.lastRefreshedDate = lastRefreshedDate;
        this.expiryDate = expiryDate;
    }

    public AuthenticationDetails() {
    }

    /**
     * This function ends the current session. This is done by removing the existing token and expiryDate,
     * as well as serializing the refreshToken.
     *
     * @return 0 for successful serialization, non-0 for error during serialization.
     */
    public int endSession() {
        this.token = null;
        this.expiryDate = null;
        //TODO add saving
        return 0;
    }
}
