package com.google.ar.core.examples.java.app.board.DTO

import java.io.Serializable


// 게시판에 필요한 데이터들을 가지는 DTO class
data class BoardData(
    val img : String,
    val description : String,
    val likes : Int,
    val publisher : String,
    val userId : String

    // 지형정보, 게시자 정보 등등 SNS 정보들 추가 예정
) : Serializable
