import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.net.URI;
import java.util.Scanner;

public class CliAuthDialog {

    public CliAuthDialog() {
    }

    /**
     * This function will prompt the user to authenticate.
     * @param link the authentication link the user should use.
     * @return the authentication code from the user.
     */
    public static String promptForCode(String link) {
        //User prompt stuff
        System.out.println("Please follow this link, and then copy and paste the code below.");
        System.out.println(link);
        Scanner scanner = new Scanner(System.in);
        System.out.print("Code: ");
        String code = scanner.nextLine();
        return code;
    }
//
//    public void run(User user) {
//        //Authenticate
//        File authFile = new File(fileName);
//        //Auth file exists
//        SpotifyApi spotifyApi;
//        if (authFile.exists()) {
//            try {
//                FileInputStream inputStream = new FileInputStream(fileName);
//                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
//                user = (User) objectInputStream.readObject();
//                if (user == null || user.getRefreshToken() == null || user.getRefreshToken().equals("")) {
//                    //TODO Do stuff here if there isn't a user
//                }
//                spotifyApi = ApiCalls.refreshAuthentication(clientId, clientSecret, user.getRefreshToken());
//                if (spotifyApi == null) {
//                    System.out.println("Reauthentication failed, please run program again.");
//                }
//                user.setRefreshToken(authCred.getRefreshToken());
//                user.setSpotifyApi(spotifyApi);
//            } catch (Exception e) {
//                System.out.println("Authentication failed, please run program again.");
//                new File(fileName).delete();
//                e.printStackTrace();
//            }
//        } else {
//            URI redirectUri = SpotifyHttpManager.makeUri("https://bscholer.github.io/spotify-redirect/index.html");
//            spotifyApi = new SpotifyApi.Builder()
//                    .setClientId(clientId)
//                    .setClientSecret(clientSecret)
//                    .setRedirectUri(redirectUri)
//                    .build();
//            String refresh = authenticateSpotify(spotifyApi);
//            user.setRefreshToken(refresh);
//            user.setSpotifyApi(spotifyApi);
//        }
//    }
}