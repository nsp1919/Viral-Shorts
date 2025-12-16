# ProGuard rules for AutoShorts
# Keep WebView JavaScript interface
-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep WebView classes
-keep class android.webkit.** { *; }

# Keep app classes
-keep class com.nspcreativehub.autoshorts.** { *; }
