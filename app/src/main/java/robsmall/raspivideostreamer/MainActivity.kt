package robsmall.raspivideostreamer

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import timber.log.Timber

class MainActivity : AppCompatActivity() {
  @BindView(R.id.url_text) lateinit var urlText: TextView

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ButterKnife.bind(this)

    urlText.text = getString(R.string.url_text).format(BuildConfig.BASE_API_URL)
  }

  @OnClick(R.id.stream_button)
  fun onStreamButtonClick() {
    Timber.d("Viewing Stream Activity.")
    val intent = Intent(this, StreamActivity::class.java)
    startActivity(intent)
  }
}
