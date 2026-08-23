package pl.meleko.trainspot.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CommercialCategoriesResponse(
    val generatedAt: String,
    val commercialCategories: List<CommercialCategoryDto>
)

@Serializable
data class CommercialCategoryDto(
    val code: String,
    val name: String,
    val carrierCode: String
)
