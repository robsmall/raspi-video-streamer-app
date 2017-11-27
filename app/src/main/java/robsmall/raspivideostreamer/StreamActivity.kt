package robsmall.raspivideostreamer

import android.Manifest
import android.annotation.SuppressLint
import android.content.res.Configuration
import android.location.Location
import android.os.Bundle
import butterknife.ButterKnife
import com.github.niqdev.mjpeg.MjpegView
import butterknife.BindView
import android.widget.Toast
import com.github.niqdev.mjpeg.DisplayMode
import com.github.niqdev.mjpeg.Mjpeg
import timber.log.Timber
import android.net.Uri
import robsmall.raspivideostreamer.api.HOST_URL
import robsmall.raspivideostreamer.api.VIDEO_FEED_PATH
import kotlin.collections.HashMap
import android.location.LocationListener
import android.location.LocationManager
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.preference.PreferenceManager
import android.view.Menu
import android.view.MenuItem
import com.squareup.moshi.Moshi
import robsmall.raspivideostreamer.api.ApiManager
import robsmall.raspivideostreamer.baseClasses.DisposableActivity
import robsmall.raspivideostreamer.api.models.StartStopResponse
import java.util.*


class StreamActivity : DisposableActivity() {
  @BindView(R.id.stream_view) lateinit var streamView: MjpegView

  private lateinit var uid: String
  private lateinit var locationManager: LocationManager
  private lateinit var homeLocation: Location
  private lateinit var moshi: Moshi
  private lateinit var url_params: HashMap<String, String>

  private val LOCATION_REQUEST_CODE = 100
  private val TIMEOUT = 5
  private val MAX_METERS_FROM_HOME = 100.0
  private val UUID_PREFS_KEY = "uuid.prefs.key"

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_stream_view)
    ButterKnife.bind(this)

    locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

    homeLocation = Location("")
    homeLocation.latitude = BuildConfig.HOME_LATITUDE
    homeLocation.longitude = BuildConfig.HOME_LONGITUDE

    moshi = Moshi.Builder().build()

    uid = getUidFromPrefs()

    url_params = hashMapOf("uid" to uid, "is_mobile" to "True")

    // Note: We can use !! here because we know the supportActionBar should never be null in practice
    // (even though Android Studio says it POSSIBLY can be :))
    supportActionBar!!.setDisplayHomeAsUpEnabled(true)
  }

  override fun onCreateOptionsMenu(menu: Menu): Boolean {
    menuInflater.inflate(R.menu.stream_menu, menu)
    return true
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> {
        finish()
        return true
      }
      R.id.enable_stream -> {
        Timber.d("Starting video stream.")
        enableCamera()
        return true
      }
      R.id.disable_stream -> {
        Timber.d("Stopping video stream.")
        disableCamera()
        return true
      }
      R.id.enable_location -> {
        Timber.d("Updating location.")

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
          ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
        } else {
          startUpdatingLocation()
        }

        return true
      }
      R.id.disable_location -> {
        // TODO: Show a dialog here asking if the user wants to re-enable the camera if they
        //       explicitly stopped it.
        stopUpdatingLocation()
        return true
      }
      else -> return super.onOptionsItemSelected(item)
    }
  }

  override fun onRequestPermissionsResult(requestCode: Int,
                                          permissions: Array<String>,
                                          grantResults: IntArray) {
    if (requestCode == LOCATION_REQUEST_CODE) {
      if (grantResults.size == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        // We can now safely use the API we requested access to.
        startUpdatingLocation()
      } else {
        // Permission was denied or request was cancelled...
        Toast.makeText(this, "This means the camera will not turn off by default if you are in the house.", Toast.LENGTH_SHORT).show()
      }
    }
  }

  override fun onResume() {
    super.onResume()
    loadRasPiCam()

    // TODO: get whether we were updating location from the savedinstancestate and resume if so.
  }

  override fun onPause() {
    super.onPause()
    streamView.stopPlayback()
    stopUpdatingLocation()
  }

  /**
   * Set the shared uid from shared preferences.
   */
  private fun setUidPref(uid: String) {
    PreferenceManager.getDefaultSharedPreferences(applicationContext).edit().putString(UUID_PREFS_KEY, uid).apply()
  }

  /**
   * Get the shared uid from shared preferences.
   */
  private fun getUidFromPrefs(): String {
    var uid = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(UUID_PREFS_KEY, null)

    if (uid == null) {
      uid = UUID.randomUUID().toString()
      setUidPref(uid)
    }

    return uid
  }

  /**
   * Get the DisplayMode based on the current orientation of the screen.
   */
  private fun getDisplayMode(): DisplayMode {
    val orientation = resources.configuration.orientation
    return if (orientation == Configuration.ORIENTATION_LANDSCAPE)
      DisplayMode.FULLSCREEN
    else
      DisplayMode.BEST_FIT
  }

  /**
   * Get the url to be used for the Mjpeg view, complete with all the parameters defined in
   * {@code URL_PARAMS}.
   */
  private fun getUrlWithParams(): String {
    val urlBuilder = Uri.parse(HOST_URL + "/" + VIDEO_FEED_PATH).buildUpon()

    for (param in url_params) {
      urlBuilder.appendQueryParameter(param.key, param.value)
    }

    val url = urlBuilder.toString()
    Timber.i("Video Url = " + url)

    return url
  }

  /**
   * Load the raspberry pi camera view into the Mjpeg view.
   */
  private fun loadRasPiCam() {
    Mjpeg.newInstance()
        // TODO: figure out an auth scheme... either to connect or to use the backend.
        // .credential(username, password)
        .open(getUrlWithParams(), TIMEOUT)
        .subscribe(
            { inputStream ->
              streamView.setSource(inputStream)
              streamView.setDisplayMode(getDisplayMode())
              streamView.showFps(true)
            }
        ) { throwable ->
          Timber.e(throwable, "MJpeg error when streaming video.")
          Toast.makeText(this, "Error streaming video: " + throwable.message, Toast.LENGTH_LONG).show()
        }
  }

  /**
   * Tell the server to enable all camera feeds (if no one else if blocking them).
   */
  fun enableCamera() {
    disposables.add(ApiManager.enableStreams(uid)
        .subscribe({ startStopResponse ->
          Timber.i("Received response when starting camera: " +
              moshi.adapter(StartStopResponse::class.java).toJson(startStopResponse))
          displayBlockingCameraMessage(startStopResponse, false)
        }, { throwable ->
          Timber.e(throwable, "Error receiving response when starting camera.")
        }))
  }

  /**
   * Tell the server to disable all camera feeds.
   */
  fun disableCamera() {
    disposables.add(ApiManager.disableStreams(uid)
        .subscribe({ startStopResponse ->
          Timber.i("Received response when stopping camera: " +
              moshi.adapter(StartStopResponse::class.java).toJson(startStopResponse))
          displayBlockingCameraMessage(startStopResponse, true)
        }, { throwable ->
          Timber.e(throwable, "Error receiving response when stopping camera.")
        }))
  }

  /**
   * How far away the user currently is from the home location.
   */
  private fun distanceFromHome(currentLocation: Location): Float = homeLocation.distanceTo(currentLocation)

  /**
   * LocationListener to manage changing of location. If the user is closer than
   * MAX_METERS_FROM_HOME, then turn all camera feeds off.
   * Otherwise, stop blocking the camera feed.
   */
  private val locationListener: LocationListener = object : LocationListener {
    override fun onLocationChanged(currentLocation: Location) {
      Timber.d("Lat: " + currentLocation.latitude + " long: " + currentLocation.longitude)

      val distanceFromHome = distanceFromHome(currentLocation)
      if (distanceFromHome < MAX_METERS_FROM_HOME) {
        Timber.d("User is closer than $MAX_METERS_FROM_HOME meters " +
            "($distanceFromHome meters) from home location, disabling all cameras.")
        disableCamera()
      } else {
        Timber.d("User is further than $MAX_METERS_FROM_HOME meters " +
            "($distanceFromHome meters) from home location, enabling all cameras if possible.")
        enableCamera()
      }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
      Timber.d("Location status changed.", provider, status, extras)
    }

    override fun onProviderEnabled(provider: String) {
      Timber.d("Location Provider enabled.", provider)
    }

    override fun onProviderDisabled(provider: String) {
      Timber.d("Location Provider disabled: ", provider)
    }
  }

  /**
   * Create persistent LocationManager reference.
   */
  @SuppressLint("MissingPermission")
  private fun startUpdatingLocation() {
    Timber.d("Stating location updates.")

    locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
    try {
      // This should be caught below so suppressing lint
      locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
    } catch (e: Exception) {
      // TODO: pull this out into an extension or something.
      when (e) {
        is SecurityException,
        is IllegalArgumentException -> {
          Timber.e(e, "Error obtaining location.")
        }
        else -> throw e
      }
    }
  }

  /**
   * Tell the locationManager to stop updating the locationListener
   */
  private fun stopUpdatingLocation() {
    Timber.d("Stopping location updates...")
    locationManager.removeUpdates(locationListener)
  }

  /**
   * Tell the user how many cameras are now blocking the feed.
   */
  private fun displayBlockingCameraMessage(startStopResponse: StartStopResponse, isStopping: Boolean) {
    // Yeah, singular and plurals and things... IDC at this time.
    var message = if (isStopping) "Disabled camera. " else "Attempting to enable camera. "
    message += "There are now ${startStopResponse.response.blocking_cameras} more cameras blocking the feed."

    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
  }
}