package robsmall.raspivideostreamer.api

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers


/**
 * Object that calls into the {@link ApiClient} singleton to reach out to the network and determines
 * where subscriptions and observations should be made.
 *
 * Note: Callers must {@code subscribe()} to the returned {@link Observable}.
 *
 * TODO: is this the best design pattern? I Haven't seen this used anywhere but it seems sane to me.
 */
object ApiManager {

  fun stopAllStreams(uid: String): Observable<ApiResponse> {
    // TODO: hold on to a subscription and destroy it.
    // Look at https://medium.com/@CodyEngel/managing-disposables-in-rxjava-2-for-android-388722ae1e8a
    // and https://github.com/r7v/Tweetz/blob/2ccd62366b07ddea4e0688e310d5dd19a37c7a5e/app/src/main/java/com/rahulrv/tweetz/viewmodel/BaseViewModel.java
    // for Disposable management.
    return ApiClient.stopAllStreams(uid)
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
  }
}