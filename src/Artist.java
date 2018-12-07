import com.wrapper.spotify.model_objects.specification.ArtistSimplified;
import com.wrapper.spotify.requests.data.artists.GetArtistRequest;

import java.util.ArrayList;

public class Artist extends Item {
    private String[] genres;
    private ArrayList<Track> tracks;
    private ArrayList<Track> topTracks;

    public Artist(ArtistSimplified artist) {
        super(artist.getName(), artist.getId(), artist.getUri());
        Shared.artists.add(this);
    }

    public Artist(com.wrapper.spotify.model_objects.specification.Artist artist) {
        super(artist.getName(), artist.getId(), artist.getUri());
        Shared.artists.add(this);
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

    public void findTopTracks(User user) {

    }

    public void findTracks(User user) {

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
