import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;

import java.io.*;
import java.net.URI;
import java.util.Scanner;

public class CliAuthDialog {
    //We don't really need getters and setters for these variables.
    //clientId/Secret should only be set once on instantiation.
    private String clientId;
    private String clientSecret;

    public CliAuthDialog() {}

    public CliAuthDialog(String clientId, String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
    }

    public SpotifyApi run() {
        //Authenticate
        File authFile = new File("auth");
        //Auth file exists
        SpotifyApi spotifyApi;
        if (authFile.exists()) {
            FileInputStream inputStream = null;
            try {
                //Read refresh token from file
                BufferedReader input = new BufferedReader(new FileReader(authFile));
                String refresh;
                while ((refresh = input.readLine()) != null) {

                }
                input.close();
                //Create spotifyApi
                spotifyApi = new SpotifyApi.Builder()
                        .setClientId(clientId)
                        .setClientSecret(clientSecret)
                        .setRefreshToken(refresh)
                        .build();
                //Try to authenticate
                AuthorizationCodeRefreshRequest authRequest = spotifyApi.authorizationCodeRefresh().build();
                AuthorizationCodeCredentials authCred = authRequest.execute();
                spotifyApi.setAccessToken(authCred.getAccessToken());
                spotifyApi.setRefreshToken(authCred.getRefreshToken());
                //Empty contents of file
                PrintWriter writer = new PrintWriter("auth");
                writer.print("");
                writer.close();
                //Write new refresh token to file.
                FileOutputStream outputStream = new FileOutputStream("auth");
                outputStream.write(refresh.getBytes());
                outputStream.close();
                return spotifyApi;
            } catch (Exception e) {
                System.out.println("Authentication failed, please run program again.");
                new File("auth").delete();
                e.printStackTrace();
            }
        } else {
            URI redirectUri = SpotifyHttpManager.makeUri("https://bscholer.github.io/spotify-redirect/index.html");
            spotifyApi = new SpotifyApi.Builder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRedirectUri(redirectUri)
                    .build();
            String refresh = authenticateSpotify(spotifyApi);
            FileOutputStream outputStream = null;
            try {
                PrintWriter writer = new PrintWriter("auth");
                writer.print("");
                writer.close();
                outputStream = new FileOutputStream("auth");
                outputStream.write(refresh.getBytes());
                outputStream.close();
                return spotifyApi;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private static String authenticateSpotify(SpotifyApi spotifyApi) {

        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope("playlist-read-private,user-library-read,playlist-read-collaborative,playlist-modify-public," +
                        "playlist-modify-private,user-read-private,user-follow-read")
                .show_dialog(true)
                .build();
        URI uri = authorizationCodeUriRequest.execute();

        //User prompt stuff
        System.out.println("Please follow this link, and then copy and paste the code below.");
        System.out.println(uri.toString());
        Scanner scanner = new Scanner(System.in);
        System.out.print("Code: ");
        String code = scanner.nextLine();

        AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
        try {
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
            return authorizationCodeCredentials.getRefreshToken();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
