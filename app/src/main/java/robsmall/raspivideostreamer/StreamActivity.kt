package robsmall.raspivideostreamer

import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import robsmall.raspivideostreamer.api.ApiClient
import robsmall.raspivideostreamer.api.HOST_URL
import robsmall.raspivideostreamer.api.VIDEO_FEED_PATH
import kotlin.collections.HashMap

class StreamActivity : AppCompatActivity() {
  @BindView(R.id.stream_view) lateinit var streamView: MjpegView
  private val uid: String = UUID.randomUUID().toString()

  private val TIMEOUT = 5
  private val URL_PARAMS: HashMap<String, String> = hashMapOf("uid" to uid)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_stream_view)
    ButterKnife.bind(this)
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
//        .credential(username, password)
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

  @OnClick(R.id.stop_stream_button)
  fun onStopButtonClick() {
    Timber.d("Stopping video stream.")

    // TODO: hold on to a subscription and destroy it.
    // TODO: move this out into its own object... look into mvp, mvvm, etc...
    // Look at https://medium.com/@CodyEngel/managing-disposables-in-rxjava-2-for-android-388722ae1e8a
    // and https://github.com/r7v/Tweetz/blob/2ccd62366b07ddea4e0688e310d5dd19a37c7a5e/app/src/main/java/com/rahulrv/tweetz/viewmodel/BaseViewModel.java
    // for Disposable management.
    // TODO: look into "Error receiving response. Non-crashing
    ApiClient.stopStream(uid)
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe({ apiResponse ->
          Timber.i("Received response: " + apiResponse.data.toString())
        }, { throwable ->
          Timber.e(throwable, "Error receiving response.")
        }
    )
  }
}