dependencies {
    implementation(project(":gateway:api"))
    implementation(project(":item:adapters"))
    implementation(project(":item:service"))
    
    // Kodein DI
    val kodeinVersion = "7.0.0"
    implementation("org.kodein.di:kodein-di:$kodeinVersion")
}
