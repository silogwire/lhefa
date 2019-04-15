package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.hardcoded.UniversityData
import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.*
import spark.Request
import spark.Response
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.stream.Collectors
import javax.ws.rs.*

@Path("/universities")
class UniversityController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun allUniversities(@QueryParam("name") name : String) : List<University>? {
        val where = if(name.isNotEmpty()) "name = '$name'" else "1=1"
        val universities = inSession { it.list<HibernateUniversity>(where) }
        return universities?.map { it.toUniversity() }
    }

    @GET
    @Path("/:id")
    fun getUniversity(@PathParam("id") id : Int) : University? {
        val uni = inSession { it.single<HibernateUniversity>(id) }
        if(uni == null)
            throw NotFound()
        return uni.toUniversity()
    }

    @GET
    @Path("/:id/meals")
    fun getMealsFromUniversity(@PathParam("id") id : Int) : List<Meal>? {
        val university = getUniversity(id)
        if(university == null)
            throw NotFound()
        val currentDay = LocalDate.now().let {day ->
            when(day.dayOfWeek) {
                DayOfWeek.SATURDAY -> day.plusDays(2)
                DayOfWeek.SUNDAY -> day.plusDays(1)
                else -> day
            }
        }
        val mealEntries = mutableListOf<Meal>()
        val endOfWeek = currentDay.with(DayOfWeek.SATURDAY)
        val meals = currentDay.datesUntil(endOfWeek)
            .map { day -> Meal(
                universityId = university.id,
                day = day.toMillis(),
                meals = UniversityData.getInstance(university.name).getMeals(day)
            ) }
            .collect(Collectors.toList())
        return meals
    }

    @PUT
    @Path("")
    fun createUniversity(university : University) : University {
        val alreadyExists = inSession { it.list<HibernateUniversity>("name = '${university.name}'") }?.firstOrNull()
        if(alreadyExists != null)
            throw AlreadyExists("university already exists")
        val hibernateUniversity = HibernateUniversity(0, university.name)
        inSession { session ->
            session save hibernateUniversity
        }
        return hibernateUniversity.toUniversity()
    }
}

