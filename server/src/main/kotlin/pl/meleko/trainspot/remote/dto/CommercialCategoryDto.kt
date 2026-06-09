package pl.meleko.trainspot.remote.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommercialCategoriesResponseDto(
    val generatedAt: String,
    val commercialCategories: List<CommercialCategoryDto>
)

@Serializable
data class CommercialCategoryDto(
    val code: String,
    val name: String,
    val carrierCode: String?
)
