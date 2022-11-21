package com.google.ar.core.examples.java.app.board.DTO
import java.io.Serializable

/**
 * 1. Storage에 있는 img url
 * 2. img만 필수, 나머지는 ""로 선언해줘서 데이터 생성할때 없어도 된다 (Builder 패턴과 같이 적용)
 */
data class UploadData(
        val img: Int,
        val description: String? ="",
        val userId: String? = "",
) : Serializable