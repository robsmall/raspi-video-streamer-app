package robsmall.raspivideostreamer.baseClasses

import android.support.v7.app.AppCompatActivity
import io.reactivex.disposables.CompositeDisposable

/**
 * An {@link AppCompatActivity} that houses a {@link CompositeDisposable} and clears all Disposables
 * that are added in {@code onStop()}.
 *
 * Note: The {@code open} keyword is used to say that this is an explicit supertype.
 * For more: https://kotlinlang.org/docs/reference/classes.html.
 */
open class DisposableActivity : AppCompatActivity() {
  protected val disposables: CompositeDisposable = CompositeDisposable()

  override fun onStop() {
    super.onStop()

    // Clear any disposables when we are stopping the activity.
    disposables.clear()
  }
}