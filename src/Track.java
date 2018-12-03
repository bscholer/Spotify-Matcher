public class Track extends Item {
    private ArtistCollection artists;
    private Album album;

    public Track() {
        super();
    }

    public Track(String name, String id, String uri, ArtistCollection artists, Album album) {
        super(name, id, uri);
        this.artists = artists;
        this.album = album;
    }

    public ArtistCollection getArtists() {
        return artists;
    }

    public void setArtists(ArtistCollection artists) {
        this.artists = artists;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }
}
