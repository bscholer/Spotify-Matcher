import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

/**
 * The User class is aimed towards being a parent class for both Spotify and Twitter user classes.
 * This class stores things like name, email, username, and also have a general AuthenticationDetails object.
 *
 * @author The Guardians of Java
 * @since 2018-10-26
 */

public class User {

    protected String username;
    //    protected String firstName;
//    protected String lastName;
//    protected String email;
    protected SpotifyApi spotifyApi;
    protected Fingerprint fingerprint;

    public User() {
    }

    public User(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public User(Fingerprint fingerprint) {
        this.fingerprint = fingerprint;
    }

    public User(SpotifyApi spotifyApi, Fingerprint fingerprint) {
        this.spotifyApi = spotifyApi;
        this.fingerprint = fingerprint;
    }

    public String getUsername() {
        return username;
    }

    public SpotifyApi getSpotifyApi() {
        return spotifyApi;
    }

    public void setUsername() {
        GetCurrentUsersProfileRequest getCurrentUsersProfileRequest = spotifyApi.getCurrentUsersProfile().build();
        try {
            com.wrapper.spotify.model_objects.specification.User user = getCurrentUsersProfileRequest.execute();
            this.username = user.getId();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSpotifyApi(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public void setFingerprint(Fingerprint fingerprint) {
        this.fingerprint = fingerprint;
    }

    public Fingerprint getFingerprint() {
        return fingerprint;
    }
}
