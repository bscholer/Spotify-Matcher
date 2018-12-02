import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.SavedAlbum;
import com.wrapper.spotify.requests.data.library.GetCurrentUsersSavedAlbumsRequest;
import com.wrapper.spotify.requests.data.playlists.GetListOfUsersPlaylistsRequest;

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
}
