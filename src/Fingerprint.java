import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.requests.data.artists.GetSeveralArtistsRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;

import java.io.Serializable;
import java.util.*;

/**
 * The fingerprint class contains data about a certain user's music taste, and will be used by the various Match classes
 * in order to generate a match.
 *
 * @author The Guardians of Java
 * @since 2018-10-26
 */

public class Fingerprint implements Serializable {
    public final transient double TIER_1_PERCENT = 0.15;
    public final transient double TIER_2_PERCENT = 0.30;
    public final transient double TIER_3_PERCENT = 1 - TIER_1_PERCENT - TIER_2_PERCENT;
    public final transient int TIER_1_DEDUCTION = 40;
    public final transient int TIER_2_DEDUCTION = 20;
    public final transient int TIER_3_DEDUCTION = 5;
    public final transient double DANCEABILITY_WEIGHT = 1 / 6.0;
    public final transient double ENERGY_WEIGHT = 1 / 6.0;
    public final transient double SPEECHINESS_WEIGHT = 1 / 6.0;
    public final transient double ACOUSTICNESS_WEIGHT = 1 / 6.0;
    public final transient double LIVELINESS_WEIGHT = 1 / 6.0;
    public final transient double TEMPO_WEIGHT = 1 / 6.0;
    public final transient double AUDIO_FEATURES_WEIGHT = 0.7;
    public final transient double GENRE_WEIGHT = 1 - AUDIO_FEATURES_WEIGHT;

    private double danceabilityAvg;
    private double danceabilityStDev;
    private double energyAvg;
    private double energyStDev;
    private double speechinessAvg;
    private double speechinessStDev;
    private double acousticnessAvg;
    private double acousticnessStDev;
    private double livelinessAvg;
    private double livelinessStDev;
    private double tempoAvg;
    private double tempoStDev;
    private Date dateCreated;
    private HashMap<String, Integer> genres;
    private ArrayList<String> tier1Genres;
    private ArrayList<String> tier2Genres;
    private ArrayList<String> tier3Genres;

    public Fingerprint() {
    }

    public Fingerprint(ArrayList<Track> tracks, User user) {
        dateCreated = Calendar.getInstance().getTime();
        tier1Genres = new ArrayList<>();
        tier2Genres = new ArrayList<>();
        tier3Genres = new ArrayList<>();
        genres = new HashMap<>();
        ArrayList<Artist> artists = new ArrayList<>();
        for (Track track : tracks) {
            ArtistCollection collection = track.getArtists();
            artists.addAll(collection.getArtists());
        }
        Timer t = new Timer(true);
        //Hopefully a more efficient way of getting genres.
        for (int i = 0; i <= Shared.artists.size() / 50; i++) {
            String ids[] = new String[Shared.artists.size() % 50];
            for (int j = 0; j < ids.length; j++) {
                ids[j] = Shared.artists.get(j + (i * 50)).getId();
            }
            GetSeveralArtistsRequest getSeveralArtistsRequest = user.getSpotifyApi()
                    .getSeveralArtists(ids)
                    .build();
            try {
                com.wrapper.spotify.model_objects.specification.Artist[] fullArtists = getSeveralArtistsRequest.execute();
                for (int j = i * 50; j < ids.length + (i * 50); j++) {
                    for (com.wrapper.spotify.model_objects.specification.Artist artist : fullArtists) {
                        if (artist == null || Shared.artists.get(j) == null) continue;
                        if (Shared.artists.get(j).getUri().equals(artist.getUri())) {
                            Shared.artists.get(j).setGenres(artist.getGenres());
                        }
                    }
                }
            } catch (TooManyRequestsException e) {
                try {
                    i--;
                    Thread.sleep(1000 * e.getRetryAfter() + 100);
                } catch (Exception ex) {
                }
            } catch (Exception e) {
            }
        }
        for (Artist artist : Shared.artists) {
            String[] genresArr = artist.getGenres();
            if (genresArr == null || genresArr.length == 0) continue;
            for (String genre : genresArr) {
                if (this.genres.containsKey(genre)) {
                    this.genres.put(genre, this.genres.get(genre) + 1);
                } else {
                    this.genres.put(genre, 1);
                }
            }
        }
        this.genres.entrySet().stream()
                .sorted((k1, k2) -> -k1.getValue().compareTo(k2.getValue()))
                .forEach(k -> {
                    int size = this.genres.size();
                    if (size * TIER_1_PERCENT > tier1Genres.size()) {
                        tier1Genres.add(k.getKey());
                    } else if (size * TIER_2_PERCENT > tier2Genres.size()) {
                        tier2Genres.add(k.getKey());
                    } else if (size * TIER_3_PERCENT > tier3Genres.size()) {
                        tier3Genres.add(k.getKey());
                    }
                });
//        System.out.println("Finding genres took " + t.end() + " milliseconds");

        t.reset();
        t.start();
        ArrayList<AudioFeatures> audioFeatures = new ArrayList<>();
        for (int i = 0; i <= tracks.size() / 100; i++) {
            String[] ids = new String[tracks.size() % 100];
            for (int j = 0; j < ids.length; j++) {
                ids[j] = tracks.get(j + (i * 100)).getId();
            }
            ApiCalls.getAudioFeatures(ids, user);
            GetAudioFeaturesForSeveralTracksRequest gaffstr = user.getSpotifyApi()
                    .getAudioFeaturesForSeveralTracks(ids)
                    .build();
            try {
                AudioFeatures[] features = gaffstr.execute();
                audioFeatures.addAll(Arrays.asList(features));
            } catch (TooManyRequestsException e) {
                try {
                    i--;
                    Thread.sleep(1000 * e.getRetryAfter() + 100);
                } catch (Exception ex) {
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        System.out.println("Finding audio features took " + t.end() + " milliseconds");

        float[] danceability = new float[audioFeatures.size()];
        float[] energy = new float[audioFeatures.size()];
        float[] speechiness = new float[audioFeatures.size()];
        float[] acousticness = new float[audioFeatures.size()];
        float[] liveness = new float[audioFeatures.size()];
        float[] tempo = new float[audioFeatures.size()];

        for (int i = 0; i < audioFeatures.size(); i++) {
            try {
                danceability[i] = audioFeatures.get(i).getDanceability();
                energy[i] = audioFeatures.get(i).getEnergy();
                speechiness[i] = audioFeatures.get(i).getSpeechiness();
                acousticness[i] = audioFeatures.get(i).getAcousticness();
                liveness[i] = audioFeatures.get(i).getLiveness();
                tempo[i] = audioFeatures.get(i).getTempo();
            } catch (Exception e) {
            }
        }

        this.danceabilityStDev = calculateSD(danceability);
        this.danceabilityAvg = calculateAvg(danceability);
        this.energyStDev = calculateSD(energy);
        this.energyAvg = calculateAvg(energy);
        this.speechinessStDev = calculateSD(speechiness);
        this.speechinessAvg = calculateAvg(speechiness);
        this.acousticnessStDev = calculateSD(acousticness);
        this.acousticnessAvg = calculateAvg(acousticness);
        this.livelinessStDev = calculateSD(liveness);
        this.livelinessAvg = calculateAvg(liveness);
        this.tempoStDev = calculateSD(tempo);
        this.tempoAvg = calculateAvg(tempo);
    }

    public static double calculateSD(float numArray[]) {
        double sum = 0.0, standardDeviation = 0.0;
        int length = numArray.length;

        for (double num : numArray) {
            sum += num;
        }

        double mean = sum / length;

        for (double num : numArray) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation / length);
    }

    /**
     * The audio features component of the match is based on a flat bell curve (not the right term), using the Empirical Rule.
     * e.g. if the avg for a feature is within half of one stDev of the user's avg, it will count as a 100% match for that feature.
     * Within 1/2 stDev: 100%
     * Within 1 stDev: 90%
     * Within 1 1/2 stDev: 75%
     * Within 2 stDev: 50%
     * More than 2 stDev: 0%
     *
     * @param user
     * @param fingerprintToMatch
     * @return
     */
    public static double findMatch(User user, Fingerprint fingerprintToMatch) {
        final int FIRST_STDEV_DIVISION_SCORE = 100;
        final int SECOND_STDEV_DIVISION_SCORE = 95;
        final int THIRD_STDEV_DIVISION_SCORE = 85;
        final int FOURTH_STDEV_DIVISION_SCORE = 70;
        final double FIRST_STDEV_DIVISION_SIZE = 0.125;
        final double SECOND_STDEV_DIVISION_SIZE = 0.25;
        final double THIRD_STDEV_DIVISION_SIZE = 0.75;
        final double FOURTH_STDEV_DIVISION_SIZE = 1.5;
        double audioFeaturesMatch = 0;
        Fingerprint fingerprint = user.getFingerprint();
        //Danceability
        if (fingerprintToMatch.getDanceabilityAvg() > (fingerprint.getDanceabilityAvg() - fingerprint.getDanceabilityStDev() * FIRST_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getDanceabilityAvg() < (fingerprint.getDanceabilityAvg() + fingerprint.getDanceabilityStDev() * FIRST_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += FIRST_STDEV_DIVISION_SCORE * fingerprint.DANCEABILITY_WEIGHT;
        } else if (fingerprintToMatch.getDanceabilityAvg() > (fingerprint.getDanceabilityAvg() - fingerprint.getDanceabilityStDev() * SECOND_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getDanceabilityAvg() < (fingerprint.getDanceabilityAvg() + fingerprint.getDanceabilityStDev() * SECOND_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += SECOND_STDEV_DIVISION_SCORE * fingerprint.DANCEABILITY_WEIGHT;
        } else if (fingerprintToMatch.getDanceabilityAvg() > (fingerprint.getDanceabilityAvg() - fingerprint.getDanceabilityStDev() * THIRD_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getDanceabilityAvg() < (fingerprint.getDanceabilityAvg() + fingerprint.getDanceabilityStDev() * THIRD_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += THIRD_STDEV_DIVISION_SCORE * fingerprint.DANCEABILITY_WEIGHT;
        } else if (fingerprintToMatch.getDanceabilityAvg() > (fingerprint.getDanceabilityAvg() - fingerprint.getDanceabilityStDev() * FOURTH_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getDanceabilityAvg() < (fingerprint.getDanceabilityAvg() + fingerprint.getDanceabilityStDev() * FOURTH_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += FOURTH_STDEV_DIVISION_SCORE * fingerprint.DANCEABILITY_WEIGHT;
        }
        //Energy
        if (fingerprintToMatch.getEnergyAvg() > (fingerprint.getEnergyAvg() - fingerprint.getEnergyStDev() * FIRST_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getEnergyAvg() < (fingerprint.getEnergyAvg() + fingerprint.getEnergyStDev() * FIRST_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += FIRST_STDEV_DIVISION_SCORE * fingerprint.ENERGY_WEIGHT;
        } else if (fingerprintToMatch.getEnergyAvg() > (fingerprint.getEnergyAvg() - fingerprint.getEnergyStDev() * SECOND_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getEnergyAvg() < (fingerprint.getEnergyAvg() + fingerprint.getEnergyStDev() * SECOND_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += SECOND_STDEV_DIVISION_SCORE * fingerprint.ENERGY_WEIGHT;
        } else if (fingerprintToMatch.getEnergyAvg() > (fingerprint.getEnergyAvg() - fingerprint.getEnergyStDev() * THIRD_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getEnergyAvg() < (fingerprint.getEnergyAvg() + fingerprint.getEnergyStDev() * THIRD_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += THIRD_STDEV_DIVISION_SCORE * fingerprint.ENERGY_WEIGHT;
        } else if (fingerprintToMatch.getEnergyAvg() > (fingerprint.getEnergyAvg() - fingerprint.getEnergyStDev() * FOURTH_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getEnergyAvg() < (fingerprint.getEnergyAvg() + fingerprint.getEnergyStDev() * FOURTH_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += FOURTH_STDEV_DIVISION_SCORE * fingerprint.ENERGY_WEIGHT;
        }
        //Speechiness
        if (fingerprintToMatch.getSpeechinessAvg() > (fingerprint.getSpeechinessAvg() - fingerprint.getSpeechinessStDev() * FIRST_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getSpeechinessAvg() < (fingerprint.getSpeechinessAvg() + fingerprint.getSpeechinessStDev() * FIRST_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += FIRST_STDEV_DIVISION_SCORE * fingerprint.SPEECHINESS_WEIGHT;
        } else if (fingerprintToMatch.getSpeechinessAvg() > (fingerprint.getSpeechinessAvg() - fingerprint.getSpeechinessStDev() * SECOND_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getSpeechinessAvg() < (fingerprint.getSpeechinessAvg() + fingerprint.getSpeechinessStDev() * SECOND_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += SECOND_STDEV_DIVISION_SCORE * fingerprint.SPEECHINESS_WEIGHT;
        } else if (fingerprintToMatch.getSpeechinessAvg() > (fingerprint.getSpeechinessAvg() - fingerprint.getSpeechinessStDev() * THIRD_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getSpeechinessAvg() < (fingerprint.getSpeechinessAvg() + fingerprint.getSpeechinessStDev() * THIRD_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += THIRD_STDEV_DIVISION_SCORE * fingerprint.SPEECHINESS_WEIGHT;
        } else if (fingerprintToMatch.getSpeechinessAvg() > (fingerprint.getSpeechinessAvg() - fingerprint.getSpeechinessStDev() * FOURTH_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getSpeechinessAvg() < (fingerprint.getSpeechinessAvg() + fingerprint.getSpeechinessStDev() * FOURTH_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += FOURTH_STDEV_DIVISION_SCORE * fingerprint.SPEECHINESS_WEIGHT;
        }
        //Acousticness
        if (fingerprintToMatch.getAcousticnessAvg() > (fingerprint.getAcousticnessAvg() - fingerprint.getAcousticnessStDev() * FIRST_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getAcousticnessAvg() < (fingerprint.getAcousticnessAvg() + fingerprint.getAcousticnessStDev() * FIRST_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += FIRST_STDEV_DIVISION_SCORE * fingerprint.ACOUSTICNESS_WEIGHT;
        } else if (fingerprintToMatch.getAcousticnessAvg() > (fingerprint.getAcousticnessAvg() - fingerprint.getAcousticnessStDev() * SECOND_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getAcousticnessAvg() < (fingerprint.getAcousticnessAvg() + fingerprint.getAcousticnessStDev() * SECOND_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += SECOND_STDEV_DIVISION_SCORE * fingerprint.ACOUSTICNESS_WEIGHT;
        } else if (fingerprintToMatch.getAcousticnessAvg() > (fingerprint.getAcousticnessAvg() - fingerprint.getAcousticnessStDev() * THIRD_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getAcousticnessAvg() < (fingerprint.getAcousticnessAvg() + fingerprint.getAcousticnessStDev() * THIRD_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += THIRD_STDEV_DIVISION_SCORE * fingerprint.ACOUSTICNESS_WEIGHT;
        } else if (fingerprintToMatch.getAcousticnessAvg() > (fingerprint.getAcousticnessAvg() - fingerprint.getAcousticnessStDev() * FOURTH_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getAcousticnessAvg() < (fingerprint.getAcousticnessAvg() + fingerprint.getAcousticnessStDev() * FOURTH_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += FOURTH_STDEV_DIVISION_SCORE * fingerprint.ACOUSTICNESS_WEIGHT;
        }
        //Liveliness
        if (fingerprintToMatch.getLivelinessAvg() > (fingerprint.getLivelinessAvg() - fingerprint.getLivelinessStDev() * FIRST_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getLivelinessAvg() < (fingerprint.getLivelinessAvg() + fingerprint.getLivelinessStDev() * FIRST_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += FIRST_STDEV_DIVISION_SCORE * fingerprint.LIVELINESS_WEIGHT;
        } else if (fingerprintToMatch.getLivelinessAvg() > (fingerprint.getLivelinessAvg() - fingerprint.getLivelinessStDev() * SECOND_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getLivelinessAvg() < (fingerprint.getLivelinessAvg() + fingerprint.getLivelinessStDev() * SECOND_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += SECOND_STDEV_DIVISION_SCORE * fingerprint.LIVELINESS_WEIGHT;
        } else if (fingerprintToMatch.getLivelinessAvg() > (fingerprint.getLivelinessAvg() - fingerprint.getLivelinessStDev() * THIRD_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getLivelinessAvg() < (fingerprint.getLivelinessAvg() + fingerprint.getLivelinessStDev() * THIRD_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += THIRD_STDEV_DIVISION_SCORE * fingerprint.LIVELINESS_WEIGHT;
        } else if (fingerprintToMatch.getLivelinessAvg() > (fingerprint.getLivelinessAvg() - fingerprint.getLivelinessStDev() * FOURTH_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getLivelinessAvg() < (fingerprint.getLivelinessAvg() + fingerprint.getLivelinessStDev() * FOURTH_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += FOURTH_STDEV_DIVISION_SCORE * fingerprint.LIVELINESS_WEIGHT;
        }
        //Tempo
        if (fingerprintToMatch.getTempoAvg() > (fingerprint.getTempoAvg() - fingerprint.getTempoStDev() * FIRST_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getTempoAvg() < (fingerprint.getTempoAvg() + fingerprint.getTempoStDev() * FIRST_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += FIRST_STDEV_DIVISION_SCORE * fingerprint.TEMPO_WEIGHT;
        } else if (fingerprintToMatch.getTempoAvg() > (fingerprint.getTempoAvg() - fingerprint.getTempoStDev() * SECOND_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getTempoAvg() < (fingerprint.getTempoAvg() + fingerprint.getTempoStDev() * SECOND_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += SECOND_STDEV_DIVISION_SCORE * fingerprint.TEMPO_WEIGHT;
        } else if (fingerprintToMatch.getTempoAvg() > (fingerprint.getTempoAvg() - fingerprint.getTempoStDev() * THIRD_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getTempoAvg() < (fingerprint.getTempoAvg() + fingerprint.getTempoStDev() * THIRD_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += THIRD_STDEV_DIVISION_SCORE * fingerprint.TEMPO_WEIGHT;
        } else if (fingerprintToMatch.getTempoAvg() > (fingerprint.getTempoAvg() - fingerprint.getTempoStDev() * FOURTH_STDEV_DIVISION_SIZE) &&
                fingerprintToMatch.getTempoAvg() < (fingerprint.getTempoAvg() + fingerprint.getTempoStDev() * FOURTH_STDEV_DIVISION_SIZE)) {
            audioFeaturesMatch += FOURTH_STDEV_DIVISION_SCORE * fingerprint.TEMPO_WEIGHT;
        }
        float genreMatch = 100;
        /*For genre in fingerprint.tier1
            find total of genres in the fingerprintToMatch that are in tier1
          find percentage that the total represents
          deduct percentage * DEDUCTION

         */
        double tier1Total = 0;
        double tier2Total = 0;
        double tier3Total = 0;

        System.out.println("*****TIER1*****");
        fingerprint.tier1Genres.forEach(System.out::println);
        System.out.println("*****TIER2*****");
        fingerprint.tier2Genres.forEach(System.out::println);
        System.out.println("*****TIER3*****");
        fingerprint.tier3Genres.forEach(System.out::println);

        for (String genre : fingerprint.tier1Genres) {
            if (fingerprintToMatch.tier1Genres.contains(genre)) {
                tier1Total++;
            }
        }
        genreMatch -= (1 - tier1Total / fingerprint.tier1Genres.size()) * fingerprint.TIER_1_DEDUCTION;

        for (String genre : fingerprint.tier2Genres) {
            if (fingerprintToMatch.tier2Genres.contains(genre)) {
                tier2Total++;
            }
        }
        genreMatch -= (1 - tier2Total / fingerprint.tier2Genres.size()) * fingerprint.TIER_2_DEDUCTION;

        for (String genre : fingerprint.tier3Genres) {
            if (fingerprintToMatch.tier3Genres.contains(genre)) {
                tier3Total++;
            }
        }
        genreMatch -= (1 - tier3Total / fingerprint.tier3Genres.size()) * fingerprint.TIER_3_DEDUCTION;

        System.out.println("Audio features match: " + audioFeaturesMatch);
        System.out.println("Genre match: " + genreMatch);
        return (audioFeaturesMatch * fingerprint.AUDIO_FEATURES_WEIGHT) + (genreMatch * fingerprint.GENRE_WEIGHT);
    }

    private double calculateAvg(float numArray[]) {
        double sum = 0.0;
        for (double num : numArray) {
            sum += num;
        }
        return sum / numArray.length;
    }

    public double getDanceabilityAvg() {
        return danceabilityAvg;
    }

    @Override
    public String toString() {
        return "Fingerprint{" +
                "\ndanceabilityAvg=" + danceabilityAvg +
                "\ndanceabilityStDev=" + danceabilityStDev +
                "\nenergyAvg=" + energyAvg +
                "\nenergyStDev=" + energyStDev +
                "\nspeechinessAvg=" + speechinessAvg +
                "\nspeechinessStDev=" + speechinessStDev +
                "\nacousticnessAvg=" + acousticnessAvg +
                "\nacousticnessStDev=" + acousticnessStDev +
                "\nlivelinessAvg=" + livelinessAvg +
                "\nlivelinessStDev=" + livelinessStDev +
                "\ntempoAvg=" + tempoAvg +
                "\ntempoStDev=" + tempoStDev +
                "\n}";
    }

    public double getDanceabilityStDev() {
        return danceabilityStDev;
    }

    public void setDanceability(double avg, double stDev) {
        this.danceabilityAvg = avg;
        this.danceabilityStDev = stDev;
    }

    public double getEnergyAvg() {
        return energyAvg;
    }

    public double getEnergyStDev() {
        return energyStDev;
    }

    public void setEnergy(double avg, double stDev) {
        this.energyAvg = avg;
        this.energyStDev = stDev;
    }

    public double getSpeechinessAvg() {
        return speechinessAvg;
    }

    public double getSpeechinessStDev() {
        return speechinessStDev;
    }

    public void setSpeechiness(double avg, double stDev) {
        this.speechinessAvg = avg;
        this.speechinessStDev = stDev;
    }

    public double getAcousticnessAvg() {
        return acousticnessAvg;
    }

    public double getAcousticnessStDev() {
        return acousticnessStDev;
    }

    public void setAcousticness(double avg, double stDev) {
        this.acousticnessAvg = avg;
        this.acousticnessStDev = stDev;
    }

    public double getLivelinessAvg() {
        return livelinessAvg;
    }

    public double getLivelinessStDev() {
        return livelinessStDev;
    }

    public void setLiveliness(double avg, double stDev) {
        this.livelinessAvg = avg;
        this.livelinessStDev = stDev;
    }

    public double getTempoAvg() {
        return tempoAvg;
    }

    public double getTempoStDev() {
        return tempoStDev;
    }

    public void setTempo(double avg, double stDev) {
        this.tempoAvg = avg;
        this.tempoStDev = stDev;
    }

    public Date getDateCreated() {
        return dateCreated;
    }
}
