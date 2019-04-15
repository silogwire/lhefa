import de.ddkfm.plan4ba.models.Lecture
import de.ddkfm.plan4ba.models.Meal
import de.ddkfm.plan4ba.models.Token
import de.ddkfm.plan4ba.models.University
import de.ddkfm.plan4ba.utils.toJson
import de.ddkfm.plan4ba.utils.toListModel
import de.ddkfm.plan4ba.utils.toModel
import kong.unirest.Unirest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UniversityTest {
    val endpoint = "http://localhost:8080"
    init {
        DBService.start()
    }

    @Test
    fun allUniversities() {
        val response = Unirest.get("$endpoint/universities")
            .asString().getOrThrow().toListModel<University>()
        println(response)
        assert(response != null)
    }

    @Test
    fun getAllSingleUniversities() {
        val universities = Unirest.get("$endpoint/universities")
            .asString().getOrThrow().toListModel<University>()
        if(universities == null)
            return
        for(uni in universities) {
            val university = Unirest.get("$endpoint/universities/${uni.id}")
                .asString().getOrThrow().toModel<University>()
            assert(university != null)
        }
    }

    @Test
    fun getAllMealsFromUniversities() {
        val universities = Unirest.get("$endpoint/universities")
            .asString().getOrThrow().toListModel<University>()
        if(universities == null)
            return
        for(uni in universities) {
            val meals = Unirest.get("$endpoint/universities/${uni.id}/meals")
                .asString().getOrThrow().toListModel<Meal>()
            println(meals)
            assert(meals != null)
        }
    }

    @Test
    fun createUniversity() {
        val uni = University(0, "Staatliche Studienakademie Leipzig", "", "")
        val response = Unirest.put("$endpoint/universities")
            .body(uni.toJson())
            .asString()
        println(response.body)
        when(response.status) {
            200 -> assertNotNull(response.body.toModel<University>())
            409 -> assert(response.body.contains("university already exists"))
            else -> response.getOrThrow()
        }
    }

}