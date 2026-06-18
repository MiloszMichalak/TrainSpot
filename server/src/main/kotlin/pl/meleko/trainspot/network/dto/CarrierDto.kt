package pl.meleko.trainspot.network.dto

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.v1.core.ResultRow
import pl.meleko.trainspot.database.CarriersTable
import pl.meleko.trainspot.util.toDateString

@Serializable
data class CarriersResponse(
    val generatedAt: String,
    val carriers: List<CarrierDto>
)

@Serializable
data class CarrierDto(
    val code: String,
    val name: String,
    val validFrom: String,
    val validTo: String?
)

fun ResultRow.toCarrierDto() = CarrierDto(
    code = this[CarriersTable.code],
    name = this[CarriersTable.name],
    validFrom = this[CarriersTable.validFrom].toDateString(),
    validTo = this[CarriersTable.validTo].toDateString()
)