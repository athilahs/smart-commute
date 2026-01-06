# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in $ANDROID_HOME/tools/proguard/proguard-android.txt

# ========================================
# SmartCommute ProGuard Rules
# ========================================

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# Retrofit with Kotlin Coroutines
-if interface * { @retrofit2.http.* public *** *(...); }
-keep,allowobfuscation interface <1>

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Keep DTOs for Gson deserialization
-keep class com.smartcommute.feature.linestatus.data.remote.dto.** { *; }
-keepclassmembers class com.smartcommute.feature.linestatus.data.remote.dto.** { *; }
-keep class com.smartcommute.feature.linedetails.data.remote.dto.** { *; }
-keepclassmembers class com.smartcommute.feature.linedetails.data.remote.dto.** { *; }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep Room entities
-keep class com.smartcommute.feature.linestatus.data.local.entity.** { *; }
-keepclassmembers class com.smartcommute.feature.linestatus.data.local.entity.** { *; }
-keep class com.smartcommute.feature.statusalerts.data.local.** { *; }
-keepclassmembers class com.smartcommute.feature.statusalerts.data.local.** { *; }
-keep class com.smartcommute.feature.linedetails.data.local.** { *; }
-keepclassmembers class com.smartcommute.feature.linedetails.data.local.** { *; }

# Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep class com.bumptech.glide.GeneratedAppGlideModuleImpl
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.smartcommute.**$$serializer { *; }
-keepclassmembers class com.smartcommute.** {
    *** Companion;
}
-keepclasseswithmembers class com.smartcommute.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-dontwarn com.google.errorprone.annotations.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Keep domain models that might be used with reflection
-keep class com.smartcommute.feature.linestatus.domain.model.** { *; }
-keep class com.smartcommute.feature.linedetails.domain.model.** { *; }
-keep class com.smartcommute.feature.statusalerts.domain.model.** { *; }

# Keep BroadcastReceivers for alarm system
-keep class com.smartcommute.feature.statusalerts.data.receiver.** { *; }

# Keep custom exceptions
-keep public class * extends java.lang.Exception

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
