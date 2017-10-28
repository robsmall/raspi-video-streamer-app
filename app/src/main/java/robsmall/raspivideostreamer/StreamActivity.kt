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


class StreamActivity : AppCompatActivity() {
  @BindView(R.id.stream_view) lateinit var streamView: MjpegView

  // TODO: get the public IP for this.
  private val CAMERA_URL = "http://10.0.0.7:8000/stream.mjpg"
//  private val CAMERA_URL = "http://10.0.0.7:5000/video_feed"
  private val TIMEOUT = 5


  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_stream_view)
    ButterKnife.bind(this)
  }

  private fun calculateDisplayMode(): DisplayMode {
    val orientation = resources.configuration.orientation
    return if (orientation == Configuration.ORIENTATION_LANDSCAPE)
      DisplayMode.FULLSCREEN
    else
      DisplayMode.BEST_FIT
  }

  private fun loadRasPiCam() {
    Mjpeg.newInstance()
//        .credential(username, password)
        .open(CAMERA_URL, TIMEOUT)
        .subscribe(
            { inputStream ->
              streamView.setSource(inputStream)
              streamView.setDisplayMode(calculateDisplayMode())
              streamView.showFps(true)
            }
        ) { throwable ->
          Timber.e(throwable, "MJpeg error when streaming video.")
          Toast.makeText(this, "Error streaming video.", Toast.LENGTH_LONG).show()
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
}