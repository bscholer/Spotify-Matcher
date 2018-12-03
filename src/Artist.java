import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;

public class Artist extends Item {
    private String[] genres;

    public Artist(ArtistSimplified artist, User user) {
        super(artist.getName(), artist.getId(), artist.getUri());
    }

    public void findGenres(User user) {
        GetArtistRequest getArtistRequest = user.getSpotifyApi()
                .getArtist(id)
                .build();
        try {
            com.wrapper.spotify.model_objects.specification.Artist matchedArtist = getArtistRequest.execute();
            this.genres = matchedArtist.getGenres();
        } catch (Exception e) {
        }
    }

    public String[] getGenres() {
        return genres;
    }

    public void setGenres(String[] genres) {
        this.genres = genres;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
