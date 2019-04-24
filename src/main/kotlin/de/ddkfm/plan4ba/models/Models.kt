package de.ddkfm.plan4ba.models

import javax.persistence.*

data class User(
        var id: Int,
        var matriculationNumber: String,
        var userHash : String?,
        var password : String,
        var groupId : Int,
        var lastLecturePolling : Long,
        var lastLectureCall : Long
)


data class Token(
        var token : String,
        var userId : Int,
        var isCalDavToken : Boolean,
        var validTo : Long,
        var isRefreshToken : Boolean
)

data class UserGroup(
        var id : Int,
        var uid : String,
        var universityId : Int
)

data class University(
        var id : Int,
        var name : String,
        var accentColor : String,
        var logoUrl : String
)

data class Meal(
        var universityId: Int,
        var day : Long,
        var meals : List<Food>

)

data class Food(
        var description: String,
        var prices : String,
        var vegetarian : Boolean,
        var vegan : Boolean,
        var additionalInformation : String
)

data class Lecture(
        var id : Int,
        var title : String,
        var start : Long,
        var end : Long,
        var allDay : Boolean,
        var description : String,
        var color : String,
        var room : String,
        var sroom : String,
        var instructor : String,
        var remarks : String,
        var exam : Boolean,
        var userId : Int
)

data class Infotext(
        var id : Int,
        var key : String,
        var description: String,
        var language : String
)

data class Notification(
        var id : Int,
        var label : String,
        var description : String,
        var type : String,
        var userId : Int
)

data class Link(
        var id : Int,
        var label : String,
        var url : String,
        var universityId : Int,
        var groupId : Int,
        var language : String
)

data class ExamStats(
        var id : Int,
        var userId : Int,
        var booked : Int,
        var exams : Int,
        var failure : Int,
        var mbooked : Int,
        var modules : Int,
        var success : Int
)
