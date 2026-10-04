package pl.meleko.trainspot.database

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.dao.id.UuidTable
import org.jetbrains.exposed.v1.datetime.CurrentTimestamp
import org.jetbrains.exposed.v1.datetime.CurrentTimestampWithTimeZone
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.time
import org.jetbrains.exposed.v1.datetime.timestamp
import org.jetbrains.exposed.v1.datetime.timestampWithTimeZone

object StationsTable : IntIdTable("stations") {
    val name = varchar("name", 100)
    val latitude = double("latitude").default(0.0)
    val longitude = double("longitude").default(0.0)
    val fetchedAt = timestamp("fetched_at").defaultExpression(CurrentTimestamp)
}

object CarriersTable : IdTable<String>("carriers") {
    val code = varchar("code", 16)
    val name = varchar("name", 100)
    val validFrom = timestamp("valid_from").nullable()
    val validTo = timestamp("valid_to").nullable()

    override val id = code.entityId()
    override val primaryKey = PrimaryKey(id)
}

object CommercialCategoriesTable : IntIdTable("commercial_categories") {
    val code = varchar("code", 20)
    val name = varchar("name", 100)
    val carrierCode = reference("carrier_code", CarriersTable, onDelete = ReferenceOption.SET_NULL)
}

object UsersTable : UuidTable("users") {
    val email = varchar("email", 255).uniqueIndex()
    val passwordHash = varchar("password_hash", 255)
    val username = varchar("username", 100).uniqueIndex().nullable()
    val avatarUrl = varchar("avatar_url", 255).nullable()
    val bio = text("bio").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object SessionsTable : UuidTable("sessions") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val lastSeen = timestampWithTimeZone("last_seen").defaultExpression(CurrentTimestampWithTimeZone)
}

object TrainTypesTable : UuidTable("train_type") {
    val name = varchar("name", 32).uniqueIndex("train_type_name_unique")
}

object TrainVehiclesTable : UuidTable("train_vehicles") {
    val typeId = reference("type_id", TrainTypesTable, onDelete = ReferenceOption.RESTRICT).index()
    val model = varchar("model", 16).default("")
    val isVerified = bool("is_verified").default(false)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object TrainModelsTable : UuidTable("train_models") {
    val vehicle = reference("vehicle", TrainVehiclesTable, onDelete = ReferenceOption.RESTRICT).index()
    val number = varchar("number", 16)
    val carrierCode = reference("carrier_code", CarriersTable, onDelete = ReferenceOption.RESTRICT)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object SpotsTable : UuidTable("spots") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val modelId = reference("model_id", TrainModelsTable, onDelete = ReferenceOption.CASCADE)
    val stationId = optReference("station_id", StationsTable, onDelete = ReferenceOption.SET_NULL)
    val trainRunId = reference("train_run_id", ScheduleTable, onDelete = ReferenceOption.CASCADE)
    val imageUrl = varchar("image_url", 255)
    val description = text("description").nullable()
    val lat = double("lat").nullable()
    val lon = double("lon").nullable()
    val spottedAt = timestampWithTimeZone("spotted_at").defaultExpression(CurrentTimestampWithTimeZone)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object LikesTable : UuidTable("likes") {
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val spotId = reference("spot_id", SpotsTable, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

object CommentsTable : UuidTable("comments") {
    val spotId = reference("spot_id", SpotsTable, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val text = text("text")
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)
    val updatedAt = timestampWithTimeZone("updated_at").nullable()
}

object CommentLikesTable : Table("comment_likes") {
    val commentId = reference("comment_id", CommentsTable, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_id", UsersTable, onDelete = ReferenceOption.CASCADE)
    val createdAt = timestampWithTimeZone("created_at").defaultExpression(CurrentTimestampWithTimeZone)

    override val primaryKey = PrimaryKey(commentId, userId)
}

object ScheduleTable : IdTable<Int>("train_runs") {
    val trainOrderId = integer("train_order_id")
    val scheduleId = integer("schedule_id")
    val trainName = varchar("train_name", 32).nullable()
    val carrierCode = reference("carrier_code", CarriersTable, onDelete = ReferenceOption.CASCADE)
    val trainNumber = varchar("train_number", 50).nullable()
    val catSymbol = varchar("cat_symbol", 16)
    val operatingDate = date("operating_date")
    val internationalArrivalNumber = varchar("international_arrival_number", 50).nullable()
    val internationalDepartureNumber = varchar("international_departure_number", 50).nullable()
    val originStationId = reference("origin_station_id", StationsTable, onDelete = ReferenceOption.CASCADE)
    val destStationId = reference("dest_station_id", StationsTable, onDelete = ReferenceOption.CASCADE)

    override val id = trainOrderId.entityId()
    override val primaryKey = PrimaryKey(id)
}

object TrainStopsTable : IntIdTable("train_stops") {
    val trainRunId = reference("train_run_id", ScheduleTable, onDelete = ReferenceOption.CASCADE)
    val stationId = reference("station_id", StationsTable, onDelete = ReferenceOption.CASCADE)
    val orderNumber = integer("order_number")
    val arrCat = varchar("arr_cat", 10).nullable()
    val arrTrainNum = varchar("arr_train_num", 50).nullable()
    val arrivalPlatform = varchar("arrival_platform", 20).nullable()
    val arrivalTrack = varchar("arrival_track", 20).nullable()
    val arrDayOffset = integer("arr_day_offset").nullable()
    val arrivalTime = time("arrival_time").nullable()
    val depCat = varchar("dep_cat", 10).nullable()
    val depTrainNum = varchar("dep_train_num", 50).nullable()
    val platform = varchar("platform", 20).nullable()
    val track = varchar("track", 20).nullable()
    val departureTime = time("departure_time").nullable()
    val depDayOffset = integer("dep_day_offset").nullable()
}
