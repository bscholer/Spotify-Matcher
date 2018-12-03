import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;

import java.util.ArrayList;

public class Album extends Item {
    private ArtistCollection artists;
    private ArrayList<Track> tracks;

    public Album() {
        super();
    }

    public Album(com.wrapper.spotify.model_objects.specification.Album album, User user) {
        super(album.getName(), album.getId(), album.getUri());
        //TODO set artist
        this.artists = new ArtistCollection(album.getArtists());
        this.tracks = new ArrayList<>();
        int total = album.getTracks().getTotal();
        for (int i = 0; i < Math.ceil(total / 100.0); i++) {
            GetAlbumsTracksRequest getAlbumsTracksRequest = user.getSpotifyApi()
                    .getAlbumsTracks(album.getId())
                    .offset(i * 100)
                    .build();
            try {
                Paging<TrackSimplified> albumTracks = getAlbumsTracksRequest.execute();
                TrackSimplified[] trackSimplifieds = albumTracks.getItems();
                for (int j = 0; j < trackSimplifieds.length; j++) {
                    Track track = new Track();
                    track.setName(trackSimplifieds[j].getName());
                    track.setId(trackSimplifieds[j].getId());
                    track.setUri(trackSimplifieds[j].getUri());

                    tracks.add(track);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String toString() {
        return String.format("%s by %s with %d songs", this.name, this.artists.toString(), tracks.size());
    }

    public ArtistCollection getArtists() {
        return artists;
    }

    public void setArtists(ArtistCollection artists) {
        this.artists = artists;
    }

    public ArrayList<Track> getTracks() {
        return tracks;
    }

    public void setTracks(ArrayList<Track> tracks) {
        this.tracks = tracks;
    }
}
