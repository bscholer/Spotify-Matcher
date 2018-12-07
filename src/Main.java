import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

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
            System.out.println("Gathering your data...");
            PlaylistSimplified[] usersPlaylistSimps = ApiCalls.getUsersPlaylists(user);
            com.wrapper.spotify.model_objects.specification.Album[] usersSpotifyAlbums = ApiCalls.getUsersAlbums(user);
            //Prompt user to select playlists from their library
            ArrayList<Playlist> usersPlaylists = new ArrayList<>();
            ArrayList<String> usersPlaylistStrings = new ArrayList<>();
            for (PlaylistSimplified play : usersPlaylistSimps) {
                usersPlaylists.add(new Playlist(play, user));
            }
            for (Playlist playlist : usersPlaylists) {
                usersPlaylistStrings.add(playlist.toString());
            }
            CliMenu playlistSelection = new CliMenu(usersPlaylistStrings,
                    "Select the playlists from your library to use to calculate your taste", true);
            int[] selections = playlistSelection.run();
            ArrayList<Playlist> playlistsToUse = new ArrayList<>();
            for (int i : selections) {
                playlistsToUse.add(usersPlaylists.get(i));
            }

            //Prompt user to select albums from their library
            ArrayList<Album> usersAlbums = new ArrayList<>();
            ArrayList<String> usersAlbumStrings = new ArrayList<>();
            for (com.wrapper.spotify.model_objects.specification.Album album : usersSpotifyAlbums) {
                if (album == null) continue;
                Album a = new Album(album, user);
                usersAlbums.add(a);
            }
            for (Album album : usersAlbums) {
                usersAlbumStrings.add(album.toString());
            }
            CliMenu albumSelection = new CliMenu(usersAlbumStrings,
                    "Select the albums from your library to use to calculate your taste", true);
            selections = albumSelection.run();
            ArrayList<Album> albumsToUse = new ArrayList<>();
            for (int i : selections) {
                albumsToUse.add(usersAlbums.get(i));
            }

            //Combine all the tracks
            System.out.println("\nCalculating your taste. This could take a minute.");


            //This code finds tracks of playlists and albums, which in turn finds albums, artists, and genres for each.
            Timer t = new Timer(true);
            for (Playlist playlist : playlistsToUse) {
                playlist.findTracks(user);
            }
            for (Album album : albumsToUse) {
                album.findTracks(user);
            }
//            System.out.println("Track identification took " + t.end() + " milliseconds.");


            ArrayList<Track> tracksToUse = new ArrayList<>();
            for (Playlist playlist : playlistsToUse) {
                tracksToUse.addAll(playlist.getTracks());
                for(Track soundtrack : playlist.getTracks())
                {
                    soundtrack.getName();
                }
            }
            for (Album album : albumsToUse) {
                tracksToUse.addAll(album.getTracks());
                for(Track soundtrack : album.getTracks())
                {
                    soundtrack.getName();
                }
            }
            Fingerprint fingerprint = new Fingerprint(tracksToUse, user);
            user.setFingerprint(fingerprint);
        } else {
            System.out.println("Using fingerprint created on " + new SimpleDateFormat("MM-dd-yyyy hh:mm a").format(user.getFingerprint().getDateCreated()) + ".");
        }
        Playlist rapCaviar = new Playlist("RapCaviar", "37i9dQZF1DX0XUsuxWHRQd", "spotify:user:spotify:playlist:37i9dQZF1DX0XUsuxWHRQd", 50);
        Playlist mostNecessary = new Playlist("Most Necessary", "37i9dQZF1DX2RxBh64BHjQ", "spotify:user:spotify:playlist:37i9dQZF1DX2RxBh64BHjQ", 50);
        rapCaviar.findTracks(user);
        mostNecessary.findTracks(user);
        ArrayList<Track> tracksTest = new ArrayList<>();
        for (Track track : rapCaviar.getTracks()) {
            tracksTest.add(track);
        }
        for (Track track : mostNecessary.getTracks()) {
            tracksTest.add(track);
        }

            Fingerprint rapCaviarFingerprint = new Fingerprint(tracksTest, user);

        System.out.println("SCORE: " + Fingerprint.findMatch(user, rapCaviarFingerprint));


        //Serialize the user.
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(fileName);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(user);
            objectOutputStream.close();
            fileOutputStream.close();
            System.out.println("Goodbye, " + user.getUsername() + "! Thanks for using Spotify Matcher!");
        } catch (Exception e) {
            System.out.println("Something went wrong while saving your profile.\n" +
                    "You'll need to login and regenerate your fingerprint again next time.");
            e.printStackTrace();
        }
    }
}
