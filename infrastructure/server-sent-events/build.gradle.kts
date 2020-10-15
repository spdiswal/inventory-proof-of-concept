dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core")
    testImplementation("io.ktor:ktor-client-cio")
    testImplementation("io.ktor:ktor-server-netty")
    
    // Logback
    val logbackVersion = "1.2.3"
    testImplementation("ch.qos.logback:logback-classic:$logbackVersion")
}
