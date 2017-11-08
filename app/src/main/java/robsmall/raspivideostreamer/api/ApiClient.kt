package robsmall.raspivideostreamer.api

import io.reactivex.Observable
import robsmall.raspivideostreamer.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory


/**
 * Api Client is the asynchronous client that communicates with the simple camera REST Api utilizing
 * Retrofit and OkHttp.
 *
 * This is the object that actually makes the requests to the server.
 *
 * Since this is defined as an {@code object}, it is treated as a singleton.
 * For more information on this, see: https://kotlinlang.org/docs/reference/object-declarations.html
 */
object ApiClient {
  private val apiService: ApiService

  init {
    // Note: The retrofit object itself doesn't make the requests, the client created by
    //       getOkHttpClient() does this.
    val retrofit = Retrofit.Builder()
        .baseUrl(HOST_URL)
        .client(getOkHttpClient())
        .addConverterFactory(MoshiConverterFactory.create())
        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
        .build()

    // Generates an singleton of the interface so we have an HttpClient to interact with the service.
    apiService = retrofit.create(ApiService::class.java)
  }

  /*
   * ==================
   * Api Calls
   * ==================
   */
  /**
   * Tell the server to stop all streams.
   */
  @JvmStatic
  fun stopAllStreams(uid: String): Observable<ApiResponse> {
    return apiService.stopAllStreams(uid)
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