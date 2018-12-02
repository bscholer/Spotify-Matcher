public class Track extends Item {
    Artist artist;
    Album album;

    public Track() {
        super();
    }

    public Track(String name, String id, String uri, Artist artist, Album album) {
        super(name, id, uri);
        this.artist = artist;
        this.album = album;
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }
}
