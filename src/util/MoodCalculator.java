package util;

import model.Mood;

public class MoodCalculator {

	private MoodCalculator() {}

	public static Mood fromAudioFeatures(double danceability, double energy,
										 double acousticness, double valence,
										 double tempo, int mode) {
		double tempoNorm = normalizeTempo(tempo);

		if (energy > 0.75 && tempoNorm > 0.65) return Mood.ENERGETIC;
		if (valence > 0.65 && energy > 0.5) return Mood.HAPPY;
		if (valence < 0.4 && mode == 0) return Mood.MELANCHOLIC;
		if (energy < 0.45 && acousticness > 0.5) return Mood.RELAXED;
		if (energy >= 0.4 && energy <= 0.65 && danceability < 0.5) return Mood.FOCUSED;
		return Mood.RELAXED;
	}

	private static double normalizeTempo(double tempo) {
		double norm = (tempo - 60.0) / (200.0 - 60.0);
		if (norm < 0) return 0;
		return Math.min(norm, 1);
	}
}
