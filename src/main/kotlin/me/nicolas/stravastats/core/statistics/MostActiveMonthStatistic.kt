package me.nicolas.stravastats.core.statistics

import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.helpers.inDateTimeFormatter
import java.time.LocalDateTime
import java.time.format.TextStyle
import java.util.*

internal class MostActiveMonthStatistic(
    activities: List<Activity>
) : Statistic("Most active month", activities) {

    private val mostActiveMonth =
        activities.groupBy { getMonth(it.startDateLocal) }
            .mapValues { (_, activities) -> activities.sumByDouble { it.distance } }
            .maxByOrNull { it.value }

    private fun getMonth(startDateLocal: String) = LocalDateTime.parse(startDateLocal, inDateTimeFormatter).month

    override fun toString(): String {
        return super.toString() + if (mostActiveMonth != null) {
            "%s with %.2f km".format(
                mostActiveMonth.key?.getDisplayName(TextStyle.FULL, Locale.ENGLISH), mostActiveMonth.value.div(1000)
            )
        } else {
            "Not available"
        }
    }
}