import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.*;
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
                objectInputStream.close();
                inputStream.close();
            } catch (Exception e) {
                user = null;
                new File(fileName).delete();
            }
        } else {
            user = null;
        }
        return user;
    }

    /**
     * Save the user's profile to the disk.
     * @param user the user to serialize
     * @param fileName the file to save object to.
     * @return 0 for success, non-zero in case of an error.
     */
    public static int saveUser(User user, String fileName) {
        //Serialize the user.
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(user);
            objectOutputStream.close();
            fileOutputStream.close();
            return 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    @Override
    public String toString() {
        return this.username;
    }
}
