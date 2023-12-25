import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDate
import java.time.format.DateTimeFormatter

plugins {
    kotlin("jvm") version "1.9.22"
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"
    kotlin("plugin.serialization") version "1.9.21"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    val junitVersion = "5.10.1"
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

tasks {
    register<Exec>("createDay") {
        group = "advent of code"
        description = "Create the current day's directory and files from the template"
        commandLine("sh", "-c", "${project.rootDir}/scripts/create-day.sh")
    }

    register<Exec>("createReadme") {
        group = "advent of code"
        description = "Create the current day's README from the AoC html page"
        commandLine("sh", "-c", "${project.rootDir}/scripts/generate-readme.sh")
    }

    register<Test>("testDay") {
        group = "advent of code"
        description = "Run the current day's tests"
        useJUnitPlatform()
        val day = LocalDate.now().format(DateTimeFormatter.ofPattern("dd"))
        filter {
            includeTestsMatching("day$day.*")
        }
        testLogging {
            info.events = mutableSetOf(
                TestLogEvent.PASSED,
                TestLogEvent.FAILED,
                TestLogEvent.STANDARD_OUT,
                TestLogEvent.STANDARD_ERROR
            )
            events = info.events
        }
    }

    test {
        useJUnitPlatform()
        testLogging.events = mutableSetOf(
            TestLogEvent.PASSED,
            TestLogEvent.FAILED,
            TestLogEvent.STANDARD_OUT,
            TestLogEvent.STANDARD_ERROR
        )
        filter {
            // Don't run tests on the "template" classes
            excludeTestsMatching("dayNN.*")
            // Don't run tests on real input during CI build, since those will fail (personal input data for problems is not checked in to github)
            if (System.getenv("CI") == "true") {
                excludeTestsMatching("*Real Input*")
            }
        }
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "19"
        java {
            sourceCompatibility = JavaVersion.VERSION_19
            targetCompatibility = JavaVersion.VERSION_19
        }
    }

    sourceSets["test"].resources {
        srcDirs("src/test/kotlin")
        exclude("**/*.kt")
    }
}
