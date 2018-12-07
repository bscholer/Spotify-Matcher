import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {

    private final static String clientId = "1f152bb92a1940b3b32cb06e18c98ceb";
    private final static String clientSecret = "579ecc79d8944827b068f4956f337ec8";
    private final static String fileName = "user.ser";

    public static void main(String[] args) {
        Shared.albums = new ArrayList<>();
        Shared.tracks = new ArrayList<>();
        Shared.artists = new ArrayList<>();
        User user = User.loadUser(fileName);
        if (user == null || user.getRefreshToken() == null || user.getRefreshToken().equals("")) {
            user = new User();
            user.setSpotifyApi(ApiCalls.authenticateSpotify(clientId, clientSecret));
            user.setRefreshToken(user.getSpotifyApi().getRefreshToken());
        } else {
            user.setSpotifyApi(ApiCalls.refreshAuthentication(clientId, clientSecret, user.getRefreshToken()));
        }
        if (user.spotifyApi == null) {
            user.setSpotifyApi(ApiCalls.authenticateSpotify(clientId, clientSecret));
            user.setRefreshToken(user.getSpotifyApi().getRefreshToken());
        }
        user.setUsername();
        if (user.getSpotifyApi() != null && user.getUsername() != null) {
            System.out.printf("\nWelcome, %s!\n", user.getUsername());
        } else {
            System.out.println("Authentication failed, please restart the program.");
            System.exit(1);
        }

        //Generate the user's fingerprint
        if (user.getFingerprint() == null) {
            Fingerprint fingerprint = Fingerprint.createNewFingerprint(user);
            user.setFingerprint(fingerprint);
        } else {
            System.out.println("Using fingerprint created on " + new SimpleDateFormat("MM-dd-yyyy hh:mm a").format(user.getFingerprint().getDateCreated()) + ".");
        }

        while (true) {
            CliMenu mainMenu = new CliMenu(new ArrayList<String>() {{
                add("Search for music");
                add("Create new fingerprint");
                add("Logout");
            }}, "Main Menu", false);
            int option = mainMenu.run()[0];
            switch (option) {
                case 0:
                    System.out.print("Query: ");
                    Scanner scanner = new Scanner(System.in);
                    String query = scanner.nextLine();
                    ArrayList<Playlist> playlists = new ArrayList<>();
                    playlists.addAll(ApiCalls.searchForPlaylist(user, query));
                    //TODO add albums and artist searching
                    break;
                case 1:
                    user.setFingerprint(Fingerprint.createNewFingerprint(user));
                    break;
                case 2:
                    user.setRefreshToken(null);
                    System.out.println("Goodbye, " + user.getUsername() + "! To log back in, restart the program.");
                    System.exit(0);
                    break;
            }
            break;
        }

        Playlist rapCaviar = new Playlist("RapCaviar", "37i9dQZF1DX0XUsuxWHRQd", "spotify:user:spotify:playlist:37i9dQZF1DX0XUsuxWHRQd", 50);
        Playlist mostNecessary = new Playlist("Most Necessary", "37i9dQZF1DX2RxBh64BHjQ", "spotify:user:spotify:playlist:37i9dQZF1DX2RxBh64BHjQ", 50);
        rapCaviar.findTracks(user);
        mostNecessary.findTracks(user);
        ArrayList<Track> tracks = new ArrayList<>();
        for (Track track : rapCaviar.getTracks()) {
            tracks.add(track);
        }
        for (Track track : mostNecessary.getTracks()) {
            tracks.add(track);
        }
        Fingerprint rapCaviarFingerprint = new Fingerprint(tracks, user);

        System.out.println("SCORE: " + Fingerprint.findMatch(user, rapCaviarFingerprint));

        //Save the user to the disk.
        int status = User.saveUser(user, fileName);
        if (status == 0) {
            System.out.println("Goodbye, " + user.getUsername() + "! Thanks for using Spotify Matcher!");
        } else {
            System.out.println("Something went wrong while saving your profile.\n" +
                    "You'll need to login and regenerate your fingerprint again next time.");
        }

    }
}
