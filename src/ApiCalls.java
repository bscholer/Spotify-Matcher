import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.*;
import com.wrapper.spotify.requests.data.library.GetCurrentUsersSavedAlbumsRequest;
import com.wrapper.spotify.requests.data.playlists.GetListOfUsersPlaylistsRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;

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

    static Album[] getUsersAlbums(User user) {
        GetCurrentUsersSavedAlbumsRequest getCurrentUsersSavedAlbumsRequest = user.getSpotifyApi()
                .getCurrentUsersSavedAlbums()
                .limit(50)
                .build();
        try {
            Paging<SavedAlbum> savedAlbumPaging = getCurrentUsersSavedAlbumsRequest.execute();
            SavedAlbum[] savedAlbums = savedAlbumPaging.getItems();
            Album[] albums = new Album[savedAlbumPaging.getTotal()];
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

}
