# Registration navigation verification

The sign-up button waits for Parse's successful registration callback before
opening MainActivity. Empty input, pending registration and provider rejection
leave the registration screen available.

With JDK 11, installed Android SDK 31 and an ignored `local.properties` containing
`sdk.dir` and a non-secret `MAPS_API_KEY` build placeholder:

```sh
bash ./gradlew --offline --no-daemon :app:testDebugUnitTest --tests '*SignUpNavigationTest'
bash ./gradlew --offline --no-daemon :app:testDebugUnitTest
```

Dependencies must already be cached for offline mode. The tests mock Android
widgets, intents, Toast and Parse construction/callbacks. They do not initialize
Parse, access credentials, create real accounts or certify device behavior.
