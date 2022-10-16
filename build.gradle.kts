import org.apache.avro.Schema.Parser
import org.apache.avro.compiler.specific.SpecificCompiler
import org.apache.avro.compiler.specific.SpecificCompiler.FieldVisibility
import org.apache.avro.generic.GenericData.StringType.CharSequence
import org.gradle.api.JavaVersion.VERSION_11
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.7.10"

    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion

    id("org.springframework.boot") version "2.6.2"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

group = "com.github.edineipiovesan"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = VERSION_11

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.6.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:1.6.4")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka:2.8.2")

    implementation("tech.allegro.schema.json2avro:converter:0.2.15")
    implementation("org.apache.avro:avro:1.8.2")
    implementation("io.confluent:kafka-avro-serializer:5.3.1")

    implementation("software.amazon.msk:aws-msk-iam-auth:1.1.4")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation("io.micrometer:micrometer-registry-prometheus:1.9.4")

    compileOnly("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
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
        jvmTarget = "11"
    }
}
