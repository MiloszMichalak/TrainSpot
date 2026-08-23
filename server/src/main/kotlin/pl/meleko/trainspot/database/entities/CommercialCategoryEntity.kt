package pl.meleko.trainspot.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import pl.meleko.trainspot.database.CommercialCategoriesTable
import pl.meleko.trainspot.network.dto.CommercialCategoryDto

class CommercialCategoryEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CommercialCategoryEntity>(CommercialCategoriesTable)

    var code by CommercialCategoriesTable.code
    var name by CommercialCategoriesTable.name
    var carrier by CarrierEntity referencedOn CommercialCategoriesTable.carrierCode

    fun toDto() = CommercialCategoryDto(
        code = code,
        name = name,
        carrierCode = carrier.id.value
    )
}
