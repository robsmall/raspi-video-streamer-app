package robsmall.raspivideostreamer

import android.app.Application
import timber.log.Timber

class RasPiVideoStreamerApplication : Application() {

  override fun onCreate() {
    super.onCreate()

    if (BuildConfig.DEBUG) {
      Timber.plant(Timber.DebugTree())
    }
  }
}