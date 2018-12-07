import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.Paging;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;
import com.wrapper.spotify.requests.data.albums.GetAlbumsTracksRequest;

import java.util.ArrayList;

public class Album extends Item {
    private ArtistCollection artists;
    private ArrayList<Track> tracks;
    /*
    Only to be used to retain the amount of tracks from the wrapper.spotify.Album to find tracks later.
    Either this value or the size of tracks will be used in getLength()
     */
    private int length;

    public Album() {
        super();
        Shared.albums.add(this);
    }

    public Album(com.wrapper.spotify.model_objects.specification.Album album, User user) {
        super(album.getName(), album.getId(), album.getUri());
        this.artists = new ArtistCollection(album.getArtists(), user);
        this.tracks = new ArrayList<>();
        this.length = album.getTracks().getTotal();
        Shared.albums.add(this);
    }

    /*
    We don't really need to worry about grabbing artists and tracks if it's created with an AlbumSimplified.

    This constructor will only be used by Track in practice, in which case the album doesn't matter enough
    to justify spending time pulling each and every one from the API.
     */
    public Album(AlbumSimplified album) {
        super(album.getName(), album.getId(), album.getUri());
        Shared.albums.add(this);
    }

    /**
     * This code was moved from the constructor, so that we only do API-heavy things when necessary, after the user
     * has picked the albums they want to use. Having this greatly decreases load times.
     *
     * @param user
     */
    public ArrayList<Track> findTracks(User user) {
        this.tracks = new ArrayList<>();
        for (int i = 0; i < Math.ceil(this.length / 100.0); i++) {
            GetAlbumsTracksRequest getAlbumsTracksRequest = user.getSpotifyApi()
                    .getAlbumsTracks(this.id)
                    .offset(i * 100)
                    .build();
            try {
                Paging<TrackSimplified> albumTracks = getAlbumsTracksRequest.execute();
                TrackSimplified[] trackSimplifieds = albumTracks.getItems();
                for (int j = 0; j < trackSimplifieds.length; j++) {
                    com.wrapper.spotify.model_objects.specification.TrackSimplified spotifyTrack = trackSimplifieds[j];
                    boolean found = false;
                    for (Track track : Shared.tracks) {
                        if (track.getName().equals(spotifyTrack.getName()) && track.getUri().equals(spotifyTrack.getUri())) {
                            this.tracks.add(track);
                            found = true;
                            System.out.println("Found duplicate!");
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
        return this.tracks;
    }

    @Override
    public String toString() {
        return String.format("%s by %s", this.name, this.artists.toString());
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

    public int getLength() {
        int len;
        try {
            len = tracks.size();
            return len;
        } catch (NullPointerException e) {
            return this.length;
        }
    }
}
