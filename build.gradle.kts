import io.github.gradlenexus.publishplugin.NexusPublishExtension

plugins {
    id("io.github.gradle-nexus.publish-plugin")
}

//buildscript {
//    repositories {
//        google()
//        mavenCentral()
//        gradlePluginPortal()
//    }
//    dependencies {
//        classpath "com.android.tools.build:gradle:4.2.0"
//        classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.5.0"
//        classpath "io.github.gradle-nexus:publish-plugin:1.0.0"
//    }
//}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    version = P.projectVersion
    group = P.projectGroupId
    description = P.projectDescription
}

val clean by tasks.creating(Delete::class) {
    group = "build"
    delete(rootProject.buildDir)
}

configure<NexusPublishExtension> {
    val nexusStagingProfileId: String? by project
    val nexusUsername: String? by project
    val nexusPassword: String? by project

    packageGroup.set(group.toString())

    repositories {
        sonatype {
            stagingProfileId.set(nexusStagingProfileId)
            username.set(nexusUsername)
            password.set(nexusPassword)
        }
    }
}
