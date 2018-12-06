package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.hardcoded.UniversityData
import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.HibernateUtils
import de.ddkfm.plan4ba.utils.doInTransaction
import de.ddkfm.plan4ba.utils.toMillis
import io.swagger.annotations.*
import spark.Request
import spark.Response
import java.time.DayOfWeek
import java.time.LocalDate
import javax.ws.rs.*

@Api(value = "/universities", description = "all operations about universities")
@Path("/universities")
@Produces("application/json")
class UniversityController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @ApiOperation(value = "list all universities", notes = "return all universities")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = University::class, responseContainer = "List")
    )
    @ApiImplicitParam(name = "name", paramType = "query")
    @Path("")
    fun allUniversities(@ApiParam(hidden = true) name : String) : Any? = HibernateUtils.doInHibernate { session ->
        var where = "WHERE 1=1"
        if(!name.isEmpty())
            where += "AND name = '$name'"

        val univeristies = session.createQuery("From HibernateUniversity $where", HibernateUniversity::class.java)
                .list()
                .map { it.toUniversity() }
        univeristies
    }

    @GET
    @ApiOperation(value = "list all meals for current week", notes = "return all meals for current week")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Meal::class, responseContainer = "List"),
            ApiResponse(code = 404, response = NotFound::class, message = "University Not Found")
    )
    @ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    @Path("/:id/meals")
    fun getMealsFromUniversity(@ApiParam(hidden = true) id : Int) : Any? = HibernateUtils.doInHibernate { session ->
        val university = getUniversity(id)
        if(university is University) {
            var currentDay = LocalDate.now()
            val mealEntries = mutableListOf<Meal>()
            currentDay = when(currentDay.dayOfWeek) {
                DayOfWeek.SATURDAY -> currentDay.plusDays(2)
                DayOfWeek.SUNDAY -> currentDay.plusDays(1)
                else -> currentDay
            }
            while (currentDay.dayOfWeek < DayOfWeek.SATURDAY) {
                mealEntries.add(Meal(
                        universityId = university.id,
                        day = currentDay.toMillis(),
                        meals = UniversityData.getInstance(university.name).getMeals(currentDay)))
                currentDay = currentDay.plusDays(1)
            }
            mealEntries
        } else NotFound("university not found")
    }

    @GET
    @ApiOperation(value = "list all links for a university", notes = "return all links for a university")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Link::class, responseContainer = "List"),
            ApiResponse(code = 404, response = NotFound::class, message = "University Not Found")
    )
    @ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    @Path("/:id/links")
    fun getLinksFromUniversity(@ApiParam(hidden = true) id : Int) : Any? = HibernateUtils.doInHibernate { session ->
        val university = this.getUniversity(id)
        if(university is University) {
            val links = session.createQuery("From HibernateLink Where university_id = ${university.id}", HibernateLink::class.java).list()
            links.map { it.toLink() }
        } else
            NotFound("university not found")
    }

    @GET
    @ApiOperation(value = "return the geo locations for the university")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Meal::class, responseContainer = "List"),
            ApiResponse(code = 404, response = NotFound::class, message = "University Not Found")
    )
    @ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    @Path("/:id/location")
    fun getGeoInformation(@ApiParam(hidden = true) id : Int) : Any? = HibernateUtils.doInHibernate { session ->
        val university = getUniversity(id)
        if(university is University) {
            UniversityData.getInstance(university.name).getLocation()
        } else NotFound("university not found")
    }

    @GET
    @ApiOperation(value = "get a specific university", notes = "get a specific university")
    @ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = University::class),
            ApiResponse(code = 404, response = NotFound::class, message = "Not Found")
    )
    @Path("/:id")
    fun getUniversity(@ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var uni = session.find(HibernateUniversity::class.java, id)
            uni?.toUniversity() ?: NotFound()
        }
    }

    @PUT
    @ApiOperation(value = "create a university")
    @Path("")
    @ApiResponses(
            ApiResponse(code = 201, message = "university created", response = University::class),
            ApiResponse(code = 409, message = "university already exists", response = AlreadyExists::class),
            ApiResponse(code = 500, message = "Could not save the university", response = HttpStatus::class)
    )
    fun createUniversity(@ApiParam university : University) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var alreadyUnis = session.createQuery("From HibernateUniversity Where name = '${university.name}'",
                    HibernateUniversity::class.java)
                    .list()

            if(alreadyUnis.size > 0)
                AlreadyExists("university already exists")
            else {
                val hibernateUniversity = HibernateUniversity(0, university.name)
                try {
                    session.doInTransaction {
                        it.persist(hibernateUniversity)
                    }
                    hibernateUniversity.toUniversity()
                } catch (e : Exception) {
                    e.printStackTrace()
                    HttpStatus(500, "Could not save the university")
                }
            }
        }
    }

    @POST
    @ApiOperation(value = "updates an university")
    @Path("/:id")
    @ApiImplicitParams(
            ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    )
    @ApiResponses(
            ApiResponse(code = 200, message = "university updated", response = University::class),
            ApiResponse(code = 500, message = "Could not edit the university", response = HttpStatus::class)
    )
    fun updateUniversity(@ApiParam university : University,
                    @ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            val existingUniversity = session.find(HibernateUniversity::class.java, id)
            if(existingUniversity == null)
                NotFound("university does not exist")

            existingUniversity.name = university.name

            try {
                session.doInTransaction {
                    it.persist(existingUniversity)
                }
                existingUniversity.toUniversity()
            } catch (e: Exception) {
                e.printStackTrace()
                HttpStatus(500, "Could not update the Group")
            }
        }
    }
}

