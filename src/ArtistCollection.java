import com.wrapper.spotify.model_objects.specification.ArtistSimplified;

import java.util.ArrayList;

public class ArtistCollection {
    private ArrayList<Artist> artists;

    public ArtistCollection() {

    }

    public ArtistCollection(ArrayList<Artist> artists) {
        this.artists = artists;
    }

    public ArtistCollection(ArtistSimplified[] artistSimplifieds, User user) {
        this.artists = new ArrayList<>();
        for (ArtistSimplified artistSimplified : artistSimplifieds) {
            boolean found = false;
            try {
                for (Artist artist : Shared.artists) {
                    if (artist.getName().equals(artistSimplified.getName()) && artist.getUri().equals(artistSimplified.getUri())) {
                        this.artists.add(artist);
                        found = true;
                    }
                }
            } catch (NullPointerException e) {}
            if (!found) {
                this.artists.add(new Artist(artistSimplified));
            }
        }
    }

    public ArrayList<Artist> getArtists() {
        return artists;
    }

    public void setArtists(ArrayList<Artist> artists) {
        this.artists = artists;
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
