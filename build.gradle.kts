// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.kotlin.multiplatform) apply false
    alias(libs.plugins.android.kotlin.multiplatform.library) apply false
}

subprojects {
    apply(plugin = "com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {
        kotlin {
            target("src/**/*.kt")
            targetExclude("build/**/*.kt")
            ktlint().editorConfigOverride(mapOf(
                "max_line_length" to "140",
                "ij_kotlin_allow_trailing_comma" to "false",
                "ktlint_standard_filename" to "disabled",
                "ij_kotlin_line_break_after_multiline_when_entry" to "false",
                "ktlint_standard_no-empty-first-line-in-method-block" to "enabled",
                "ktlint_function_signature_body_expression_wrapping" to "multiline",
                "ktlint_function_signature_rule_force_multiline_when_parameter_count_greater_or_equal_than" to "2",
                "ktlint_function_naming_ignore_when_annotated_with" to "Composable, Test",
                "ktlint_standard_value-argument-comment" to "disabled",
                "ktlint_standard_value-parameter-comment" to "disabled",
                "ktlint_standard_no-unused-imports" to "enabled",
                "ktlint_standard_import-ordering" to "enabled",
                "ktlint_standard_no-wildcard-imports" to "enabled",
                "ij_kotlin_imports_layout" to "*,java.**,javax.**,kotlin.**,^",
            ))
        }
        kotlinGradle {
            target("*.kts")
            ktlint()
        }
        format("misc") {
            target("**/*.md", "**/*.gradle", "**/.gitignore")
            trimTrailingWhitespace()
            endWithNewline()
        }
    }

    // Custom task to check for fully qualified class names in code
    tasks.register("checkFullyQualifiedNames") {
        group = "verification"
        description = "Check for fully qualified class names in Kotlin source files"

        doLast {
            val sourceFiles = fileTree("src") {
                include("**/*.kt")
                exclude("build/**")
            }

            val fullyQualifiedPattern = Regex("""(?<!import\s)(?<!@file:)(?<!\*\s)(?<!//.*)\b([a-z]+\.){2,}[A-Z][a-zA-Z0-9]*""")
            var violationsFound = false

            sourceFiles.forEach { file ->
                val lines = file.readLines()
                lines.forEachIndexed { index, line ->
                    // Skip import statements and comments
                    if (!line.trimStart().startsWith("import ") &&
                        !line.trimStart().startsWith("//") &&
                        !line.trimStart().startsWith("*") &&
                        !line.contains("@file:")) {

                        fullyQualifiedPattern.findAll(line).forEach { match ->
                            println("ERROR: Fully qualified class name found in ${file.relativeTo(rootDir)}:${index + 1}")
                            println("   Line: ${line.trim()}")
                            println("   Found: ${match.value}")
                            println("   Fix: Add proper import statement and use simple class name")
                            println()
                            violationsFound = true
                        }
                    }
                }
            }

            if (violationsFound) {
                throw GradleException("Fully qualified class names found in code. Please use proper import statements instead.")
            } else {
                println("SUCCESS: No fully qualified class names found in code bodies")
            }
        }
    }

    afterEvaluate {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            finalizedBy("spotlessApply")
            compilerOptions {
                freeCompilerArgs.add("-opt-in=kotlin.time.ExperimentalTime")
            }
        }

        // Run the FQN check as part of the check task
        tasks.named("check") {
            dependsOn("checkFullyQualifiedNames")
        }
    }
}
