package me.nicolas.stravastats

import com.beust.jcommander.JCommander
import com.beust.jcommander.ParameterException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.module.kotlin.KotlinModule
import me.nicolas.stravastats.business.Activity
import me.nicolas.stravastats.core.ActivityLoader
import me.nicolas.stravastats.core.StatsBuilder
import me.nicolas.stravastats.core.StravaService
import me.nicolas.stravastats.strava.StravaApi
import java.time.LocalDate


internal class MyStravaStats(incomingArgs: Array<String>) {

    private val stravaStatsProperties = loadPropertiesFromFile()

    private val stravaApi = StravaApi(stravaStatsProperties)

    private val statsBuilder = StatsBuilder()

    private val activityLoader = ActivityLoader(stravaStatsProperties, stravaApi)

    private val stravaService = StravaService(statsBuilder)

    private val parameters = Parameters()

    init {
        JCommander.newBuilder()
            .addObject(parameters)
            .programName("Strava Stats")
            .build().parse(*incomingArgs)

        println("http://www.strava.com/api/v3/oauth/authorize?client_id=${parameters.clientId}&response_type=code&redirect_uri=http://localhost:8080/exchange_token&approval_prompt=auto&scope=read_all,activity:read_all")
    }

    fun run() {
        val startTime = System.currentTimeMillis()

        val activities = mutableListOf<Activity>()
        if (parameters.year != null) {
            activities.addAll(loadActivities(parameters.clientId, parameters.year!!))
        } else {
            for (year in LocalDate.now().year downTo 2010) {
                activities.addAll(loadActivities(parameters.clientId, year))
            }
        }

        if (stravaStatsProperties.removingNonMovingSections) {
            activities.forEach { it.removeNonMoving() }
        }

        displayStatistics(activities)

        if (parameters.csv) {
            activities
                .groupBy { activity -> activity.startDateLocal.subSequence(0, 4).toString() }
                .forEach { exportCSV(filterActivities(it.value), it.key.toInt()) }
        }

        println()
        println("Execution time = ${System.currentTimeMillis() - startTime} m")
    }

    /**
     * Display statistics
     */
    private fun displayStatistics(activities: List<Activity>) {
        val stravaStats = stravaService.computeStatistics(activities)
        stravaStats.displayStatistics()
    }

    /**
     * Export activities in a CSV file.
     */
    private fun exportCSV(activities: List<Activity>, year: Int) {
        print("* Export activities for $year [")
        print("Ride")
        stravaService.exportBikeCSV(activities.filter { activity -> activity.type == "Ride" }, "Ride", year)
        print(", Run")
        stravaService.exportRunCSV(activities.filter { activity -> activity.type == "Run" }, "Run", year)
        print(", Hike")
        stravaService.exportHikeCSV(activities.filter { activity -> activity.type == "Hike" }, "Hike", year)
        println("]")

    }

    /**
     * Apply filter if exist.
     * @param activities activities to filter.
     */
    private fun filterActivities(activities: List<Activity>): List<Activity> {
        return if (parameters.filter != null) {
            val lowBoundary = parameters.filter!! - (5 * parameters.filter!! / 100)
            val highBoundary = parameters.filter!! + (5 * parameters.filter!! / 100)

            activities.filter { activity -> activity.distance > lowBoundary && activity.distance < highBoundary }
        } else {
            activities
        }
    }

    /**
     * Load activities
     */
    private fun loadActivities(clientId: String, year: Int): List<Activity> {

        return when {
            // with access token
            parameters.accessToken != null -> activityLoader.getActivitiesWithAccessToken(
                clientId,
                year,
                parameters.accessToken!!
            )
            // with access authorization code
            parameters.code != null && parameters.clientSecret != null -> activityLoader.getActivitiesWithAuthorizationCode(
                clientId,
                year,
                parameters.clientSecret!!,
                parameters.code!!
            )
            // from local cache
            parameters.code == null && parameters.accessToken == null -> activityLoader.getActivitiesFromFile(
                clientId,
                year
            )

            else -> throw ParameterException("-code with -clientSecret or -accessToken must be provided")
        }
    }

    /**
     * Load properties from application.yml
     */
    private fun loadPropertiesFromFile(): MyStravaStatsProperties {

        val mapper = ObjectMapper(YAMLFactory()) // Enable YAML parsing
        mapper.registerModule(KotlinModule()) // Enable Kotlin support

        val inputStream = javaClass.getResourceAsStream("/application.yml")
        return mapper.readValue(inputStream, MyStravaStatsProperties::class.java)
    }
}

private fun disableWarning() {
    System.err.close()
    System.setErr(System.out)
}

fun main(incomingArgs: Array<String>) {
    disableWarning()
    MyStravaStats(incomingArgs).run()
}






