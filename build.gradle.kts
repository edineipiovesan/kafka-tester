import org.apache.avro.Schema.Parser
import org.apache.avro.compiler.specific.SpecificCompiler
import org.apache.avro.compiler.specific.SpecificCompiler.FieldVisibility
import org.apache.avro.generic.GenericData.StringType.CharSequence
import org.gradle.api.JavaVersion.VERSION_17
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.7.20"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion

    id("org.springframework.boot") version "3.0.0-SNAPSHOT"
    id("io.spring.dependency-management") version "1.0.13.RELEASE"
}

group = "com.github.edineipiovesan"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = VERSION_17

repositories {
    mavenLocal()
    mavenCentral()
    maven { url = uri("https://repo.spring.io/milestone") }
    maven { url = uri("https://repo.spring.io/snapshot") }
    maven { url = uri("https://packages.confluent.io/maven") }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.6.4")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka:2.9.0")

    implementation("tech.allegro.schema.json2avro:converter:0.2.15")
    implementation("org.apache.avro:avro:1.11.0")
    implementation("io.confluent:kafka-avro-serializer:7.2.1")

    implementation("software.amazon.msk:aws-msk-iam-auth:1.1.4")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("io.micrometer:micrometer-registry-prometheus:1.9.4")

    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")

    implementation(platform("com.squareup.okhttp3:okhttp-bom:4.10.0"))
    implementation("com.squareup.okhttp3:okhttp")
    implementation("com.squareup.okhttp3:logging-interceptor")
}

/**
 *  Avro Generator
 */
buildscript {
    dependencies {
        classpath("org.apache.avro:avro-tools:1.8.2")
    }
}

val avroGen by tasks.register("generateAvroJavaClasses") {
    val sourceAvroFiles = fileTree("src/main/resources/avro") { include("**/*.avsc") }
    val generatedJavaDir = File("${rootDir}/src/main/java")

    inputs.files(sourceAvroFiles)
    outputs.dir(generatedJavaDir)

    doLast {
        sourceAvroFiles.forEach { avroFile ->
            val schema = Parser().parse(avroFile)
            val compiler = SpecificCompiler(schema)
            compiler.setFieldVisibility(FieldVisibility.PRIVATE)
            compiler.setOutputCharacterEncoding("UTF-8")
            compiler.setStringType(CharSequence)
            compiler.compileToDestination(avroFile, generatedJavaDir)
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    source(avroGen)
}

tasks.named("clean", Delete::class) {
    delete("src/main/java")
}

/**
 * Kotlin
 */
tasks.withType<KotlinCompile> {
    dependsOn(avroGen)
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}
