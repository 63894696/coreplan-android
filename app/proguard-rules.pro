# fitness-android/app/proguard-rules.pro

# Keep data classes for Gson/Room
-keep class com.example.fitness.data.** { *; }
-keep class com.example.fitness.ui.** { *; }

# Hilt
-keepattributes InnerClasses
-keepattributes *Annotation*
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }

# Room
-keep class com.example.fitness.data.** { *; }

# Gson
-keep class com.google.gson.** { *; }
-keep class com.example.fitness.data.** { *; }

# Coil
-keep class coil.** { *; }
-keep interface coil.** { *; }
