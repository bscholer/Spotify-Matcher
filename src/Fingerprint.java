import com.wrapper.spotify.model_objects.specification.Album;
import com.wrapper.spotify.model_objects.specification.AlbumSimplified;
import com.wrapper.spotify.model_objects.specification.PlaylistSimplified;

import java.io.Serializable;

/**
 * The fingerprint class contains data about a certain user's music taste, and will be used by the various Match classes
 * in order to generate a match.
 *
 * @author The Guardians of Java
 * @since 2018-10-26
 */

public class Fingerprint implements Serializable {
    private boolean isCreated;
    private int avgLoudness;
    private String username;

    public Fingerprint () {

    }

    public Fingerprint(Album[] albums, PlaylistSimplified[] playlists) {

    }
}
