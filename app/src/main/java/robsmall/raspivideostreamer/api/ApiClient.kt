package robsmall.raspivideostreamer.api

import io.reactivex.Observable


/**
 * Api Client is the asynchronous client that communicates with the simple camera REST Api utilizing
 * Retrofit and OkHttp.
 * This is the object that actually makes the requests to the server.
 */
object ApiClient {
  private val apiService: ApiService = ApiService.singleton

  init {
    // Note: The retrofit object itself doesn't make the requests, the client created by
    //       getOkHttpClient() does this.

  }

  fun stopStream(uid: String): Observable<ApiResponse> {
    return apiService.stopStream(uid)
  }
}