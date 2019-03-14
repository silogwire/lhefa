package de.ddkfm.plan4ba.hardcoded

import de.ddkfm.plan4ba.models.Food
import de.ddkfm.plan4ba.models.Geo
import org.jsoup.Jsoup
import java.net.URL
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
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
    open fun getLocation() : Geo = Geo(0.0, 0.0)
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
                            description = element.select("h4").html(),
                            prices = element.select(".meals__price").text().replace("Preise:", ""),
                            vegetarian = element.select("i.meals__badge[data-icon=\"vegan\"]").isNotEmpty(),
                            vegan = element.select("i.meals__badge[data-icon=\"cheese\"]").isNotEmpty(),
                            additionalInformation = ""
                    )
        }
    }

    override fun getLocation(): Geo = Geo(51.310394, 12.303310)
}

object BaDresden : UniversityData() {
    override var logo : String = ""
    override fun cacheMeal(day: LocalDate): List<Food> {
        val now = LocalDate.now()
        val beginOfWeek = now.minusDays(now.dayOfWeek.value - 1L)
        val endOfWorkWeek = now.plusDays(5L - now.dayOfWeek.value)
        val week = if(day in beginOfWeek..endOfWorkWeek) "" else "-w1"
        val url = "https://www.studentenwerk-dresden.de/mensen/speiseplan/mensa-johannstadt.html$week"
        val doc = Jsoup.parse(URL(url), 5000)
        val meals = doc.getElementsByTag("article")
        val dayFormatted = day.format(DateTimeFormatter.ofPattern("EEEE, d. MMMM yyyy", Locale.GERMAN))
        return meals
                .filter { it.select(".swdd-ueberschrift").html() == dayFormatted }
                .map { element ->
                    element.select(".swiper-slide").map { swiper ->
                        val description = swiper.select(".flex-grow-1").text()
                        val prices = swiper.select("strong").html()
                        val vegetarian = swiper.select("img[alt=\"vegetarisch\"]").isNotEmpty()
                        val vegan = swiper.select("img[alt=\"vegan\"]").isNotEmpty()
                        if(description.isNullOrEmpty() ) null
                        else Food(description, prices, vegetarian || vegan, vegan, "")
                    }
                }
                .flatten()
                .filterNotNull()
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