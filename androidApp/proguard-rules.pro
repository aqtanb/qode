# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-assumenosideeffects class timber.log.Timber {
    public static *** v(...);
    public static *** d(...);
}

-assumenosideeffects class co.touchlab.kermit.Logger {
    public *** v(...);
    public *** d(...);
}

# Keep Kotlin serialization metadata and generated serializers (used by type-safe Navigation routes)
-keep class com.qodein.**$$serializer { *; }
-keepclassmembers class com.qodein.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-dontwarn kotlinx.serialization.**

# Keep any class annotated with @Serializable (covers shared models and navigation routes)
-keep @kotlinx.serialization.Serializable class com.qodein.** { *; }

# Preserve Firestore DTOs so reflection-based mapping keeps working in release builds
-keep class com.qodein.core.data.dto.** { *; }
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.DocumentId <fields>;
    @com.google.firebase.firestore.ServerTimestamp <fields>;
}
