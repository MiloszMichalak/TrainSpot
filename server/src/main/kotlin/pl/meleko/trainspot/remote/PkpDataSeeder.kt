package pl.meleko.trainspot.remote

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import pl.meleko.trainspot.remote.dto.SchedulesResponseDto

class PkpDataSeeder(
    private val pkpApiClient: PkpApiClient
) {
    suspend fun seedAll() {
        println("\n[1/3] Downloading dictionaries...")
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
        println("Fetching schedules...")
        val schedules = pkpApiClient.fetchSchedules()

        println("Found ${schedules.routes.size} schedules")
        return schedules
    }

    private suspend fun uploadDictionariesToDatabase(){
        PkpImportRepository.clearDictionariesData()

        val carriers = pkpApiClient.fetchCarriers()
        val commercialCategories = pkpApiClient.fetchCommercialCategories()
        val stations = pkpApiClient.fetchStations()

        PkpImportRepository.importCarriersData(carriers)
        PkpImportRepository.importCommercialCategories(commercialCategories)
        PkpImportRepository.importStations(stations)
    }

    private fun uploadScheduleToDatabase(schedules: SchedulesResponseDto) {
        transaction {
            println("Importing train runs...")
            schedules.routes.forEach { schedule ->
                PkpImportRepository.importSchedule(schedule)

                PkpImportRepository.importTrainStops(schedule.trainOrderId, schedule.stations)

                println("Added train run: ${schedule.name} (${schedule.nationalNumber})")
            }

            println("Data uploaded successfully")
        }
    }
}
