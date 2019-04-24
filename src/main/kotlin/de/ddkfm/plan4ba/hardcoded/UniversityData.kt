package de.ddkfm.plan4ba.hardcoded

import de.ddkfm.plan4ba.models.Food
import kong.unirest.Unirest
import org.json.JSONObject
import org.jsoup.Jsoup
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.absoluteValue

open class UniversityData {
    var lastTimeCached = mutableMapOf<LocalDate, LocalDateTime>()
    open var cache : MutableMap<LocalDate, List<Food>> = mutableMapOf()
    open var accentColor : String = "#009ee3"
    open var logo : String = "https://www.ba-sachsen.de/fileadmin/tmpl/daten/berufsakademie_sachsen/img/logo/ba_sachsen_logo.svg"
    open protected fun cacheMeal(day : LocalDate) : List<Food> = emptyList()
    fun getMeals(day : LocalDate) : List<Food> {
        val cachedMeals = cache[day]
        val lastUpdated = lastTimeCached[day]
        val diff = lastUpdated?.until(LocalDateTime.now(), ChronoUnit.HOURS)?.absoluteValue ?: 42//random high number as default value
        if(cachedMeals == null || diff > 12) {
            val new = this.cacheMeal(day)
            cache[day] = new
            lastTimeCached[day] = LocalDateTime.now()
            return new
        }
        return cachedMeals
    }
    companion object {
        @JvmStatic
        fun getInstance(uni : String) : UniversityData = when(uni) {
            "Staatliche Studienakademie Leipzig" -> BaLeipzig
            "Staatliche Studienakademie Dresden" -> BaDresden
            "Staatliche Studienakademie Breitenbrunn" -> BaBreitenbrunn
            "Staatliche Studienakademie Plauen" -> BaPlauen
            else -> UniversityData()
        }
    }

    fun cacheMealFromOpenMensa(id : Int, day : LocalDate) : List<Food> {
        val dateString = "%d-%02d-%02d".format(day.year, day.monthValue, day.dayOfMonth)
        val response = Unirest.get("https://openmensa.org/api/v2/canteens/$id/days/$dateString/meals")
            .asJson().body.array
        return response.map { meal ->
            meal as JSONObject
            val prices = meal.getJSONObject("prices")
            var pricesString = "${prices.getDoubleOrDefault("students", 0.0)}€,"
            pricesString += "${prices.getDoubleOrDefault("employees", 0.0)}€,"
            pricesString += "${prices.getDoubleOrDefault("pupils", 0.0)}€,"
            pricesString += "${prices.getDoubleOrDefault("others", 0.0)}€,"
            val vegetarian = meal.has("notes") && meal.getJSONArray("notes").contains("vegetarisch")
            val vegan = meal.has("notes") && meal.getJSONArray("notes").contains("vegan")
            Food(
                meal.getString("name"),
                pricesString,
                vegetarian,
                vegan,
                meal.getString("category")
            )
        }
    }
}

fun JSONObject.getDoubleOrDefault(field : String, default : Double) : Double {
    return if(this.isNull(field))
        default
    else
        this.getDouble(field);
}
object BaLeipzig : UniversityData() {
    override var accentColor: String = "#309D4A"
    override var logo: String = "https://www.ba-leipzig.de/fileadmin/tmpl/daten/berufsakademie_sachsen/img/logo/ba_leipzig_logo.svg"
    override fun cacheMeal(day: LocalDate): List<Food> {

        val date = "%d-%02d-%02d".format(day.year, day.monthValue, day.dayOfMonth)
        val url = "https://www.studentenwerk-leipzig.de/mensen-cafeterien/speiseplan?location=140&date=$date&criteria=&meal_type=all"
        val doc = Jsoup.parse(URL(url), 5000)
        val meals = doc.getElementsByClass("meals__head")
        return meals
                .map { element ->
                    Food(
                            description = {
                                val mainTitle = element.select("h4").html()
                                val details = element.parent().parent().select("details")
                                val subTitles = details.select(".u-list-bare li").map { it.html() }
                                (listOf(mainTitle) + subTitles ).joinToString(separator = "\n")
                            }.invoke(),
                            prices = element.select(".meals__price").text().replace("Preise:", ""),
                            vegetarian = element.select("i.meals__badge[data-icon=\"vegan\"]").isNotEmpty(),
                            vegan = element.select("i.meals__badge[data-icon=\"cheese\"]").isNotEmpty(),
                            additionalInformation = ""
                    )
        }
    }
}

object BaDresden : UniversityData() {
    override var logo : String = ""
    override fun cacheMeal(day: LocalDate): List<Food> {
        return cacheMealFromOpenMensa(87, day)
    }
}

object BaPlauen : UniversityData() {
    override var accentColor: String = "#0171bf"
    override var logo: String = "https://www.ba-plauen.de/images/bilder_neu/header_logo_ba.gif"
}

object BaBreitenbrunn : UniversityData() {
    override var accentColor : String = "#E60000"
    override var logo : String = "http://www.ba-breitenbrunn.de/typo3temp/pics/43ebf57fb9.jpg"
}