/**
 * Mood calculation engine based on Spotify audio features.
 * Maps numeric audio characteristics to one of five mood categories.
 * Uses a rule-based algorithm that prioritizes energy, valence, and tempo.
 */
package util;

import model.Mood;

public class MoodCalculator {

    /**
     * Private constructor prevents instantiation of this utility class.
     */
    private MoodCalculator() {}

    /**
     * Calculates song mood from Spotify audio features.
     * 
     * Classification logic (checked in order):
     * 1. High energy + fast tempo → ENERGETIC
     * 2. High valence + moderate energy → HAPPY
     * 3. Low valence + minor key → MELANCHOLIC
     * 4. Low energy + high acousticness → RELAXED
     * 5. Mid energy + low danceability → FOCUSED
     * 6. Default → RELAXED
     * 
     * @param danceability How suitable for dancing (0.0-1.0)
     * @param energy       Intensity and activity level (0.0-1.0)
     * @param acousticness Confidence the track is acoustic (0.0-1.0)
     * @param valence      Musical positiveness/happiness (0.0-1.0)
     * @param tempo        Beats per minute (typically 60-200)
     * @param mode         Musical mode: 1=major, 0=minor
     * @return Calculated mood category
     */
    public static Mood fromAudioFeatures(double danceability, double energy,
                                         double acousticness, double valence,
                                         double tempo, int mode) {
        // Normalize tempo to 0-1 scale (60-200 BPM range)
        double tempoNorm = normalizeTempo(tempo);

        // Classification rules (order matters - first match wins)
        if (energy > 0.75 && tempoNorm > 0.65) return Mood.ENERGETIC;
        if (valence > 0.65 && energy > 0.5) return Mood.HAPPY;
        if (valence < 0.4 && mode == 0) return Mood.MELANCHOLIC;
        if (energy < 0.45 && acousticness > 0.5) return Mood.RELAXED;
        if (energy >= 0.4 && energy <= 0.65 && danceability < 0.5) return Mood.FOCUSED;
        
        // Default to RELAXED if no other category fits
        return Mood.RELAXED;
    }

    /**
     * Normalizes tempo from BPM (60-200) to a 0-1 scale.
     * Values outside the range are clamped to 0 or 1.
     */
    private static double normalizeTempo(double tempo) {
        double norm = (tempo - 60.0) / (200.0 - 60.0);
        if (norm < 0) return 0;
        return Math.min(norm, 1);
    }
}
