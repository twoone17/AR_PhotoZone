package com.google.ar.core.examples.java.app.board

import java.io.Serializable


// 게시판에 필요한 데이터들을 가지는 DTO class
data class BoardData(
    val imgURL : String,
    var description : String,
    var likes : Long,
    val publisher : String,
    val userId : String,
    val documentId : String

    // 지형정보, 게시자 정보 등등 SNS 정보들 추가 예정
) : Serializable
