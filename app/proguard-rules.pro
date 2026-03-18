# Keep source file names and line numbers for readable crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep all annotations (required by Gson, Room, Compose)
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

# ---- Gson ----
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }
# Keep data classes used for JSON parsing
-keep class app.krafted.chickquiz.data.questions.Question { *; }
-keep class app.krafted.chickquiz.data.questions.QuestionBank { *; }
# Generic type safety for Gson
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# ---- Room ----
# Keep all entity, DAO, and database classes
-keep class app.krafted.chickquiz.data.db.** { *; }
# Room-generated _Impl classes
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# ---- Kotlin ----
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings { <fields>; }
-keepclassmembers class kotlin.Lazy { *; }

# ---- Coroutines ----
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-dontwarn kotlinx.coroutines.**

# ---- Compose ----
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }

# ---- AndroidX / Lifecycle ----
-keep class androidx.lifecycle.** { *; }
-keep class androidx.navigation.** { *; }

# ---- Enum classes ----
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
