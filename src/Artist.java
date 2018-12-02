import com.wrapper.spotify.model_objects.specification.ArtistSimplified;

public class Artist extends Item {

    public Artist(ArtistSimplified artist) {
        super(artist.getName(), artist.getId(), artist.getUri());
    }

    @Override
    public String toString() {
        return this.name;
    }
}
