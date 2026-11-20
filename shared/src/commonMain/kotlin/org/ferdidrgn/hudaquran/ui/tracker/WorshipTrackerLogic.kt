package org.ferdidrgn.hudaquran.ui.tracker

/** A user's private reminder state; it is not a religious ruling or a validity judgement. */
internal enum class PrayerMark {
    PENDING,
    COMPLETED,
    QADA,
}

/**
 * Each action is reversible and never makes a user lose a previous mark accidentally:
 * tapping the current state clears it, while choosing the other state replaces it.
 */
internal fun togglePrayerMark(current: PrayerMark, requested: PrayerMark): PrayerMark =
    if (current == requested) PrayerMark.PENDING else requested

internal data class PrayerDaySummary(
    val completed: Int = 0,
    val qada: Int = 0,
) {
    val marked: Int get() = completed + qada
}

internal fun weeklyPrayerProgress(
    summaries: List<PrayerDaySummary>,
    target: Int = 25,
): Pair<Int, Int> {
    val safeTarget = target.coerceAtLeast(1)
    return summaries.sumOf { it.completed } to safeTarget
}
