package robsmall.raspivideostreamer.api.models

class StartStopResponse(
    val status: String,
    val response: Response)

class Response(val blocking_cameras: Int)