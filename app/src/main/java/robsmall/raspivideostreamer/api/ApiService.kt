package robsmall.raspivideostreamer.api

import io.reactivex.Observable
import retrofit2.http.POST
import retrofit2.http.Query
import robsmall.raspivideostreamer.BuildConfig

/**
 * Interface that defines all of the endpoints and HOST_URL for communication with the server.
 * This "interfaces" with Retrofit (different meaning of interface).
 */

// TODO: read these in from a secure gradle file so we don't have to use the local IP.
val HOST_URL = BuildConfig.BASE_API_URL
val SIMPLE_STREAM_HOST_URL = BuildConfig.BASE_RAW_STREAM_URL

val VIDEO_FEED_PATH = "video_feed"

interface ApiService {
  @POST("/stop_feed")
  fun stopStream(
      @Query("uid") uid: String
  ): Observable<ApiResponse>
}