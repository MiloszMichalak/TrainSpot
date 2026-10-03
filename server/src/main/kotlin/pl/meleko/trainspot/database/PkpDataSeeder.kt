package pl.meleko.trainspot.database

import pl.meleko.trainspot.network.PkpApiClient
import pl.meleko.trainspot.network.dto.SchedulesResponse
import pl.meleko.trainspot.repository.PkpImportRepository

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

    private suspend fun downloadSchedules(): SchedulesResponse {
        println("Fetching schedules...")
        val schedules = pkpApiClient.fetchSchedules()

        println("Found ${schedules.routes.size} schedules")
        return schedules
    }

    private suspend fun uploadDictionariesToDatabase(){
        val carriers = pkpApiClient.fetchCarriers()
        val commercialCategories = pkpApiClient.fetchCommercialCategories()
        val stations = pkpApiClient.fetchStations()

        val stationsWithGeolocation = PkpImportRepository.withStationGeolocation(stations)

        PkpImportRepository.clearDictionariesData()
        PkpImportRepository.importCarriersData(carriers)
        PkpImportRepository.importCommercialCategories(commercialCategories)
        PkpImportRepository.importStations(stationsWithGeolocation)
    }

    private suspend fun uploadScheduleToDatabase(schedules: SchedulesResponse) {
        println("Importing train runs...")
        schedules.routes.forEach { schedule ->
            PkpImportRepository.importSchedule(schedule)

            PkpImportRepository.importTrainStops(schedule.trainOrderId, schedule.stations)

            println("Added train run: ${schedule.name} (${schedule.nationalNumber})")
        }

        println("Data uploaded successfully")
    }
}