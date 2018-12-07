import com.neovisionaries.i18n.CountryCode;
import com.wrapper.spotify.SpotifyApi;
import com.wrapper.spotify.SpotifyHttpManager;
import com.wrapper.spotify.enums.ModelObjectType;
import com.wrapper.spotify.model_objects.credentials.AuthorizationCodeCredentials;
import com.wrapper.spotify.model_objects.special.SearchResult;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRefreshRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeRequest;
import com.wrapper.spotify.requests.authorization.authorization_code.AuthorizationCodeUriRequest;
import com.wrapper.spotify.requests.data.artists.GetArtistsAlbumsRequest;
import com.wrapper.spotify.requests.data.library.GetCurrentUsersSavedAlbumsRequest;
import com.wrapper.spotify.requests.data.playlists.GetListOfUsersPlaylistsRequest;
import com.wrapper.spotify.requests.data.search.SearchItemRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;

import java.net.URI;
import java.util.ArrayList;

public class ApiCalls {
    static PlaylistSimplified[] getUsersPlaylists(User user) {
        GetListOfUsersPlaylistsRequest getListOfUsersPlaylistsRequest = user.getSpotifyApi()
                .getListOfUsersPlaylists(user.getUsername())
                .limit(50)
                .build();
        try {
            Paging<PlaylistSimplified> playlistSimplifiedPaging = getListOfUsersPlaylistsRequest.execute();
            return playlistSimplifiedPaging.getItems();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static com.wrapper.spotify.model_objects.specification.Album[] getUsersAlbums(User user) {
        GetCurrentUsersSavedAlbumsRequest getCurrentUsersSavedAlbumsRequest = user.getSpotifyApi()
                .getCurrentUsersSavedAlbums()
                .limit(50)
                .build();
        try {
            Paging<SavedAlbum> savedAlbumPaging = getCurrentUsersSavedAlbumsRequest.execute();
            SavedAlbum[] savedAlbums = savedAlbumPaging.getItems();
            com.wrapper.spotify.model_objects.specification.Album[] albums =
                    new com.wrapper.spotify.model_objects.specification.Album[savedAlbumPaging.getTotal()];
            for (int i = 0; i < savedAlbums.length; i++) {
                albums[i] = savedAlbums[i].getAlbum();
            }
            return albums;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static AudioFeatures getAudioFeatures(String id, User user) {
        GetAudioFeaturesForTrackRequest gafftr = user.getSpotifyApi()
                .getAudioFeaturesForTrack(id)
                .build();
        try {
            AudioFeatures features = gafftr.execute();
            return features;
        } catch (Exception e) {
            return null;
        }
    }

    static AudioFeatures[] getAudioFeatures(String[] ids, User user) {
        GetAudioFeaturesForSeveralTracksRequest gaffstr = user.getSpotifyApi()
                .getAudioFeaturesForSeveralTracks(ids)
                .build();
        try {
            AudioFeatures[] features = gaffstr.execute();
            return features;
        } catch (Exception e) {
            return null;
        }
    }

    static ArrayList<Playlist> searchForPlaylist(User user, String query) {
        SearchItemRequest searchItemRequest = user.getSpotifyApi()
                .searchItem(query, ModelObjectType.PLAYLIST.getType())
                .market(CountryCode.US)
                .limit(10)
                .build();
        try {
            SearchResult searchResult = searchItemRequest.execute();
            PlaylistSimplified[] playlistSimplifieds = searchResult.getPlaylists().getItems();
            ArrayList<Playlist> playlists = new ArrayList<>();
            for (PlaylistSimplified playlist : playlistSimplifieds) {
                playlists.add(new Playlist(playlist, user));
            }
            return playlists;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static ArrayList<Album> searchForAlbum(User user, String query) {
        SearchItemRequest searchItemRequest = user.getSpotifyApi()
                .searchItem(query, ModelObjectType.ALBUM.getType())
                .market(CountryCode.US)
                .limit(10)
                .build();
        try {
            SearchResult searchResult = searchItemRequest.execute();
            AlbumSimplified[] albumSimplifieds = searchResult.getAlbums().getItems();
            ArrayList<Album> albums = new ArrayList<>();
            for (AlbumSimplified albumSimplified : albumSimplifieds) {
                albums.add(new Album(albumSimplified));
            }
            return albums;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static ArrayList<Artist> searchForArtist(User user, String query) {
        SearchItemRequest searchItemRequest = user.getSpotifyApi()
                .searchItem(query, ModelObjectType.ARTIST.getType())
                .market(CountryCode.US)
                .limit(10)
                .build();
        try {
            SearchResult searchResult = searchItemRequest.execute();
            com.wrapper.spotify.model_objects.specification.Artist[] spotifyArtists =
                    searchResult.getArtists().getItems();
            ArrayList<Artist> artists = new ArrayList<>();
            for (com.wrapper.spotify.model_objects.specification.Artist artist : spotifyArtists) {
                artists.add(new Artist(artist));
            }
            return artists;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static ArrayList<Album> getArtistsAlbums(Artist artist, User user) {
        GetArtistsAlbumsRequest getArtistsAlbumsRequest = user.getSpotifyApi()
                .getArtistsAlbums(artist.getId())
                .limit(50)
                .build();
        try {
            AlbumSimplified[] albumSimplifieds = getArtistsAlbumsRequest.execute().getItems();
            ArrayList<Album> albums = new ArrayList<>();
            for (AlbumSimplified albumSimplified : albumSimplifieds) {
                albums.add(new Album(albumSimplified));
            }
            return albums;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    static ArrayList<Track> getArtistsTracks(Artist artist, User user) {
        return new ArrayList<>() {{
            getArtistsAlbums(artist, user).forEach(album -> addAll(album.findTracks(user)));
        }};
    }

    static SpotifyApi refreshAuthentication(String clientId, String clientSecret, String refreshToken) {
        SpotifyApi spotifyApi;
        try {
            //Create spotifyApi
            spotifyApi = new SpotifyApi.Builder()
                    .setClientId(clientId)
                    .setClientSecret(clientSecret)
                    .setRefreshToken(refreshToken)
                    .build();
            //Try to authenticate
            AuthorizationCodeRefreshRequest authRequest = spotifyApi.authorizationCodeRefresh().build();
            AuthorizationCodeCredentials authCred = authRequest.execute();
            spotifyApi.setAccessToken(authCred.getAccessToken());
            spotifyApi.setRefreshToken(authCred.getRefreshToken());
        } catch (Exception e) {
            spotifyApi = null;
        }
        return spotifyApi;
    }

    static SpotifyApi authenticateSpotify(String clientId, String clientSecret) {
        SpotifyApi spotifyApi;
        URI redirectUri = SpotifyHttpManager.makeUri("https://bscholer.github.io/spotify-redirect/index.html");
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRedirectUri(redirectUri)
                .build();
        AuthorizationCodeUriRequest authorizationCodeUriRequest = spotifyApi.authorizationCodeUri()
                .scope("playlist-read-private,user-library-read,playlist-read-collaborative,playlist-modify-public," +
                        "playlist-modify-private,user-read-private,user-follow-read")
                .show_dialog(true)
                .build();
        URI uri = authorizationCodeUriRequest.execute();

        String code = CliAuthDialog.promptForCode(uri.toString());

        AuthorizationCodeRequest authorizationCodeRequest = spotifyApi.authorizationCode(code).build();
        try {
            AuthorizationCodeCredentials authorizationCodeCredentials = authorizationCodeRequest.execute();
            spotifyApi.setAccessToken(authorizationCodeCredentials.getAccessToken());
            spotifyApi.setRefreshToken(authorizationCodeCredentials.getRefreshToken());
        } catch (Exception e) {
            spotifyApi = null;
        }
        return spotifyApi;
    }
}
