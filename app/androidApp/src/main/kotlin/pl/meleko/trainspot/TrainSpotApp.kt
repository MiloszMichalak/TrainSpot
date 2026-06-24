package pl.meleko.trainspot

import android.app.Application
import org.koin.android.ext.koin.androidContext
import pl.meleko.trainspot.di.initKoin

class TrainSpotApp : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@TrainSpotApp)
        }
    }
}
