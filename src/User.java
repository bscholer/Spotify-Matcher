/**
 * The User class is aimed towards being a parent class for both Spotify and Twitter user classes.
 * This class stores things like name, email, username, and also have a general AuthenticationDetails object.
 *
 * @author The Guardians of Java
 * @since 2018-10-26
 */

public class User {

    protected String username;
    protected String firstName;
    protected String lastName;
    protected String email;
    protected AuthenticationDetails authenticationDetails;
    protected Fingerprint fingerprint;
}
