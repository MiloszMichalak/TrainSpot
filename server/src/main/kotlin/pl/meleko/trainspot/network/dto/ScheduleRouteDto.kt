package pl.meleko.trainspot.network.dto

import kotlinx.serialization.Serializable
import pl.meleko.trainspot.model.ScheduleRoute

@Serializable
data class ScheduleRouteDto(
    val scheduleId: Int,
    val orderId: Long,
    val trainOrderId: Int,
    val name: String? = null,
    val carrierCode: String,
    val nationalNumber: String? = null,
    val commercialCategorySymbol: String,
    val originStation: StationDto? = null,
    val destStation: StationDto? = null,
    val arrivalTime: String? = null,
    val departureTime: String? = null
)

fun ScheduleRouteDto.toScheduleRoute() = ScheduleRoute(
    scheduleId = this.scheduleId,
    orderId = this.orderId,
    trainOrderId = this.trainOrderId,
    name = this.name,
    carrierCode = this.carrierCode,
    nationalNumber = this.nationalNumber,
    commercialCategorySymbol = this.commercialCategorySymbol,
    originStation = this.originStation?.toStation(),
    destStation = this.destStation?.toStation(),
    arrivalTime = this.arrivalTime,
    departureTime = this.departureTime
)