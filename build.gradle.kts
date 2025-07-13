// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.spotless) apply false
    alias(libs.plugins.android.library) apply false
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
            ))
        }
        kotlinGradle {
            target("*.kts")
            ktlint()
        }
        format("misc") {
            target("**/*.md", "**/*.gradle", "**/.gitignore")
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }
    }

    afterEvaluate {
        tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
            finalizedBy("spotlessApply")
        }
    }
}