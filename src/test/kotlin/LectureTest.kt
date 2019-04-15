import de.ddkfm.plan4ba.main
import de.ddkfm.plan4ba.models.Infotext
import de.ddkfm.plan4ba.models.Lecture
import de.ddkfm.plan4ba.models.OK
import de.ddkfm.plan4ba.utils.single
import de.ddkfm.plan4ba.utils.toJson
import de.ddkfm.plan4ba.utils.toListModel
import de.ddkfm.plan4ba.utils.toModel
import kong.unirest.Unirest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.random.Random
import kotlin.random.nextULong

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LectureTest {
    val endpoint = "http://localhost:8080"
    init {
        DBService.start()
    }

    @Test
    fun allLectures() {
        val response = Unirest.get("$endpoint/lectures")
            .asString().getOrThrow().toListModel<Lecture>()
        println(response)
        assert(response != null)
    }

    private fun lecturesByUserId() : List<Lecture> {
        val userId = 1
        val response = Unirest.get("$endpoint/lectures?userId=$userId")
            .asString().getOrThrow().toListModel<Lecture>()
        println(response)
        assert(response != null)
        return response!!
    }
    @Test
    fun allLecturesByUserId() {
        lecturesByUserId()
    }

    @Test
    fun getLectureById() {
        val lectures = lecturesByUserId()
        for(lecture in lectures) {
            val singleLecture = Unirest.get("$endpoint/lectures/${lecture.id}")
                .asString().getOrThrow().toModel<Lecture>()
            println(singleLecture)
        }
    }

    @Test
    fun deleteLectures() {
        val userId = 1;
        val response = Unirest.delete("$endpoint/lectures?userId=$userId")
            .asString().getOrThrow().toModel<OK>()
        assert(response != null && response.code == 200)
    }

    @Test
    fun createLectures() {
        val maxLectures = 100;
        for(i in 0 until maxLectures) {
            val lecture = Lecture(
                0,
                "title",
                Random.nextLong(0, 11),
                Random.nextLong(0, 11),
                Random.nextBoolean(),
                randomString(30),
                "efefef",
                randomString(),
                randomString(),
                randomString(35),
                randomString(),
                Random.nextBoolean(),
                1
            )
            val response = Unirest.put("$endpoint/lectures")
                .body(lecture.toJson())
                .asString()
            assert(response != null && response.status in listOf(200, 409))
            println(response.body)
        }
    }

    @Test
    fun updateLecture() {
        val lectures = lecturesByUserId()
        if(lectures.isEmpty())
            return
        for(i in 0..4) {
            val lecture = lectures.random()
            lecture.description += "test"
            lecture.start += 20
            lecture.instructor += "test"
            val response = Unirest.post("$endpoint/lectures/${lecture.id}")
                .body(lecture.toJson())
                .asString().getOrThrow()
            println(response)
        }
    }
}