# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Chris Boyd\AppData\Local\Android\Sdk/tools/proguard/proguard-android-optimize.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Keep Hilt generated classes
-keep class dagger.hilt.internal.aggregatedroot.codegen.*
-keep class com.example.electricianappnew.Hilt_*.** { *; }
-keep class *..HiltModules_* { *; }
-keep class *..*_*Factory { *; }
-keep class *..*_*MembersInjector { *; }

# Keep Room generated classes
-keep class androidx.room.** { *; }
-keep class com.example.electricianappnew.data.local.** { *; }
-keep class com.example.electricianappnew.data.model.** { *; }

# Keep Coroutines specific classes
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.flow.** { *; }
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-keepclassmembers class ** {
    kotlin.coroutines.Continuation **;
}
-keepclassmembers class kotlin.coroutines.jvm.internal.BaseContinuationImpl {
    kotlin.coroutines.Continuation continuation;
}

# Keep application class
-keep public class com.example.electricianappnew.ElectricianApp { *; }

# Keep Activities, Services, BroadcastReceivers, ContentProviders
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService

# Keep custom Views
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# Keep R classes
-keep class **.R$* { *; }

# Keep annotation classes
-keep @interface androidx.annotation.Keep
-keep @androidx.annotation.Keep class *
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# Keep specific classes that might be accessed via reflection
# -keep public class my.package.MyClass

# Ignore warnings about specific libraries if necessary
# -dontwarn com.example.library.**
