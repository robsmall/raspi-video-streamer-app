# raspi-video-streamer-app
Kotlin Android Application For Receiving a Live Stream from A RaspberryPi.
// TODO: Add link to backend repo.

Resources For Learning:
-----------------------
- [Jake Wharton talk on use of Retrofit to help understand cool features.](http://jakewharton.com/making-retrofit-work-for-you-ohio/)
- [Jake Wharton talk on Retrofit and RxJava being used together](http://jakewharton.com/retrofit-and-rxjava/)
- [Kotlin & Retrofit article](https://android.jlelse.eu/keddit-part-6-api-retrofit-kotlin-d309074af0) -- See related github.
- [Kotlin & RxJava & RxAndroid article](https://android.jlelse.eu/keddit-part-5-kotlin-rxjava-rxandroid-105f95bfcd22) -- See related github.

Credits
-------
- // TODO: Note the mjpeg library


Configuration
-------------
In the `build.gradle` file licated in `app`, you must create and set the path
and properties for `secureProperties`.

For Example:

I have a file located at `/Users/robsmall/src/android/RaspiVideoStreamer/secure-gradle.properties` that contains:
```
BaseApiUrl=http://10.0.0.7:5000
BaseRawStreamUrl=http://10.0.0.7:8000
```

See < // TODO: Inset Link To Backend Repo Once Supplied> for how to set up the server side for this project.