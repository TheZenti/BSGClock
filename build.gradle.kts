plugins {
    id("java")
    id("application")
}

group = "de.zentigame"
version = "1.0-SNAPSHOT"

val lwjglVersion = "2.9.3"

repositories {
    mavenCentral()
    maven("https://www.beatunes.com/repo/maven2/")
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.1")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.1")
    implementation("org.slick2d:slick2d-core:1.0.2")
    implementation("org.lwjgl.lwjgl:lwjgl:$lwjglVersion")
    implementation("org.lwjgl.lwjgl:lwjgl_util:$lwjglVersion")
    runtimeOnly("org.lwjgl.lwjgl:lwjgl-platform:$lwjglVersion")
    implementation("com.jcraft:jogg:0.0.7")
    implementation("org.l33tlabs.twl:pngdecoder:1.0")
}

val copyNativeLibs = tasks.register<Copy>("copyNativeLibs") {
    from(configurations.runtimeClasspath.get()
        .filter { it.name.contains("natives") }
        .map { zipTree(it) }) {
        include("**.so", "**.dll", "**.dylib")
    }
    into("native_lib")
}

distributions {
    main {
        contents {
            from(copyNativeLibs) {
                into("native_lib")
            }
        }
    }
}

application {
    mainClass.set("de.zentigame.bsgclock.MainMenu")
    applicationDefaultJvmArgs = mutableListOf("-Djava.library.path=./native_lib")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
