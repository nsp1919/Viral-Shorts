# AutoShorts Android App

A WebView wrapper for the AutoShorts web application.

## Prerequisites

- Android Studio Hedgehog (2023.1.1) or later
- JDK 17 or later
- Android SDK 34

## Setup Instructions

### 1. Open in Android Studio

1. Open Android Studio
2. Select "Open" and navigate to `Auto Post App/android`
3. Wait for Gradle sync to complete

### 2. Update Web App URL

Before building, update the web app URL in `MainActivity.kt`:

```kotlin
companion object {
    private const val WEB_APP_URL = "https://your-vercel-app.vercel.app"
}
```

### 3. Build APK

#### Debug Build (for testing)
```bash
./gradlew assembleDebug
```
APK location: `app/build/outputs/apk/debug/app-debug.apk`

#### Release Build (for distribution)
1. Create a signing key (if you don't have one):
```bash
keytool -genkey -v -keystore autoshorts-release.keystore -alias autoshorts -keyalg RSA -keysize 2048 -validity 10000
```

2. Add signing config to `app/build.gradle.kts` or use Android Studio's Build → Generate Signed Bundle/APK

## Features

- ✅ Full WebView with JavaScript enabled
- ✅ Video file upload from device storage
- ✅ Download processed videos to Downloads folder
- ✅ Splash screen with app branding
- ✅ Dark theme matching web app
- ✅ Back button navigation in WebView
- ✅ Loading progress indicator
- ✅ Error handling with retry option

## Permissions

- **INTERNET**: Load web content
- **READ_EXTERNAL_STORAGE**: Select videos for upload (Android 12 and below)
- **READ_MEDIA_VIDEO**: Select videos for upload (Android 13+)
- **WRITE_EXTERNAL_STORAGE**: Download videos (Android 9 and below)

## Troubleshooting

### "Unable to connect" error
- Ensure the backend and frontend are deployed and accessible
- Check if the device has internet connectivity
- Verify the `WEB_APP_URL` is correct

### File upload not working
- Grant storage permissions when prompted
- On Android 13+, grant "Photos and videos" permission

### Downloads not appearing
- Check Downloads folder on device
- Ensure "Allow downloads" is enabled in system settings

## Package Information

- **Package Name**: `com.nspcreativehub.autoshorts`
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
