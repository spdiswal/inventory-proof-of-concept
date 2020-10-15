import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.serialization")
    
    repositories {
        jcenter()
        mavenCentral()
    }
    
    dependencies {
        // Kotlin
        implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
        
        val coroutinesVersion = "1.3.9"
        implementation(platform("org.jetbrains.kotlinx:kotlinx-coroutines-bom:$coroutinesVersion"))
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
        
        val serializationVersion = "1.0.0"
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
        
        // Ktor
        val ktorVersion = "1.4.1"
        implementation(platform("io.ktor:ktor-bom:$ktorVersion"))
        
        // Kotest
        val kotestVersion = "4.3.0"
        testImplementation("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
        
        // Strikt
        val striktVersion = "0.28.0"
        testImplementation("io.strikt:strikt-core:$striktVersion")
    }
    
    tasks {
        withType<KotlinCompile> {
            kotlinOptions {
                languageVersion = "1.4"
                apiVersion = "1.4"
                jvmTarget = "14"
                
                freeCompilerArgs = listOf(
                    "-Xopt-in=kotlin.ExperimentalStdlibApi",
                    "-Xopt-in=kotlin.time.ExperimentalTime",
                    "-Xopt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
                    "-Xopt-in=kotlinx.coroutines.FlowPreview",
                    "-Xopt-in=io.ktor.util.KtorExperimentalAPI"
                )
            }
        }
        
        test {
            useJUnitPlatform()
        }
    }
}
