plugins {
    `java-library`
    alias(libs.plugins.jcommon)
    alias(libs.plugins.bundler)
}

jcommon {
    javaVersion = JavaVersion.VERSION_25
    setupPaperRepository()
}

repositories {
    listOf("https://mvn-repo.arim.space/lesser-gpl3", "https://mvn-repo.arim.space/affero-gpl3").forEach {
        maven {
            url = uri(it)
            mavenContent {
                includeGroupAndSubgroups("space.arim")
            }
        }
    }
}

dependencies {
    compileOnly(libs.velocity.api)
    compileOnly(libs.libertybans)

    implementation(libs.codec4j.io.yaml) {
        exclude("org.yaml", "snakeyaml")
    }
}

bundler {
    copyToRootBuildDirectory("WarnActions-${project.version}")
    replacePluginVersionForVelocity(project.version)
}

tasks.shadowJar {
    minimize()
    relocate("dev.siroshun.codec4j", "${project.group}.libs.codec4j")
    relocate("dev.siroshun.jfun", "${project.group}.libs.jfun")
}
