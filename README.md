# CSCI 3010 Project
#### WeatherMap

### Dependencies
* macOS Sierra (10.12) or High Sierra (10.13)
* Xcode 9

No external dependencies should be required

### How to setup and run the project
1. Install Xcode 9 from the [Mac App Store](https://itunes.apple.com/us/app/xcode/id497799835)
2. Clone the git repo
3. Open Xcode and select "Open another project" at the bottom-right of the welcome menu
4. Open `WeatherMap/WeatherMap.xcodeproj`
5. Click the play icon (▶︎) at the top-left of Xcode. This will open up the iOS Simulator app and load the WeatherMap app in the simulator. This may take a few seconds to a couple minutes, depending on the system.
6. Select whether location services are to be enabled. If they are disabled, the simulator will center over the CU Engineering Center. If they are enabled, the simulator will center over San Francisco. This may seem counter-intuitive, but this is because I have the Engineering Center as a default value, and the simulator uses San Francisco as its location when location services are enabled on it. In short, the iOS Simulator does not actually collect your current location.
