Networking Components
---------------------
- `ApiService`: Interface that holds all of the endpoints.
- `ApiClient `: Class that is used to asynchronously communicate with the server.
- `ApiResponse`: Response object to house data received from the server.
- `ApiModel`: Calls into the `ApiClient` to delegate where observations and subscriptions should be made.
    - This is the object that callers should interact with.