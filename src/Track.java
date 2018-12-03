import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.model_objects.specification.TrackSimplified;

public class Track extends Item {
    private ArtistCollection artists;
    private Album album;
    private AudioFeatures audioFeatures;

    public Track() {
        super();
        Shared.tracks.add(this);
    }

    public Track(String name, String id, String uri, ArtistCollection artists, Album album) {
        super(name, id, uri);
        this.artists = artists;
        this.album = album;
        Shared.tracks.add(this);
    }

    public Track(com.wrapper.spotify.model_objects.specification.Track track, User user) {
        this.id = track.getId();
        this.uri = track.getUri();
        this.name = track.getName();
        this.artists = new ArtistCollection(track.getArtists(), user);
        this.album = new Album(track.getAlbum());
        Shared.tracks.add(this);
    }

    /**
     * Album is not set with this constructor.
     * @param track
     * @param user
     */
    public Track(TrackSimplified track, User user) {
        this.id = track.getId();
        this.uri = track.getUri();
        this.name = track.getName();
        this.artists = new ArtistCollection(track.getArtists(), user);
        Shared.tracks.add(this);
    }

    public ArtistCollection getArtists() {
        return artists;
    }

    public void setArtists(ArtistCollection artists) {
        this.artists = artists;
    }

    public AudioFeatures getAudioFeatures() {
        return audioFeatures;
    }

    public void setAudioFeatures(AudioFeatures audioFeatures) {
        this.audioFeatures = audioFeatures;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }
}
