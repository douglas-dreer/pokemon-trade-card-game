plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.allopen)

    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.sonarqube)
    alias(libs.plugins.asciidoctor)

    id("jacoco")
}

group = "br.com.tcg.pokemon"
version = "0.0.1-SNAPSHOT"
description = "pokemon-trade-card-game"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.jacocoTestReport {
    dependsOn(tasks.test, "integrationTest")

    executionData.setFrom(fileTree(layout.buildDirectory).include("**/jacoco/*.exec"))

    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
    }
}

// Voltando para a declaração individual, que é a forma correta e funcional.
dependencies {
    // ========== Spring Boot Starters ==========
    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.spring.boot.starter.security)
    implementation(libs.spring.boot.starter.kafka)

    // ========== Security ==========
    implementation(libs.spring.security.oauth2.resource.server)
    implementation(libs.spring.security.oauth2.jose)

    // ========== Database & Migrations ==========
    runtimeOnly(libs.postgresql)
    implementation(libs.flyway.core)
    implementation(libs.flyway.postgresql)

    // ========== HTTP Client ==========
    implementation(libs.spring.cloud.starter.openfeign)

    // ========== Kotlin & Jackson ==========
    implementation(libs.kotlin.reflect)
    implementation(libs.jackson.module.kotlin)

    // ========== OpenAPI/Swagger ==========
    implementation(libs.springdoc.openapi.starter.webmvc.ui)

    // ========== Outras Libs ==========
    implementation(libs.gson)

    // ========== Development Tools ==========
    developmentOnly(libs.spring.boot.devtools)
    developmentOnly(libs.spring.boot.docker.compose)

    // ========== Testing ==========
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)
    testImplementation(libs.spring.security.test)
    testImplementation(libs.spring.kafka.test)
    testImplementation(libs.spring.restdocs.mockmvc)
    testImplementation(libs.mockk)
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${libs.versions.springCloud.get()}")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

sonarqube {
    properties {
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.projectName", "pokemon-trade-card-game")
        property("sonar.qualitygate.wait", true)

        System.getenv("SONAR_ORGANIZATION")?.let { property("sonar.organization", it) }
        System.getenv("SONAR_PROJECT_KEY")?.let { property("sonar.projectKey", it) }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.bootBuildImage {
    imageName = "ghcr.io/${project.group}/${project.name}:${project.version}"
    runImage = "paketobuildpacks/ubuntu-noble-run-base:latest"
}

extra["snippetsDir"] = file("build/generated-snippets")

tasks.test {
    outputs.dir(project.extra["snippetsDir"]!!)
}

tasks.asciidoctor {
    inputs.dir(project.extra["snippetsDir"]!!)
    dependsOn(tasks.test)
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

configurations {
    val integrationTestImplementation by getting {
        extendsFrom(implementation.get())
    }
    "integrationTestRuntimeOnly" {
        extendsFrom(runtimeOnly.get())
    }
}

tasks.register<Test>("integrationTest") {
    description = "Roda os testes de integração."
    group = "verification"
    testClassesDirs = sourceSets.getByName("integrationTest").output.classesDirs
    classpath = sourceSets.getByName("integrationTest").runtimeClasspath
    shouldRunAfter(tasks.test)
}

tasks.check {
    dependsOn("integrationTest")
}