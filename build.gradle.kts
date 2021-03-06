import java.util.EnumSet
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
  }
  dependencies {
    classpath("com.android.tools.build:gradle:7.2.1")
    classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:$kotlinVersion")
    classpath("com.diffplug.spotless:spotless-plugin-gradle:6.7.2")
    classpath("dev.ahmedmourad.nocopy:nocopy-gradle-plugin:1.4.0")
    classpath("org.jacoco:org.jacoco.core:0.8.8")
    classpath("com.vanniktech:gradle-android-junit-jacoco-plugin:0.17.0-SNAPSHOT")
    classpath("com.github.ben-manes:gradle-versions-plugin:0.42.0")
  }
}

subprojects {
  apply(plugin = "com.diffplug.spotless")
  apply(plugin = "com.vanniktech.android.junit.jacoco")
  apply(plugin = "com.github.ben-manes.versions")

  configure<com.diffplug.gradle.spotless.SpotlessExtension> {
    val EDITOR_CONFIG_KEYS: Set<String> = hashSetOf(
      "ij_kotlin_imports_layout",
      "indent_size",
      "end_of_line",
      "charset"
    )

    kotlin {
      target("**/*.kt")

      // TODO this should all come from editorconfig https://github.com/diffplug/spotless/issues/142
      val data = mapOf(
        "indent_size" to "2",
        "ij_kotlin_imports_layout" to "*",
        "end_of_line" to "lf",
        "charset" to "utf-8",
        "disabled_rules" to arrayOf(
          "experimental:package-name",
          "experimental:trailing-comma",
          "experimental:type-parameter-list-spacing",
        ).joinToString(separator = ","),
      )

      ktlint(ktlintVersion)
        .setUseExperimental(true)
        .userData(data.filterKeys { it !in EDITOR_CONFIG_KEYS })
        .editorConfigOverride(data.filterKeys { it in EDITOR_CONFIG_KEYS })

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }

    format("xml") {
      target("**/res/**/*.xml")

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }

    kotlinGradle {
      target("**/*.gradle.kts", "*.gradle.kts")

      val data = mapOf(
        "indent_size" to "2",
        "ij_kotlin_imports_layout" to "*",
        "end_of_line" to "lf",
        "charset" to "utf-8"
      )
      ktlint(ktlintVersion)
        .setUseExperimental(true)
        .userData(data.filterKeys { it !in EDITOR_CONFIG_KEYS })
        .editorConfigOverride(data.filterKeys { it in EDITOR_CONFIG_KEYS })

      trimTrailingWhitespace()
      indentWithSpaces()
      endWithNewline()
    }
  }

  configure<com.vanniktech.android.junit.jacoco.JunitJacocoExtension> {
    jacocoVersion = "0.8.7"
    includeNoLocationClasses = true
    includeInstrumentationCoverageInMergedReport = true
    csv.isEnabled = false
    xml.isEnabled = true
    html.isEnabled = true
  }

  afterEvaluate {
    tasks.withType<Test> {
      extensions
        .getByType<JacocoTaskExtension>()
        .run {
          isIncludeNoLocationClasses = true
          excludes = listOf("jdk.internal.*")
        }

      testLogging {
        showExceptions = true
        showCauses = true
        showStackTraces = true
        showStandardStreams = true
        events = EnumSet.of(
          TestLogEvent.PASSED,
          TestLogEvent.FAILED,
          TestLogEvent.SKIPPED,
          TestLogEvent.STANDARD_OUT,
          TestLogEvent.STANDARD_ERROR
        )
        exceptionFormat = TestExceptionFormat.FULL
      }
    }
  }
}

allprojects {
  tasks.withType<KotlinCompile> {
    kotlinOptions {
      val version = JavaVersion.VERSION_11.toString()
      jvmTarget = version
    }
  }

  repositories {
    google()
    mavenCentral()
    maven(url = "https://jitpack.io")
    maven(url = "https://s01.oss.sonatype.org/content/repositories/snapshots/")
  }
}

tasks.register("clean", Delete::class) {
  delete(rootProject.buildDir)
}
