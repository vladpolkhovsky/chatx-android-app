# ChatX android application

To build component, you need Java 17

## Build apk

You need an Android Sdk installed in your system.

### Prerequirements

- Environment variable `ANDROID_HOME` set to SDK HOME (example: `D:/Android/SDK`)
- Or file [local.properties](local.properties) with variable
  ```properties
  sdk.dir=D\:\\Android\\SDK
  ```
- In file `[gradle.properties](gradle.properties)` update variable `app.domain` to actual server
  base path