package pl.meleko.trainspot.database.entities

import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass
import pl.meleko.trainspot.database.StationsTable
import pl.meleko.trainspot.network.dto.StationDto

class StationEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<StationEntity>(StationsTable)

    var name by StationsTable.name
    var fetchedAt by StationsTable.fetchedAt

    fun toDto() = StationDto(
        id = id.value,
        name = name
    )
}
