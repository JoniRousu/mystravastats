package me.nicolas.stravastats.business

import me.nicolas.stravastats.utils.formatSeconds


/**
 * An effort within an activity.
 */
data class ActivityEffort(
    val activity: Activity,
    val distance: Double,
    val seconds: Int,
    val altitude: Double,
    val ixStart: Int,
    val ixEnd: Int
) {
    fun getFormattedSpeed(): String {
        return if (activity.type == Run) {
            "${(seconds * 1000 / distance).formatSeconds()}/km"
        } else {
            "%.02f km/h".format(distance / seconds * 3600 / 1000)
        }
    }

    fun getSpeed(): String {
        return if (activity.type == Run) {
            (seconds * 1000 / distance).formatSeconds()
        } else {
            "%.02f".format(distance / seconds * 3600 / 1000)
        }
    }

    fun getFormattedGradient() = "${this.getGradient()} %"

    fun getGradient() = "%.02f".format(100 * altitude / distance)
}