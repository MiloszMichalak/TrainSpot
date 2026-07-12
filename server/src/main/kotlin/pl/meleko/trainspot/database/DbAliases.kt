package pl.meleko.trainspot.database

import org.jetbrains.exposed.v1.core.alias

object DbAliases {
    val originStation = StationsTable.alias("origin_station")
    val destStation = StationsTable.alias("dest_station")
}
