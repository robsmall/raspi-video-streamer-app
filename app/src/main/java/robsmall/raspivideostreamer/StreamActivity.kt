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
import java.util.*
import android.net.Uri
import butterknife.OnClick
import robsmall.raspivideostreamer.api.HOST_URL
import robsmall.raspivideostreamer.api.VIDEO_FEED_PATH
import kotlin.collections.HashMap
import android.location.LocationListener
import android.location.LocationManager
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import robsmall.raspivideostreamer.api.ApiManager
import robsmall.raspivideostreamer.baseClasses.DisposableActivity


class StreamActivity : DisposableActivity() {
  @BindView(R.id.stream_view) lateinit var streamView: MjpegView

  private val uid: String = UUID.randomUUID().toString()
  private lateinit var locationManager: LocationManager
  private lateinit var homeLocation: Location

  private val LOCATION_REQUEST_CODE = 100
  private val TIMEOUT = 5
  private val URL_PARAMS: HashMap<String, String> = hashMapOf("uid" to uid)
  private val MAX_METERS_FROM_HOME = 100.0

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_stream_view)
    ButterKnife.bind(this)

    // Create persistent LocationManager reference
    locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

    // Set up the home location from the private gradle file.
    homeLocation = Location("")
    homeLocation.latitude = BuildConfig.HOME_LATITUDE
    homeLocation.longitude = BuildConfig.HOME_LONGITUDE
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
  }

  override fun onPause() {
    super.onPause()
    streamView.stopPlayback()
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
    var url_builder = Uri.parse(HOST_URL + "/" + VIDEO_FEED_PATH).buildUpon()

    for (param in URL_PARAMS) {
      url_builder.appendQueryParameter(param.key, param.value)
    }

    val url = url_builder.toString()
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
          Toast.makeText(this, "Error streaming video.", Toast.LENGTH_LONG).show()
        }
  }

  /**
   * How far away the user currently is from the home location.
   */
  private fun distanceFromHome(currentLocation: Location): Float {
    return homeLocation.distanceTo(currentLocation)
  }

  /**
   * LocationListener to manage changing of location. If the user is closer than
   * MAX_METERS_FROM_HOME, then turn all camera feeds off.
   *
   * TODO: Otherwise, turn the camera back on.
   */
  private val locationListener: LocationListener = object : LocationListener {
    override fun onLocationChanged(currentLocation: Location) {
      Timber.d("Lat: " + currentLocation.latitude + " long: " + currentLocation.longitude)

      val distanceFromHome = distanceFromHome(currentLocation)
      if (distanceFromHome < MAX_METERS_FROM_HOME) {
        Timber.d("User is closer than $MAX_METERS_FROM_HOME meters " +
            "($distanceFromHome meters)from home location, disabling all cameras")

        disposables.add(ApiManager.stopAllStreams(uid)
            .subscribe({ apiResponse ->
              Timber.i("Received response: " + apiResponse.status)
            }, { throwable ->
              Timber.e(throwable, "Error receiving response.")
            }))
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

  @OnClick(R.id.stop_stream_button)
  fun onStopClick() {
    Timber.d("Stopping video stream.")

    disposables.add(ApiManager.stopAllStreams(uid)
        .subscribe({ apiResponse ->
          Timber.i("Received response: " + apiResponse.status)
        }, { throwable ->
          Timber.e(throwable, "Error receiving response.")
        }))
  }

  @OnClick(R.id.update_location_button)
  fun onUpdateLocationClick() {
    Timber.d("Updating location.")

    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)
    } else {
      startUpdatingLocation()
    }
  }
}