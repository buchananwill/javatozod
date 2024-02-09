plugins {
    id("java")
}

group = "org.maths"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.reflections:reflections:0.10.2")
    implementation("org.apache.logging.log4j:log4j-core:3.0.0-alpha1")
    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

val runDtoParser by tasks.registering(JavaExec::class) {
    group = "custom"
    description = "Runs DtoParser."
    mainClass.set("org.javatozod.DtoParser")
    classpath = sourceSets["main"].runtimeClasspath
}


tasks.withType<JavaCompile> {
    options.annotationProcessorPath = configurations["annotationProcessor"]
}

tasks.named("compileJava") {
    finalizedBy("runDtoParser")
}
