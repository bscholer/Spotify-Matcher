import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsTracksRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;

import java.util.ArrayList;
import java.util.Arrays;

public class Playlist extends Item {
    private String owner;
    private ArrayList<Track> tracks;

    public Playlist() {
        super();
    }

    //TODO delete this if it doesn't need to be used
    public Playlist(com.wrapper.spotify.model_objects.specification.Playlist playlist) {
        super();
    }

    public Playlist(PlaylistSimplified playlist, User user) {
        super();
        this.name = playlist.getName();
        this.owner = playlist.getOwner().getId();
        this.id = playlist.getId();
        this.uri = playlist.getUri();
        this.tracks = new ArrayList<>();
        int total = playlist.getTracks().getTotal();
        for (int i = 0; i < Math.ceil(total / 100.0); i++) {
            GetPlaylistsTracksRequest getPlaylistsTracksRequest = user.getSpotifyApi()
                    .getPlaylistsTracks(playlist.getId())
                    .offset(i * 100)
                    .build();
            try {
                Paging<PlaylistTrack> playlistTracks = getPlaylistsTracksRequest.execute();
                PlaylistTrack[] playlistTracksArray = playlistTracks.getItems();
                for (int j = 0; j < playlistTracksArray.length; j++) {
                    Track track = new Track();
                    track.setId(playlistTracksArray[j].getTrack().getId());
                    track.setUri(playlistTracksArray[j].getTrack().getUri());
                    track.setName(playlistTracksArray[j].getTrack().getName());
                    tracks.add(track);
                    //TODO set album and artist
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getOwner() {
        return owner;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public int getLength() {
        return tracks.size();
    }

    @Override
    public String toString() {
        return String.format("%s by %s, with %d songs", this.name, this.owner, this.tracks.size());
    }
}
