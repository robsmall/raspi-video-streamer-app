package robsmall.raspivideostreamer.api

import io.reactivex.Observable
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
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

  // Since this is a companion object initialized using the lazy function, the object will only be
  // created once and this is treated as a singleton. For more info, check out:
  // https://kotlinlang.org/docs/reference/delegated-properties.html
  companion object {
    val singleton: ApiService by lazy {
      val retrofit = Retrofit.Builder()
          .baseUrl(HOST_URL)
          .client(getOkHttpClient())
          .addConverterFactory(MoshiConverterFactory.create())
          .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
          .build()

      // Generates an singleton of the interface so we have an HttpClient to interact with the service.
      retrofit.create(ApiService::class.java)
    }

    /**
     * Get the log level to be used for HTTP requests.
     */
    private fun getLogLevel(): HttpLoggingInterceptor.Level {
      return if (BuildConfig.DEBUG) {
        HttpLoggingInterceptor.Level.BODY
      } else {
        HttpLoggingInterceptor.Level.NONE
      }
    }

    /**
     * Generate the OkHttpClient to be used for requests.
     */
    private fun getOkHttpClient(): OkHttpClient {
      val interceptor = HttpLoggingInterceptor()
      interceptor.level = getLogLevel()

      return OkHttpClient.Builder()
          .addInterceptor(interceptor)
          .build()
    }
  }
}