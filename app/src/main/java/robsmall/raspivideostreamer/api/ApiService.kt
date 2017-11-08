package robsmall.raspivideostreamer.api

import io.reactivex.Observable
import retrofit2.http.POST
import retrofit2.http.Query
import robsmall.raspivideostreamer.BuildConfig
import robsmall.raspivideostreamer.api.models.StartStopResponse

val HOST_URL = BuildConfig.BASE_API_URL
val SIMPLE_STREAM_HOST_URL = BuildConfig.BASE_RAW_STREAM_URL

val VIDEO_FEED_PATH = "video_feed"

/**
 * Interface that defines all of the endpoints and HOST_URL for communication with the server.
 * This "interfaces" with Retrofit (different meaning of interface).
 */
interface ApiService {
  @POST("/disable_feed")
  fun disableStreams(
      @Query("uid") uid: String
  ): Observable<StartStopResponse>

  @POST("/enable_feed")
  fun enableStreams(
      @Query("uid") uid: String
  ): Observable<StartStopResponse>
}