import com.wrapper.spotify.model_objects.specification.AudioFeatures;
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

    public Fingerprint() {
    }

    public Fingerprint(ArrayList<Track> tracks, User user) {
        this.username = user.getUsername();
        for (Track track : tracks) {
            //TODO Add artist genre stuff here
        }
        ArrayList<AudioFeatures> audioFeatures = new ArrayList<>();
        for (int i = 0; i < tracks.size() / 100; i++) {
            String[] ids = new String[tracks.size() % 100];
            for (int j = 0; j < ids.length; j++) {
                ids[j] = tracks.get(j + (i * 100)).getId();
            }
            GetAudioFeaturesForSeveralTracksRequest gaffstr = user.getSpotifyApi()
                    .getAudioFeaturesForSeveralTracks(ids)
                    .build();
            try {
                AudioFeatures[] features = gaffstr.execute();
                audioFeatures.addAll(Arrays.asList(features));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

    public static double calculateAvg(float numArray[]) {
        double sum = 0.0;
        for (double num : numArray) {
            sum += num;
        }
        return sum / numArray.length;
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
