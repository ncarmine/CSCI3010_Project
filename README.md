# CSCI 3010 Project - WeatherMap

## Dependencies
### iOS app
* macOS Sierra (10.12) or High Sierra (10.13)
* Xcode 9

### Android app
* Linux (tested on Ubuntu 16.04) or macOS (Sierra 10.12 or High Sierra 10.13)
* Android Studio 3.0 or above (tested on 3.1)

No external dependencies should be required for either platform.

## How to setup and run the project
### iOS
1. Install Xcode 9 from the [Mac App Store](https://itunes.apple.com/us/app/xcode/id497799835).
2. Clone the git repo.
3. Open Xcode and select "Open another project" at the bottom-right of the welcome menu.
4. Open `WeatherMap/iOS/WeatherMap.xcodeproj`
5. Click the play icon (▶︎) at the top-left of Xcode. This will open up the iOS Simulator app and load the WeatherMap app in the simulator. This may take a few seconds to a couple minutes, depending on the system.
6. Select whether location services are to be enabled. If they are disabled, the simulator will center over the CU Engineering Center. If they are enabled, the simulator will center over San Francisco. This may seem counter-intuitive, but this is because I have the Engineering Center as a default value, and the simulator uses San Francisco as its location when location services are enabled on it. In short, the iOS Simulator does not actually collect your current location.

### Android
**NOTE: I have only been able to get the maps part of the Android version working on Ubuntu 16.04 and not macOS.**  
The app will run on macOS, but the map will not load.  
1. [Install Android Studio](https://developer.android.com/studio/install.html) for your selected OS.
2. Clone the git repo.
3. Open Android Studio and click "Import project".
4. Navigate to `WeatherMap/Android/WeatherMap` and select it. This should have the Android Studio icon to the left of it. Click "OK" to import the project.
5. Once the project is loaded into Android Studio, click the green play icon (▶︎) at the top-middle to top-right (depending on window size) of Android Studio. This will open the "Select Deployment Target" menu for selecting/creating Android device emulators for the app to run on. I test on Pixel 2 API 27.
6. If "Pixel 2 API 27" is available, click "OK" in the "Select Deployment Target" window to run the app on a virtual Pixel 2 running Android Oreo. Else, see "Creating a New Android Virtual Device" below for creating a new device.
7. A map will be opened with a pin over Sydney, Australia. Google chooses this as the default maps pin location for an arbitrary reason.

#### Creating a New Android Virtual Device
1. Click the "Create New Virtual Device" button in the "Select Deployment Target" window.
2. Select "Pixel 2", then click "Next".
3. Select the Release Name: Oreo / API Level: 27 option. This may required a download. Then click "Next".
4. Name the virtual device as you wish, and click "Finish" to create a Pixel 2 emulator.
5. Go back to #6 in the Android run instructions and select the device just created (likely titled "Pixel 2 API 27" if left as default).
