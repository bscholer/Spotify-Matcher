import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;

import java.util.ArrayList;

public class Main {

    private final static String clientId = "1f152bb92a1940b3b32cb06e18c98ceb";
    private final static String clientSecret = "579ecc79d8944827b068f4956f337ec8";

    public static void main(String[] args) {
        Shared.albums = new ArrayList<>();
        Shared.tracks = new ArrayList<>();
        Shared.artists = new ArrayList<>();
        //Authenticate, and create User object
        CliAuthDialog authDialog = new CliAuthDialog(clientId, clientSecret);
        //Create a new User object from the SpotifyApi that CliAuthDialog.run returns.
        User user = new User(authDialog.run());
        user.setUsername();
        if (user.getSpotifyApi() != null && user.getUsername() != null) {
            System.out.printf("\nWelcome, %s!\n\n", user.getUsername());
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
            System.out.println();


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


            /*
            This code finds tracks of playlists and albums, which in turn finds albums, artists, and genres for each.
            EFFICIENCY TROUBLE SPOT!!!!!!!!
             */
            Timer t = new Timer(true);
            for (Playlist playlist : playlistsToUse) {
                playlist.findTracks(user);
            }
            for (Album album : albumsToUse) {
                album.findTracks(user);
            }
            System.out.println("Track identification took " + t.end() + " milliseconds.");


            ArrayList<Track> tracksToUse = new ArrayList<>();
            for (Playlist playlist : playlistsToUse) {
                tracksToUse.addAll(playlist.getTracks());
            }
            for (Album album : albumsToUse) {
                tracksToUse.addAll(album.getTracks());
            }
            Fingerprint fingerprint = new Fingerprint(tracksToUse, user);
            Playlist rapCaviar = new Playlist("RapCaviar", "37i9dQZF1DX0XUsuxWHRQd", "spotify:user:spotify:playlist:37i9dQZF1DX0XUsuxWHRQd", 50);
            rapCaviar.findTracks(user);
            Fingerprint rapCaviarFingerprint = new Fingerprint(rapCaviar.getTracks(), user);
            for (Track track : rapCaviar.getTracks()) {
                System.out.println(track.getName());
            }
            System.out.println(rapCaviar.getTracks().size());
            System.out.println(rapCaviarFingerprint.toString());
            System.out.println(fingerprint.toString());
            user.setFingerprint(fingerprint);
            System.out.println("SCORE: " + Fingerprint.findMatch(user, rapCaviarFingerprint));
        }
    }
}
