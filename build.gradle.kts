// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.kotlin.serialization) apply false
  // KSP plugin enabled in Phase 2 when Room annotation processing is activated
  // alias(libs.plugins.kotlin.ksp) apply false
}