plugins {
    groovy
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain {
        (this as JavaToolchainSpec).languageVersion.set(JavaLanguageVersion.of(8))
    }
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())

    implementation("com.badlogicgames.gdx:gdx-jnigen:2.0.1")
    implementation("org.reflections:reflections:0.10.2")
}
