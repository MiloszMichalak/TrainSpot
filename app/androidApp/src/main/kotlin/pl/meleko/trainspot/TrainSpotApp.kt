package pl.meleko.trainspot

import android.app.Application
import org.koin.android.ext.koin.koinAndroidContext
import pl.meleko.trainspot.di.initKoin

class TrainSpotApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            koinAndroidContext(this@TrainSpotApp)
        }
    }
}
