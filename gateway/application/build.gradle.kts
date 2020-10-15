plugins {
    application
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

dependencies {
    implementation(project(":gateway:api"))
    implementation(project(":gateway:composition-root"))
    implementation(project(":infrastructure:server-sent-events"))
    implementation(project(":item:adapters"))
    implementation(project(":item:service"))
    
    // Kodein DI
    val kodeinVersion = "7.0.0"
    implementation("org.kodein.di:kodein-di:$kodeinVersion")
    
    // Ktor
    implementation("io.ktor:ktor-serialization")
    implementation("io.ktor:ktor-server-netty")
    testImplementation("io.ktor:ktor-client-cio")
    
    // Logback
    val logbackVersion = "1.2.3"
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
}
