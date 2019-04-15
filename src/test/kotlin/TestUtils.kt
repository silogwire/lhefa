import de.ddkfm.plan4ba.main
import kong.unirest.HttpResponse
import java.util.*


fun <T> HttpResponse<T>.getOrThrow() : T {
    if(this.status == 200)
        return this.body
    else
        throw Exception(this.body.toString())
}
fun randomString(length : Int = 10) : String {
    val chars = ('A'..'Z').joinToString(separator = "")
    return (0 until length).map { chars[Random().nextInt(chars.length)] }.joinToString(separator = "")
}
object DBService {
    var started = false

    init {
        main(emptyArray())
        started = true
    }

    fun start() {
        if (!started) {
            main(emptyArray())
            started = false
        }
    }
}