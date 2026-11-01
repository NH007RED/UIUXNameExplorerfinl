# Ultra Compressor R8 / ProGuard rules for UI/UX Name Explorer v16
-optimizationpasses 5
-allowaccessmodification

-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations, AnnotationDefault
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Room entities, DAOs, and database classes
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao interface * { *; }

# Keep app domain models, repositories, ViewModels, and Retrofit interfaces intact
# while allowing R8 to strip 14+ MB of unused Material Icons & library classes
-keep class com.nh.uiuxexplorer.** { *; }

# Keep Kotlin Enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Strip verbose debug logging in compressed builds
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
}

# Suppress harmless OkHttp/Conscrypt/Platform warnings during R8 full mode
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
