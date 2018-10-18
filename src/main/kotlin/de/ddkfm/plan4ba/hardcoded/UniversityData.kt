package de.ddkfm.plan4ba.hardcoded

import de.ddkfm.plan4ba.models.Food
import de.ddkfm.plan4ba.models.Meal
import de.ddkfm.plan4ba.utils.toMillis
import org.jsoup.Jsoup
import java.net.URL
import java.time.LocalDate

open class UniversityData {
    open var accentColor : String = "#009ee3"
    open var logo : String = "https://www.ba-sachsen.de/fileadmin/tmpl/daten/berufsakademie_sachsen/img/logo/ba_sachsen_logo.svg"
    open fun getMeals(day : LocalDate) : List<Food> = emptyList()
    companion object {
        @JvmStatic
        fun getInstance(uni : String) : UniversityData = when(uni) {
            "Staatliche Studienakademie Leipzig" -> BaLeipzig()
            "Staatliche Studienakademie Dresden" -> BaDresden()
            "Staatliche Studienakademie Breitenbrunn" -> BaBreitenbrunn()
            "Staatliche Studienakademie Plauen" -> BaPlauen()
            else -> UniversityData()
        }
    }
}

class BaLeipzig : UniversityData() {
    override var accentColor: String = "#309D4A"
    override var logo: String = "http://www.logistik-leipzig-halle.net/wp-content/uploads/2017/02/berufsakademie_logo_neu.jpg"
    override fun getMeals(day: LocalDate): List<Food> {
        val date = "${day.year}-${day.monthValue}-${day.dayOfMonth}"
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
}

class BaDresden : UniversityData() {
    override var logo : String = ""
}

class BaPlauen : UniversityData() {
    override var accentColor: String = "#0171bf"
    override var logo: String = "https://www.ba-plauen.de/images/bilder_neu/header_logo_ba.gif"
}

class BaBreitenbrunn : UniversityData() {
    override var accentColor : String = "#E60000"
    override var logo : String = "http://www.ba-breitenbrunn.de/typo3temp/pics/43ebf57fb9.jpg"
}