plugins {
    id("java")
}

dependencies {
    api(project(":infrastructure:messaging"))
    
    implementation(project(":item:api"))
}
