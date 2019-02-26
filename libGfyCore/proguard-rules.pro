# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/dekalo/Android/Sdk/tools/proguard/proguard-android.txt
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
-keep class com.gfycat.app.ApplicationIDHelperLib
-keep class com.gfycat.app.ApplicationIDHelperApp
-keepclassmembers class com.gfycat.app.ApplicationIDHelperApp {
    public *;
}

# pojo files
-keep class com.gfycat.core.downloading.pojo.** { *; }
-keep class com.gfycat.core.gfycatapi.pojo.** { *; }
-keep class com.gfycat.core.creation.pojo.** { *; }
-keep class com.gfycat.core.configuration.pojo.** { *; }
-keep class com.gfycat.core.authentication.pojo.** { *; }

# Because of RxJava-Retrofit RxJavaCallAfapterFactory issue
-keepnames class rx.Single
-keepnames class rx.Completable
