# ADB Mock GPS 🛰️

[![Android Release Build](https://github.com/lesurJ/ADB-Mock-GPS/actions/workflows/release.yml/badge.svg)](https://github.com/lesurJ/ADB-Mock-GPS/actions/workflows/release.yml)

This simple app allows setting and retrieving mock GPS locations via ADB broadcasts on an Android 📱 smartphone. While other apps exist for manually setting mock locations, automating the process is better for testing purposes. Appium also offers this feature, but it doesn’t always work reliably on older phones—hence this project.

## Table of Contents

- [ADB Mock GPS 🛰️](#adb-mock-gps-️)
  - [Table of Contents](#table-of-contents)
  - [Important Notes](#important-notes)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Usage](#usage)
    - [Examples](#examples)
    - [Testing Other Apps](#testing-other-apps)
  - [Troubleshooting](#troubleshooting)
    - [Common Issues](#common-issues)
  - [Understanding the FLAG_INCLUDE_STOPPED_PACKAGES](#understanding-the-flag_include_stopped_packages)

## Important Notes

⚠️ This app will act as a Mock Location Provider that exports **UNRESTRICTED** intents!

- The app must remain installed and not force-stopped to receive broadcasts.
- Mock locations will persist until you disable mock location app or restart the device
- Always test with known coordinates first to verify functionality

## Prerequisites

- 🔓 **Developer Options Enabled**: Your device should have Developer Options enabled

- **USB Debugging**: Enable USB debugging in Developer Options

## Installation

- 📲 **Install and Run the App**

  This app is not available in the PlayStore. You can find a prebuilt APK in the `Releases` section of this repo.

  1. From your device, download the `.apk` file.
  2. Install it.

- 🛰️ **Enable Mock Locations**

  1. Open the `ADB Mock GPS` app.
  2. If the location permissions are not granted, grant them using the appropriate `Grant` buttton.
  3. Make sure to select this app as the mock location provider in the developer menu. Click `Select in Developer Options`, open the "Select mock location app" menu and select `ADB Mock GPS` app from the list.

## Usage

To set the GPS location using adb, use this command with LATITUDE, LONGITUDE (and optionally ALTITUDE):

```bash
adb shell am broadcast -a com.adbmockgps.SET_LOCATION --es lat "LATITUDE" --es lon "LONGITUDE" [--es alt "ALTITUDE"] -f 0x01000000
```

To get the GPS location using adb, use this command:

```bash
adb shell am broadcast -a com.adbmockgps.GET_LOCATION -f 0x01000000
```

⚠️ **Note**: The `-f 0x01000000` flag is crucial! It sets `FLAG_INCLUDE_STOPPED_PACKAGES`, allowing the broadcast to reach your app even when it's not actively running. See [Understanding the FLAG_INCLUDE_STOPPED_PACKAGES](#understanding-the-flag_include_stopped_packages).

### Examples

Set location to London, UK:

```bash
adb shell am broadcast -a com.adbmockgps.SET_LOCATION --es lat "51.5074" --es lon "-0.1278" -f 0x01000000
```

Set location with altitude to Paris, France :

```bash
adb shell am broadcast -a com.adbmockgps.SET_LOCATION --es lat "48.8566" --es lon "2.3522" --es alt "35" -f 0x01000000
```

Get location

```bash
adb shell am broadcast -a com.adbmockgps.GET_LOCATION -f 0x01000000
```

Result:

```bash
Broadcasting: Intent { act=com.adbmockgps.GET_LOCATION flg=0x1400000 }
Broadcast completed: result=0, data="48.8566,2.3522,0.0"
```

### Testing Other Apps

Once your mock location is set, you can test it with:

- 🗺️ Google Maps
- 🚗 Other GPS-dependent apps
- 📍 Location-based services

The mock location will be used by all apps that request GPS coordinates.

## Troubleshooting

**Check if the app is installed and running:**

```bash
adb shell pm list packages | grep adbmockgps
```

**Check if the broadcast receiver is registered:**

```bash
adb shell dumpsys package com.adbmockgps | grep -A 5 -B 5 Receiver
```

### Common Issues

1. **"Permission denied error"**

   - Ensure the app has been granted location permissions.
   - Confirm that the app is selected as the mock location app under Developer Options.

2. **No location updates**

   - Verify ADB is connected: `adb devices`
   - Check the logcat for error messages: `adb logcat -s MockGPS`
   - Ensure the broadcast action matches exactly: `com.adbmockgps.SET_LOCATION`

## Understanding the FLAG_INCLUDE_STOPPED_PACKAGES

The `-f 0x01000000` flag is essential because:

- **Android Security**: Since Android 3.1, apps in a "stopped" state do not receive implicit broadcasts.
- **Stopped State**: Apps are considered "stopped" when not actively running or recently used
- **The Flag**: `FLAG_INCLUDE_STOPPED_PACKAGES` (0x01000000) bypasses this restriction
- **Without Flag**: `flg=0x400000` (excludes stopped packages)
- **With Flag**: `flg=0x1400000` (this includes both FLAG_INCLUDE_STOPPED_PACKAGES and FLAG_RECEIVER_FOREGROUND)
