package robsmall.raspivideostreamer.api

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import robsmall.raspivideostreamer.api.models.StartStopResponse


/**
 * Object that calls into the {@link ApiClient} singleton to reach out to the network and determines
 * where subscriptions and observations should be made.
 *
 * Note: Callers must {@code subscribe()} to the returned {@link Observable}.
 *
 * TODO: is this the best design pattern? I Haven't seen this used anywhere but it seems sane to me.
 */
object ApiManager {

  fun disableStreams(uid: String): Observable<StartStopResponse> {
    return ApiClient.disableStreams(uid)
        .observeOn(Schedulers.io())
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
  }
}