import com.wrapper.spotify.model_objects.specification.ArtistSimplified;

import java.util.ArrayList;

public class ArtistCollection {
    ArrayList<Artist> artists;

    public ArtistCollection() {

    }

    public ArtistCollection(ArrayList<Artist> artists) {
        this.artists = artists;
    }

    public ArtistCollection(ArtistSimplified[] artistSimplifieds) {
        this.artists = new ArrayList<>();
        for (ArtistSimplified artistSimplified : artistSimplifieds) {
            this.artists.add(new Artist(artistSimplified));
        }
    }

    @Override
    public String toString() {
        String ret = "";
        for (Artist artist : artists) {
            ret += artist.toString() + ", ";
        }
        return ret.substring(0, ret.length() - 2);
    }
}
