package pl.meleko.trainspot.database

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.datetime
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object StationsTable : IntIdTable("stations") {
    val name = varchar("name", 100)
    val fetchedAt = timestamp("fetched_at").defaultExpression(CurrentTimestamp)
}

object CarriersTable : Table("carriers") {
    val code = varchar("code", 10).uniqueIndex()
    val name = varchar("name", 100)
    val validFrom = date("valid_from").nullable()
    val validTo = date("valid_to").nullable()

    override val primaryKey = PrimaryKey(code)
}

object CommercialCategoriesTable : Table("commercial_categories") {
    val code = varchar("code", 20).uniqueIndex()
    val name = varchar("name", 100).nullable()
    val carrierCode = varchar("carrier_code", 10).references(CarriersTable.code, onDelete = ReferenceOption.SET_NULL).nullable()
    val speedCategory = varchar("speed_category", 20).nullable()

    override val primaryKey = PrimaryKey(code)
}

object UsersTable : IntIdTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val username = varchar("username", 100).uniqueIndex()
    val avatarUrl = varchar("avatar_url", 255).nullable()
    val bio = text("bio").nullable()
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val sessionId = integer("session_id").references(SessionsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val spotsId = integer("spots_id").references(SpotsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val likesId = integer("likes_id").references(LikesTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
}

object SessionsTable : IntIdTable("sessions") {
    val userId = integer("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val lastSeen = timestampWithTimeZone("last_seen").defaultExpression(CurrentTimestampWithTimeZone)
}

object TrainModelsTable : IntIdTable("train_models") {
    val name = varchar("name", 100)
    val series = varchar("series", 50).nullable()
    val traction = varchar("traction", 20).nullable()
    val carrierCode = varchar("carrier_code", 10).references(CarriersTable.code, onDelete = ReferenceOption.SET_NULL)
    val spotId = integer("spot_id").references(SpotsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
}

object SpotsTable : IntIdTable("spots") {
    val userId = integer("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val modelId = integer("model_id").references(TrainModelsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val stationId = integer("station_id").references(StationsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val trainNumber = varchar("train_number", 50).nullable()
    val trainRunId = integer("train_run_id").nullable()
    val imageUrl = varchar("image_url", 255)
    val caption = text("caption").nullable()
    val lat = double("lat").nullable()
    val lon = double("lon").nullable()
    val spottedAt = timestampWithTimeZone("spotted_at").defaultExpression(CurrentTimestampWithTimeZone)
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
}

object LikesTable : IntIdTable("likes") {
    val userId = integer("user_id").references(UsersTable.id, onDelete = ReferenceOption.CASCADE)
    val spotId = integer("spot_id").references(SpotsTable.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
}

object TrainRunsTable : IntIdTable("train_runs") {
    val trainOrderId = integer("train_order_id")
    val scheduleId = integer("schedule_id").nullable()
    val carrierCode = varchar("carrier_code", 10).references(CarriersTable.code, onDelete = ReferenceOption.SET_NULL).nullable()
    val trainNumber = varchar("train_number", 50).nullable()
    val trainName = varchar("train_name", 100).nullable()
    val catSymbol = varchar("cat_symbol", 10).nullable()
    val operatingDate = date("operating_date")
    val originStationId = integer("origin_station_id").references(StationsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val destStationId = integer("dest_station_id").references(StationsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val stopsId = integer("stops_id").references(TrainStopsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
    val spotsId = integer("spots_id").references(SpotsTable.id, onDelete = ReferenceOption.SET_NULL).nullable()
}

object TrainStopsTable : IntIdTable("train_stops") {
    val trainRunId = integer("train_run_id").references(TrainRunsTable.id, onDelete = ReferenceOption.CASCADE)
    val stationId = integer("station_id").references(StationsTable.id, onDelete = ReferenceOption.CASCADE)
    val orderNumber = integer("order_number")
    val arrivalTime = datetime("arrival_time").nullable()
    val departureTime = datetime("departure_time").nullable()
    val arrDayOffset = integer("arr_day_offset").nullable()
    val depDayOffset = integer("dep_day_offset").nullable()
    val platform = varchar("platform", 20).nullable()
    val track = varchar("track", 20).nullable()
    val arrCat = varchar("arr_cat", 10).nullable()
    val depCat = varchar("dep_cat", 10).nullable()
    val arrTrainNum = varchar("arr_train_num", 50).nullable()
    val depTrainNum = varchar("dep_train_num", 50).nullable()
}
