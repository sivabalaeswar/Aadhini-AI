# Aadhini — Termux Build Guide

## Prerequisites

Termux-ல இந்த commands run பண்ணுங்க:

```bash
pkg update
pkg install openjdk-17
```

## Step 1 — Project Copy

இந்த zip-ஐ download பண்ணி Termux-ல extract பண்ணுங்க:

```bash
cd ~
# Zip file Termux-ல copy பண்ணியிருந்தா:
unzip AadhiniProject.zip
cd AadhiniProject
```

## Step 2 — Storage Permission

```bash
termux-setup-storage
```
(Allow பண்ணுங்க — APK Downloads folder-ல save ஆகும்)

## Step 3 — Build

```bash
chmod +x termux_build.sh
./termux_build.sh
```

Script automatically:
- Gradle 8.2.2 download பண்ணும்
- Android SDK download பண்ணும்
- APK build பண்ணும்
- Downloads folder-ல copy பண்ணும்

## Step 4 — Install APK

1. Files app திற
2. Downloads → `Aadhini-v1.0-debug.apk` tap பண்ணு
3. Install பண்ணு (Unknown sources allow பண்ணணும்)

## Manual Build (Script-இல்லாம)

```bash
# local.properties create பண்ணு
echo "sdk.dir=$HOME/android-sdk" > local.properties

# Gradle run பண்ணு
~/.gradle-dist/gradle-8.2.2/bin/gradle \
    --no-daemon \
    :aadhini-android:assembleDebug
```

## APK Location

Build success ஆனா:
```
~/storage/downloads/Aadhini-v1.0-debug.apk
```
அல்லது:
```
~/AadhiniProject/aadhini-android/build/outputs/apk/debug/aadhini-android-debug.apk
```

## Troubleshooting

**"Java not found"**
```bash
pkg install openjdk-17
```

**"SDK not found"**
Script auto-download பண்ணும். Internet connection இருக்கணும்.

**"Permission denied"**
```bash
chmod +x termux_build.sh
```

**Gradle memory error**
```bash
export GRADLE_OPTS="-Xmx1g"
./termux_build.sh
```
