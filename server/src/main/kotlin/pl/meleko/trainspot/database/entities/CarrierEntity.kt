package pl.meleko.trainspot.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.Entity
import org.jetbrains.exposed.v1.dao.EntityClass
import pl.meleko.trainspot.database.CarriersTable
import pl.meleko.trainspot.network.dto.CarrierDto
import pl.meleko.trainspot.util.toDateString

class CarrierEntity(id: EntityID<String>) : Entity<String>(id) {
    companion object : EntityClass<String, CarrierEntity>(CarriersTable)

    var name by CarriersTable.name
    var validFrom by CarriersTable.validFrom
    var validTo by CarriersTable.validTo

    fun toDto() = CarrierDto(
        code = id.value,
        name = name,
        validFrom = validFrom.toDateString(),
        validTo = validTo?.toDateString()
    )
}
