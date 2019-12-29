import com.moowork.gradle.node.yarn.YarnTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("org.springframework.boot") version "2.2.2.RELEASE"
    id("io.spring.dependency-management") version "1.0.8.RELEASE"
    kotlin("jvm") version "1.3.61"
    kotlin("plugin.spring") version "1.3.61"
    id("org.jlleitschuh.gradle.ktlint") version "9.1.1"
    id("com.moowork.node") version "1.3.1"
    kotlin("kapt") version "1.3.61"
}

group = "com.ozwillo"
version = "0.5.0-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

val developmentOnly by configurations.creating
configurations {
    runtimeClasspath {
        extendsFrom(developmentOnly)
    }
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
    mavenCentral()
}

dependencies {

    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-amqp")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:1.0.1.RELEASE")
    developmentOnly("org.springframework.boot:spring-boot-devtools")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.amqp:spring-rabbit-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("com.github.tomakehurst:wiremock-standalone:2.25.1")
    testImplementation("com.ninja-squad:springmockk:2.0.0")
    testImplementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo")
}

defaultTasks("bootRun")

tasks.withType<Test> {
    environment("SPRING_PROFILES_ACTIVE", "test")
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}

ktlint {
    disabledRules.set(setOf("no-wildcard-imports"))
}

tasks.bootRun {
    environment("SPRING_PROFILES_ACTIVE", "dev")
}

node {
    version = "8.11.3"
    download = false
}

tasks.processResources {
    // we don't want the JS sources in the jar, we only need their transpiled, bundled version in build/
    exclude(listOf("public/src", "public/node_modules/", "public/babel.config.json", "public/.*"))
}

tasks {
    register<YarnTask>("yarnInstall") {
        args = listOf("--cwd", "./src/main/resources/public", "install")
    }
    register<YarnTask>("frontBundle") {
        args = listOf("--cwd", "./src/main/resources/public", "run", "build")
    }
}

tasks.named("jar") {
    dependsOn(":frontBundle")
}

tasks.named("frontBundle") {
    dependsOn(":yarnInstall")
}

tasks.named("assemble") {
    dependsOn(":frontBundle")
}

tasks.getByName<BootJar>("bootJar") {
    launchScript {
        properties(mapOf("initInfoProvides" to "ozwillo-dc-importer"))
    }
}
