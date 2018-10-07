package de.ddkfm.plan4ba.models

data class User(
        var id: Int,
        var matriculationNumber: String,
        var userHash : String?,
        var password : String,
        var lastLogin : Long?,
        var groupId : Int
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