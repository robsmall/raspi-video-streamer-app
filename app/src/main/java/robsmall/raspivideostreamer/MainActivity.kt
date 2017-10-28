package robsmall.raspivideostreamer

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import butterknife.ButterKnife
import butterknife.OnClick
import timber.log.Timber

class MainActivity : AppCompatActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)
    ButterKnife.bind(this)
  }

  @OnClick(R.id.stream_button)
  fun onStreamButtonClick() {
    Timber.d("Viewing Stream Activity.")
    val intent = Intent(this, StreamActivity::class.java)
    startActivity(intent)
  }
}
