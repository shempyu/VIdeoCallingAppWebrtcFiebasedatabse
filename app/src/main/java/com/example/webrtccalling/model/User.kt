package com.example.webrtccalling.model

class User {
    var name: String?=null
    var email:String?=null
    var password:String?= null
    var token: String? = null
    var status: String? = null
    var uid : String? = null

    constructor()
    constructor(name: String?, email: String?, password: String?) {
        this.name = name
        this.email = email
        this.password = password
    }


    constructor(email: String?, password: String?) {
        this.email = email
        this.password = password
    }

    constructor(
        name: String?,
        email: String?,
        password: String?,
        token: String?,
        status: String?,
        uid: String?
    ) {
        this.name = name
        this.email = email
        this.password = password
        this.token = token
        this.status = status
        this.uid = uid
    }


}