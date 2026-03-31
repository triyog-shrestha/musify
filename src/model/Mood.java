/**
 * Enum representing the five mood categories for songs in Musify.
 * Each mood corresponds to a numeric score used in calculations:
 * - ENERGETIC: 1.0 (highest energy)
 * - HAPPY: 0.8 (positive, upbeat)
 * - FOCUSED: 0.6 (moderate, attentive)
 * - RELAXED: 0.4 (calm, low energy)
 * - MELANCHOLIC: 0.2 (sad, introspective)
 * 
 * Moods are automatically assigned by MoodCalculator based on Spotify audio features.
 */
package model;

public enum Mood {
    RELAXED,
    HAPPY,
    MELANCHOLIC,
    ENERGETIC,
    FOCUSED
}