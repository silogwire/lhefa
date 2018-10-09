package de.ddkfm.plan4ba.models

data class User(
        var id: Int,
        var matriculationNumber: String,
        var userHash : String?,
        var password : String,
        var lastLogin : Long?,
        var groupId : Int,
        var storeUserHash : Boolean,
        var hasUserSpecificCalendar : Boolean
)

data class UserGroup(
        var id : Int,
        var uid : String,
        var universityId : Int
)

data class University(
        var id : Int,
        var name : String
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
        var userId : Int?,
        var groupId : Int?
)