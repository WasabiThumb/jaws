import java.io.FileOutputStream

plugins {
    id("java")
}

val protocolVersion: Byte = 0
val patchVersion: Byte = 0
group = "xyz.wasabicodes"
version = "1.${protocolVersion}.${patchVersion}"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.goterl:lazysodium-java:5.1.4")
    implementation("net.java.dev.jna:jna:5.14.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.6")
    implementation("org.slf4j:slf4j-api:2.0.6")
    implementation("it.unimi.dsi:fastutil:8.5.13")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.slf4j:slf4j-simple:2.0.6")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register("substitutions") {
    doFirst {
        file("src/main/resources/xyz/wasabicodes/jaws/protocol-version.txt")
            .let { FileOutputStream(it, false) }
            .use { out ->
                val s: String = protocolVersion.toString()
                out.write(s.toByteArray(Charsets.UTF_8))
                out.flush()
            }
    }
}

tasks.processResources {
    dependsOn("substitutions")
}
