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




A problem occurred configuring root project 'Feesh'.
> Could not resolve all artifacts for configuration 'classpath'.
   > Could not resolve dev.architectury:architectury-pack200:0.1.3.
     Required by:
         root project : > dev.deftu.gradle.multiversion-root:dev.deftu.gradle.multiversion-root.gradle.plugin:2.64.0 > dev.deftu.gradle:gradle-toolkit:2.64.0
         root project : > dev.deftu.gradle.multiversion-root:dev.deftu.gradle.multiversion-root.gradle.plugin:2.64.0 > dev.deftu.gradle:gradle-toolkit:2.64.0 > gg.essential:architectury-loom:1.10.36 > dev.architectury.architectury-pack200:dev.architectury.architectury-pack200.gradle.plugin:0.1.3
      > Could not resolve dev.architectury:architectury-pack200:0.1.3.
         > Could not get resource 'https://maven.architectury.dev/dev/architectury/architectury-pack200/0.1.3/architectury-pack200-0.1.3.pom'.
            > Could not GET 'https://maven.architectury.dev/dev/architectury/architectury-pack200/0.1.3/architectury-pack200-0.1.3.pom'.
               > The server may not support the client's requested TLS protocol versions: (TLSv1.2, TLSv1.3). You may need to configure the client to allow other protocols to be used. For 
more on this, please refer to https://docs.gradle.org/8.12.1/userguide/build_environment.html#sec:gradle_system_properties in the Gradle documentation.
                  > PKIX path validation failed: java.security.cert.CertPathValidatorException: validity check failed


systemProp.javax.net.ssl.checkRevocation=false into gradle.properties