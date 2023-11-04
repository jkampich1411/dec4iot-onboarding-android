# `sensors_iot/onboarding-android` - dec4IoT Application for Android

This folder contains the application that is needed to configure Bangle.JS and Puck.JS devices and to send Emergency Data from Bangle.JS 

Content:
* [Developer Information](#developer-information)
    * [Deployment](#deployment)
* [Contributing](#contributing)
* [About](#about)
* [License](#license)

## Developer Information

### Deployment

Either download a prebuild APK from GitHub Actions, clone this Repo or download the Repository's Zip File.

#### Download prebuilt APK
In the "[Generate APK](https://github.com/jkampich1411/dec4iot-onboarding-android/actions/workflows/build-android-app.yml)" Action, choose the latest Actions Run and download the Artifact ZIP.
In there you will find an "app-debug.apk", which you can deploy onto your devices.

#### Building from Android Studio
After cloning this Repo / downloading the ZIP, open it in [Android Studio](https://developer.android.com/studio), export an APK via "Build" > "Build Bundle(s) / APK(s)" > "Build APK(s)".

#### Building from the command line
* Make sure you have Java 17 or newer installed.
* After cloning this Repo / downloading the ZIP, open a Terminal Window in the folder you cloned to / extracted the Zip into.
* Depending on your OS, run `./gradlew build` (Here Windows)
* Then, run `./gradlew assemble` (Windows, again)

## Contributing

Please report bugs and suggestions for new features using the [GitHub Issue-Tracker](https://github.com/dec112/dc-iot/issues) and follow the [Contributor Guidelines](https://github.com/twbs/ratchet/blob/master/CONTRIBUTING.md).

If you want to contribute, please follow these steps:

1. Fork it!
2. Create a feature branch: `git checkout -b my-new-feature`
3. Commit changes: `git commit -am 'Add some feature'`
4. Push into branch: `git push origin my-new-feature`
5. Send a Pull Request



## About

<img align="right" src="https://raw.githubusercontent.com/dec112/dc-iot/main/app/assets/images/netidee.jpeg" height="150">This project has received funding from [Netidee Call 17](https://netidee.at).

<br clear="both" />

## License

[MIT License 2023 - DEC112](https://raw.githubusercontent.com/dec112/dc-iot/main/LICENSE)
