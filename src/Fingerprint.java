import com.wrapper.spotify.exceptions.detailed.TooManyRequestsException;
import com.wrapper.spotify.model_objects.specification.AudioFeatures;
import com.wrapper.spotify.requests.data.artists.GetSeveralArtistsRequest;
import com.wrapper.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * The fingerprint class contains data about a certain user's music taste, and will be used by the various Match classes
 * in order to generate a match.
 *
 * @author The Guardians of Java
 * @since 2018-10-26
 */

public class Fingerprint implements Serializable {
    public final double TIER_1_PERCENT = 0.15;
    public final double TIER_2_PERCENT = 0.30;
    public final double TIER_3_PERCENT = 1 - TIER_1_PERCENT - TIER_2_PERCENT;
    public final int TIER_1_DEDUCTION = 40;
    public final int TIER_2_DEDUCTION = 20;
    public final int TIER_3_DEDUCTION = 5;
    public final double DANCEABILITY_WEIGHT = 1 / 6.0;
    public final double ENERGY_WEIGHT = 1 / 6.0;
    public final double SPEECHINESS_WEIGHT = 1 / 6.0;
    public final double ACOUSTICNESS_WEIGHT = 1 / 6.0;
    public final double LIVELINESS_WEIGHT = 1 / 6.0;
    public final double TEMPO_WEIGHT = 1 / 6.0;
    public final double AUDIO_FEATURES_WEIGHT = 0.7;
    public final double GENRE_WEIGHT = 1 - AUDIO_FEATURES_WEIGHT;

    private String username;
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
    private HashMap<String, Integer> genres;
    private ArrayList<String> tier1Genres;
    private ArrayList<String> tier2Genres;
    private ArrayList<String> tier3Genres;

    public Fingerprint() {
    }

    public Fingerprint(ArrayList<Track> tracks, User user) {
        tier1Genres = new ArrayList<>();
        tier2Genres = new ArrayList<>();
        tier3Genres = new ArrayList<>();
        genres = new HashMap<>();
        this.username = user.getUsername();
        ArrayList<Artist> artists = new ArrayList<>();
        for (Track track : tracks) {
            ArtistCollection collection = track.getArtists();
            artists.addAll(collection.getArtists());
        }
        Timer t = new Timer(true);
        //Hopefully a more efficient way of getting genres.
        for (int i = 0; i < Shared.artists.size() / 50; i++) {
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
        //TODO make sorting better. It only seems to work on the first chunk of the map.
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
        System.out.println("*******TIER 1********");
        for (String s : tier1Genres) {
            System.out.println(s);
        }
        System.out.println("*******TIER 2********");
        for (String s : tier2Genres) {
            System.out.println(s);
        }
        System.out.println("*******TIER 3********");
        for (String s : tier3Genres) {
            System.out.println(s);
        }
        System.out.println("Finding genres took " + t.end() + " milliseconds");

        t.reset();
        t.start();
        ArrayList<AudioFeatures> audioFeatures = new ArrayList<>();
        for (int i = 0; i < tracks.size() / 100; i++) {
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
        System.out.println("Finding audio features took " + t.end() + " milliseconds");

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
        final int HALF_STDEV_SCORE = 100;
        final int ONE_STDEV_SCORE = 80;
        final int ONE_AND_HALF_STDEV_SCORE = 65;
        final int TWO_STDEV_SCORE = 40;
        double audioFeaturesMatch = 0;
        Fingerprint fingerprint = user.getFingerprint();
        //Danceability
        if (fingerprintToMatch.getDanceabilityAvg() > (fingerprint.getDanceabilityAvg() - fingerprint.getDanceabilityStDev() * 0.25) &&
                fingerprintToMatch.getDanceabilityAvg() < (fingerprint.getDanceabilityAvg() + fingerprint.getDanceabilityStDev() * 0.25)) {
            audioFeaturesMatch += HALF_STDEV_SCORE * fingerprint.DANCEABILITY_WEIGHT;
        } else if (fingerprintToMatch.getDanceabilityAvg() > (fingerprint.getDanceabilityAvg() - fingerprint.getDanceabilityStDev() * 1) &&
                fingerprintToMatch.getDanceabilityAvg() < (fingerprint.getDanceabilityAvg() + fingerprint.getDanceabilityStDev() * 1)) {
            audioFeaturesMatch += ONE_STDEV_SCORE * fingerprint.DANCEABILITY_WEIGHT;
        } else if (fingerprintToMatch.getDanceabilityAvg() > (fingerprint.getDanceabilityAvg() - fingerprint.getDanceabilityStDev() * 1.5) &&
                fingerprintToMatch.getDanceabilityAvg() < (fingerprint.getDanceabilityAvg() + fingerprint.getDanceabilityStDev() * 1.5)) {
            audioFeaturesMatch += ONE_AND_HALF_STDEV_SCORE * fingerprint.DANCEABILITY_WEIGHT;
        } else if (fingerprintToMatch.getDanceabilityAvg() > (fingerprint.getDanceabilityAvg() - fingerprint.getDanceabilityStDev() * 2) &&
                fingerprintToMatch.getDanceabilityAvg() < (fingerprint.getDanceabilityAvg() + fingerprint.getDanceabilityStDev() * 2)) {
            audioFeaturesMatch += TWO_STDEV_SCORE * fingerprint.DANCEABILITY_WEIGHT;
        }
        System.out.println(audioFeaturesMatch);
        //Energy
        if (fingerprintToMatch.getEnergyAvg() > (fingerprint.getEnergyAvg() - fingerprint.getEnergyStDev() * 0.25) &&
                fingerprintToMatch.getEnergyAvg() < (fingerprint.getEnergyAvg() + fingerprint.getEnergyStDev() * 0.25)) {
            audioFeaturesMatch += HALF_STDEV_SCORE * fingerprint.ENERGY_WEIGHT;
        } else if (fingerprintToMatch.getEnergyAvg() > (fingerprint.getEnergyAvg() - fingerprint.getEnergyStDev() * 1) &&
                fingerprintToMatch.getEnergyAvg() < (fingerprint.getEnergyAvg() + fingerprint.getEnergyStDev() * 1)) {
            audioFeaturesMatch += ONE_STDEV_SCORE * fingerprint.ENERGY_WEIGHT;
        } else if (fingerprintToMatch.getEnergyAvg() > (fingerprint.getEnergyAvg() - fingerprint.getEnergyStDev() * 1.5) &&
                fingerprintToMatch.getEnergyAvg() < (fingerprint.getEnergyAvg() + fingerprint.getEnergyStDev() * 1.5)) {
            audioFeaturesMatch += ONE_AND_HALF_STDEV_SCORE * fingerprint.ENERGY_WEIGHT;
        } else if (fingerprintToMatch.getEnergyAvg() > (fingerprint.getEnergyAvg() - fingerprint.getEnergyStDev() * 2) &&
                fingerprintToMatch.getEnergyAvg() < (fingerprint.getEnergyAvg() + fingerprint.getEnergyStDev() * 2)) {
            audioFeaturesMatch += TWO_STDEV_SCORE * fingerprint.ENERGY_WEIGHT;
        }
        System.out.println(audioFeaturesMatch);
        //Speechiness
        if (fingerprintToMatch.getSpeechinessAvg() > (fingerprint.getSpeechinessAvg() - fingerprint.getSpeechinessStDev() * 0.25) &&
                fingerprintToMatch.getSpeechinessAvg() < (fingerprint.getSpeechinessAvg() + fingerprint.getSpeechinessStDev() * 0.25)) {
            audioFeaturesMatch += HALF_STDEV_SCORE * fingerprint.SPEECHINESS_WEIGHT;
        } else if (fingerprintToMatch.getSpeechinessAvg() > (fingerprint.getSpeechinessAvg() - fingerprint.getSpeechinessStDev() * 1) &&
                fingerprintToMatch.getSpeechinessAvg() < (fingerprint.getSpeechinessAvg() + fingerprint.getSpeechinessStDev() * 1)) {
            audioFeaturesMatch += ONE_STDEV_SCORE * fingerprint.SPEECHINESS_WEIGHT;
        } else if (fingerprintToMatch.getSpeechinessAvg() > (fingerprint.getSpeechinessAvg() - fingerprint.getSpeechinessStDev() * 1.5) &&
                fingerprintToMatch.getSpeechinessAvg() < (fingerprint.getSpeechinessAvg() + fingerprint.getSpeechinessStDev() * 1.5)) {
            audioFeaturesMatch += ONE_AND_HALF_STDEV_SCORE * fingerprint.SPEECHINESS_WEIGHT;
        } else if (fingerprintToMatch.getSpeechinessAvg() > (fingerprint.getSpeechinessAvg() - fingerprint.getSpeechinessStDev() * 2) &&
                fingerprintToMatch.getSpeechinessAvg() < (fingerprint.getSpeechinessAvg() + fingerprint.getSpeechinessStDev() * 2)) {
            audioFeaturesMatch += TWO_STDEV_SCORE * fingerprint.SPEECHINESS_WEIGHT;
        }
        System.out.println(audioFeaturesMatch);
        //Acousticness
        if (fingerprintToMatch.getAcousticnessAvg() > (fingerprint.getAcousticnessAvg() - fingerprint.getAcousticnessStDev() * 0.25) &&
                fingerprintToMatch.getAcousticnessAvg() < (fingerprint.getAcousticnessAvg() + fingerprint.getAcousticnessStDev() * 0.25)) {
            audioFeaturesMatch += HALF_STDEV_SCORE * fingerprint.ACOUSTICNESS_WEIGHT;
        } else if (fingerprintToMatch.getAcousticnessAvg() > (fingerprint.getAcousticnessAvg() - fingerprint.getAcousticnessStDev() * 1) &&
                fingerprintToMatch.getAcousticnessAvg() < (fingerprint.getAcousticnessAvg() + fingerprint.getAcousticnessStDev() * 1)) {
            audioFeaturesMatch += ONE_STDEV_SCORE * fingerprint.ACOUSTICNESS_WEIGHT;
        } else if (fingerprintToMatch.getAcousticnessAvg() > (fingerprint.getAcousticnessAvg() - fingerprint.getAcousticnessStDev() * 1.5) &&
                fingerprintToMatch.getAcousticnessAvg() < (fingerprint.getAcousticnessAvg() + fingerprint.getAcousticnessStDev() * 1.5)) {
            audioFeaturesMatch += ONE_AND_HALF_STDEV_SCORE * fingerprint.ACOUSTICNESS_WEIGHT;
        } else if (fingerprintToMatch.getAcousticnessAvg() > (fingerprint.getAcousticnessAvg() - fingerprint.getAcousticnessStDev() * 2) &&
                fingerprintToMatch.getAcousticnessAvg() < (fingerprint.getAcousticnessAvg() + fingerprint.getAcousticnessStDev() * 2)) {
            audioFeaturesMatch += TWO_STDEV_SCORE * fingerprint.ACOUSTICNESS_WEIGHT;
        }
        System.out.println(audioFeaturesMatch);
        //Liveliness
        if (fingerprintToMatch.getLivelinessAvg() > (fingerprint.getLivelinessAvg() - fingerprint.getLivelinessStDev() * 0.25) &&
                fingerprintToMatch.getLivelinessAvg() < (fingerprint.getLivelinessAvg() + fingerprint.getLivelinessStDev() * 0.25)) {
            audioFeaturesMatch += HALF_STDEV_SCORE * fingerprint.LIVELINESS_WEIGHT;
        } else if (fingerprintToMatch.getLivelinessAvg() > (fingerprint.getLivelinessAvg() - fingerprint.getLivelinessStDev() * 1) &&
                fingerprintToMatch.getLivelinessAvg() < (fingerprint.getLivelinessAvg() + fingerprint.getLivelinessStDev() * 1)) {
            audioFeaturesMatch += ONE_STDEV_SCORE * fingerprint.LIVELINESS_WEIGHT;
        } else if (fingerprintToMatch.getLivelinessAvg() > (fingerprint.getLivelinessAvg() - fingerprint.getLivelinessStDev() * 1.5) &&
                fingerprintToMatch.getLivelinessAvg() < (fingerprint.getLivelinessAvg() + fingerprint.getLivelinessStDev() * 1.5)) {
            audioFeaturesMatch += ONE_AND_HALF_STDEV_SCORE * fingerprint.LIVELINESS_WEIGHT;
        } else if (fingerprintToMatch.getLivelinessAvg() > (fingerprint.getLivelinessAvg() - fingerprint.getLivelinessStDev() * 2) &&
                fingerprintToMatch.getLivelinessAvg() < (fingerprint.getLivelinessAvg() + fingerprint.getLivelinessStDev() * 2)) {
            audioFeaturesMatch += TWO_STDEV_SCORE * fingerprint.LIVELINESS_WEIGHT;
        }
        System.out.println(audioFeaturesMatch);
        //Tempo
        if (fingerprintToMatch.getTempoAvg() > (fingerprint.getTempoAvg() - fingerprint.getTempoStDev() * 0.25) &&
                fingerprintToMatch.getTempoAvg() < (fingerprint.getTempoAvg() + fingerprint.getTempoStDev() * 0.25)) {
            audioFeaturesMatch += HALF_STDEV_SCORE * fingerprint.TEMPO_WEIGHT;
        } else if (fingerprintToMatch.getTempoAvg() > (fingerprint.getTempoAvg() - fingerprint.getTempoStDev() * 1) &&
                fingerprintToMatch.getTempoAvg() < (fingerprint.getTempoAvg() + fingerprint.getTempoStDev() * 1)) {
            audioFeaturesMatch += ONE_STDEV_SCORE * fingerprint.TEMPO_WEIGHT;
        } else if (fingerprintToMatch.getTempoAvg() > (fingerprint.getTempoAvg() - fingerprint.getTempoStDev() * 1.5) &&
                fingerprintToMatch.getTempoAvg() < (fingerprint.getTempoAvg() + fingerprint.getTempoStDev() * 1.5)) {
            audioFeaturesMatch += ONE_AND_HALF_STDEV_SCORE * fingerprint.TEMPO_WEIGHT;
        } else if (fingerprintToMatch.getTempoAvg() > (fingerprint.getTempoAvg() - fingerprint.getTempoStDev() * 2) &&
                fingerprintToMatch.getTempoAvg() < (fingerprint.getTempoAvg() + fingerprint.getTempoStDev() * 2)) {
            audioFeaturesMatch += TWO_STDEV_SCORE * fingerprint.TEMPO_WEIGHT;
        }
        System.out.println(audioFeaturesMatch);
        return audioFeaturesMatch;
    }

    private double calculateAvg(float numArray[]) {
        double sum = 0.0;
        for (double num : numArray) {
            sum += num;
        }
        return sum / numArray.length;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getDanceabilityAvg() {
        return danceabilityAvg;
    }

    @Override
    public String toString() {
        return "Fingerprint{" +
                "username='" + username + '\'' +
                ", danceabilityAvg=" + danceabilityAvg +
                ", danceabilityStDev=" + danceabilityStDev +
                ", energyAvg=" + energyAvg +
                ", energyStDev=" + energyStDev +
                ", speechinessAvg=" + speechinessAvg +
                ", speechinessStDev=" + speechinessStDev +
                ", acousticnessAvg=" + acousticnessAvg +
                ", acousticnessStDev=" + acousticnessStDev +
                ", livelinessAvg=" + livelinessAvg +
                ", livelinessStDev=" + livelinessStDev +
                ", tempoAvg=" + tempoAvg +
                ", tempoStDev=" + tempoStDev +
                '}';
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
}
