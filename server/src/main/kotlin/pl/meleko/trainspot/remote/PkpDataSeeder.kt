package pl.meleko.trainspot.remote

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import pl.meleko.trainspot.remote.dto.SchedulesResponseDto

object PkpDataSeeder {
    suspend fun seedAll() {
        println("=".repeat(60))
        println("Starting PKP PLK API data seeding")
        println("=".repeat(60))

        println("\n[1/2] Downloading dictionaries...")
        uploadDictionariesToDatabase()

        println("\n[2/3] Downloading schedule...")

        val schedulesResponse = downloadSchedules()

        println("\n[3/3] Uploading all data to database...")
        uploadScheduleToDatabase(schedulesResponse)

        println("\n" + "=".repeat(60))
        println("Seeding completed successfully!")
        println("=".repeat(60))
    }

    private suspend fun downloadSchedules(): SchedulesResponseDto {
        println("  Fetching schedules (includes dictionaries)...")
        val schedules = PkpApiClient.fetchSchedules()

        println("    Found ${schedules.routes.size} schedules")
        return schedules
    }

    private suspend fun uploadDictionariesToDatabase(){
        PkpImportRepository.clearDictionariesData()

        val carriers = PkpApiClient.fetchCarriers()
        val commercialCategories = PkpApiClient.fetchCommercialCategories()
        val stations = PkpApiClient.fetchStations()

        PkpImportRepository.importCarriersData(carriers)
        PkpImportRepository.importCommercialCategories(commercialCategories)
        PkpImportRepository.importStations(stations)
    }

    private fun uploadScheduleToDatabase(schedules: SchedulesResponseDto) {
        transaction {
            println("  Importing train runs...")
            PkpImportRepository.importSchedules(schedules.routes)

            schedules.routes.forEach { schedule ->
                PkpImportRepository.importTrainStops(schedule.stations)

                println("    Added train run: ${schedule.name} (${schedule.nationalNumber})")
            }

            println("  Data uploaded successfully")
        }
    }
}
