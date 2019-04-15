import de.ddkfm.plan4ba.main
import de.ddkfm.plan4ba.models.Infotext
import de.ddkfm.plan4ba.utils.toListModel
import de.ddkfm.plan4ba.utils.toModel
import kong.unirest.Unirest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class InfoTest {
    val endpoint = "http://localhost:8080"
    init {
        DBService.start()
    }
    @Test
    fun getAllInfos() {
        infos()
    }

    private fun infos() : List<Infotext>{
        val response = Unirest.get("$endpoint/info")
            .asString().getOrThrow().toListModel<Infotext>()
        println(response)
        assert(response != null && response.isNotEmpty())
        return response ?: throw Exception()
    }

    @Test
    fun getAllInfosByKey() {
        val infoTexts = infos()
        for(infoText in infoTexts) {
            val singleResp = Unirest.get("$endpoint/info?key=${infoText.key}")
                .asString().getOrThrow().toListModel<Infotext>()
            println(singleResp)
            assert(singleResp != null)
        }
    }

    @Test
    fun getInfosById() {
        val infoTexts = infos()
        for(infoText in infoTexts) {
            val singleResp = Unirest.get("$endpoint/info/${infoText.id}")
                .asString().getOrThrow().toModel<Infotext>()
            println(singleResp)
            assert(singleResp != null)
        }
    }
}