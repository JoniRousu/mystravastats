package me.nicolas.stravastats.ihm.task

import javafx.concurrent.Task
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.business.Athlete
import me.nicolas.stravastats.service.StravaService
import java.time.LocalDate
import kotlin.system.measureTimeMillis

internal class StravaLoadActivitiesTask(clientId: String, clientSecret: String) :
    Task<Pair<Athlete?, List<Activity>>>() {

    private val stravaService = StravaService(clientId, clientSecret)

    override fun call(): Pair<Athlete?, List<Activity>> {
        updateMessage("Waiting for your agreement to allow MyStravaStats to access to your Strava data ...")
        val athlete = stravaService.getLoggedInAthlete()

        val activities = mutableListOf<Activity>()
        val elapsed = measureTimeMillis {
            for (currentYear in LocalDate.now().year downTo 2010) {
                updateMessage("Loading $currentYear activities ...")
                activities.addAll(stravaService.getActivities(currentYear))
            }
        }
        updateMessage("All activities are loaded.")
        println("All activities are loaded in ${elapsed / 1000} s.")

        return Pair(athlete, activities)
    }
}