package pl.meleko.trainspot.network.dto

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ResultRow
import pl.meleko.trainspot.database.CommercialCategoriesTable

@Serializable
data class CommercialCategoriesResponse(
    val generatedAt: String,
    val commercialCategories: List<CommercialCategoryDto>
)

@Serializable
data class CommercialCategoryDto(
    val code: String,
    val name: String,
    val carrierCode: String?
)

fun ResultRow.toCommercialCategoryDto() = CommercialCategoryDto(
    code = this[CommercialCategoriesTable.code],
    name = this[CommercialCategoriesTable.name].orEmpty(),
    carrierCode = this[CommercialCategoriesTable.carrierCode]
)
