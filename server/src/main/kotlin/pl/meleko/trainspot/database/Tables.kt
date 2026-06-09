package pl.meleko.trainspot.database

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object StationsTable : IntIdTable("stations") {
    val name = varchar("name", 100)
    val fetchedAt = timestamp("fetched_at").defaultExpression(CurrentTimestamp)
}

object CarriersTable : Table("carriers") {
    val code = varchar("code", 10).uniqueIndex()
    val name = varchar("name", 100)
    val validFrom = timestamp("valid_from").nullable()
    val validTo = timestamp("valid_to").nullable()

    override val primaryKey = PrimaryKey(code)
}

object CommercialCategoriesTable : Table("commercial_categories") {
    val code = varchar("code", 20).uniqueIndex()
    val name = varchar("name", 100).nullable()
    val carrierCode = varchar("carrier_code", 10).references(CarriersTable.code, onDelete = ReferenceOption.SET_NULL).nullable()

    override val primaryKey = PrimaryKey(code)
}

object UsersTable : IntIdTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val username = varchar("username", 100).uniqueIndex()
    val avatarUrl = varchar("avatar_url", 255).nullable()
    val bio = text("bio").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val sessionId = integer("session_id").references(SessionsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
}

object SessionsTable : IntIdTable("sessions") {
    val userId = integer("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val lastSeen = timestampWithTimeZone("last_seen").defaultExpression(CurrentTimestampWithTimeZone)
}

object TrainModelsTable : IntIdTable("train_models") {
    val model = varchar("model", 32)
    val numbers = varchar("numbers", 16)
    val carrierCode = varchar("carrier_code", 10).references(CarriersTable.code, onDelete = ReferenceOption.SET_NULL)
}

object SpotsTable : IntIdTable("spots") {
    val userId = integer("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val modelId = integer("model_id").references(TrainModelsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val stationId = integer("station_id").references(StationsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val trainNumber = varchar("train_number", 50).nullable()
    val trainRunId = integer("train_run_id").nullable()
    val imageUrl = varchar("image_url", 255)
    val description = text("description").nullable()
    val lat = double("lat").nullable()
    val lon = double("lon").nullable()
    val spottedAt = timestampWithTimeZone("spotted_at").defaultExpression(CurrentTimestampWithTimeZone)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object LikesTable : IntIdTable("likes") {
    val userId = integer("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val spotId = integer("spot_id").references(SpotsTable.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object ScheduleTable : IntIdTable("train_runs") {
    val scheduleId = integer("schedule_id").nullable()
    val trainOrderId = integer("train_order_id")
    val trainName = varchar("train_name", 32).nullable()
    val carrierCode = varchar("carrier_code", 10).references(CarriersTable.code, onDelete = ReferenceOption.SET_NULL).nullable()
    val trainNumber = varchar("train_number", 50).nullable()
    val catSymbol = varchar("cat_symbol", 10).nullable()
    val operatingDate = timestamp("operating_date")
    val internationalArrivalNumber = varchar("international_arrival_number", 50).nullable()
    val internationalDepartureNumber = varchar("international_departure_number", 50).nullable()
    val originStationId = integer("origin_station_id").references(StationsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val destStationId = integer("dest_station_id").references(StationsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
}

object TrainStopsTable : IntIdTable("train_stops") {
    val trainRunId = integer("train_run_id").references(ScheduleTable.id, onDelete = ReferenceOption.CASCADE)
    val stationId = integer("station_id").references(StationsTable.id, onDelete = ReferenceOption.CASCADE)
    val orderNumber = integer("order_number")
    val arrCat = varchar("arr_cat", 10).nullable()
    val arrTrainNum = varchar("arr_train_num", 50).nullable()
    val arrivalPlatform = varchar("arrival_platform", 20).nullable()
    val arrivalTrack = varchar("arrival_track", 20).nullable()
    val arrDayOffset = integer("arr_day_offset").nullable()
    val arrivalTime = timestamp("arrival_time").nullable()
    val depCat = varchar("dep_cat", 10).nullable()
    val depTrainNum = varchar("dep_train_num", 50).nullable()
    val platform = varchar("platform", 20).nullable()
    val track = varchar("track", 20).nullable()
    val departureTime = timestamp("departure_time").nullable()
    val depDayOffset = integer("dep_day_offset").nullable()
}
