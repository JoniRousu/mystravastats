package me.nicolas.stravastats.ihm

import javafx.scene.control.Hyperlink

data class StatisticDisplay(val label: String, val value: String, val activity: Hyperlink?)
data class ActivityDisplay(val name: Hyperlink?, val distance: Double, val totalElevationGain: Double, val date: String)
data class BadgeDisplay(val label: String, val activity: Hyperlink?)