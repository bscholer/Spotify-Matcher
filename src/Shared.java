import java.awt.image.AreaAveragingScaleFilter;
import java.util.ArrayList;

/**
 * The sole purpose of this class is to hold data used by multiple classes. Think of it as a 'static' class?
 */

public class Shared {
    //These lists should be checked for a certain item before calling the API.
    public static ArrayList<Artist> artists;
    public static ArrayList<Track> tracks;
    public static ArrayList<Album> albums;
}
