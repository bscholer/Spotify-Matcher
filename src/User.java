import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URI;

/**
 * The User class is aimed towards being a parent class for both Spotify and Twitter user classes.
 * This class stores things like name, email, username, and also have a general AuthenticationDetails object.
 *
 * @author The Guardians of Java
 * @since 2018-10-26
 */

public class User implements Serializable {

    protected String username;
    protected transient SpotifyApi spotifyApi;
    protected Fingerprint fingerprint;
    private String refreshToken;

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

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
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

    public static User loadUser(String fileName) {
        User user;
        File authFile = new File(fileName);
        //Auth file exists
        if (authFile.exists()) {
            try {
                FileInputStream inputStream = new FileInputStream(fileName);
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                user = (User) objectInputStream.readObject();
            } catch (Exception e) {
                user = null;
                new File(fileName).delete();
            }
        } else {
            user = null;
        }
        return user;
    }

    @Override
    public String toString() {
        return this.username;
    }
}
