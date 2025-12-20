# Fabric Example Mod

## Setup

For setup instructions please see the [fabric documentation page](https://docs.fabricmc.net/develop/getting-started/setting-up) that relates to the IDE that you are using.

./gradlew --refresh-dependencies --stacktrace - DO I need to run it?
./gradlew build


Run:
https://docs.fabricmc.net/develop/getting-started/vscode/launching-the-game#generating-launch-targets

Gradlew tasks - IDE - Vscode - Run
Run & Tests - Minecraft Client

.\gradlew clean build
   .\gradlew :1.21.10-fabric:dependencies --configuration runtimeClasspath | Select-String -Pattern "fabric-loader"