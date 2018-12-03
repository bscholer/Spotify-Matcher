import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;
import com.wrapper.spotify.model_objects.specification.PlaylistTrack;
import com.wrapper.spotify.requests.data.playlists.GetPlaylistsTracksRequest;

import java.util.ArrayList;

public class Playlist extends Item {
    private String owner;
    private ArrayList<Track> tracks;
    /*
    Only to be used to retain the amount of tracks from the PlaylistSimplified to find tracks later. Either this value
    or the size of tracks will be used in getLength()
     */
    private int length;

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
        this.length = playlist.getTracks().getTotal();
    }

    /*
    This code was moved from the constructor, so that we only do API heavy things when necessary, after the user
    has picked the playlists they want to use. Having this greatly increases load times.
     */
    public void findTracks(User user) {
        this.tracks = new ArrayList<>();
        for (int i = 0; i < Math.ceil(this.length / 100.0); i++) {
            GetPlaylistsTracksRequest getPlaylistsTracksRequest = user.getSpotifyApi()
                    .getPlaylistsTracks(this.id)
                    .offset(i * 100)
                    .build();
            try {
                Paging<PlaylistTrack> playlistTracks = getPlaylistsTracksRequest.execute();
                PlaylistTrack[] playlistTracksArray = playlistTracks.getItems();
                for (int j = 0; j < playlistTracksArray.length; j++) {
                    com.wrapper.spotify.model_objects.specification.Track spotifyTrack = playlistTracksArray[j].getTrack();
                    boolean found = false;
                    for (Track track : Shared.tracks) {
                        if (track.getName().equals(spotifyTrack.getName()) && track.getUri().equals(spotifyTrack.getUri())) {
                            this.tracks.add(track);
                            found = true;
                        }
                    }
                    if (!found) {
                        tracks.add(new Track(spotifyTrack, user));
                    }
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
        int len;
        try {
            len = tracks.size();
            return len;
        } catch (NullPointerException e) {
            return this.length;
        }
    }

    @Override
    public String toString() {
        return String.format("%s by %s, with %d songs", this.name, this.owner, this.getLength());
    }
}
