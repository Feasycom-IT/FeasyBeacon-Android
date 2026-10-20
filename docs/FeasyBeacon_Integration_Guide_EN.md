# FeasyBeacon Android SDK Integration Guide (AAR + Demo)

> **Release package**: `feasybeacon-sdk-android-3.3.9.9.zip`
> **SDK AAR**: `FeasyBeaconLibrary-release.aar`
> **SDK Java package**: `com.feasycom`
> **Demo applicationId**: `com.feasycom.feasybeacon`
> **Minimum Android version for the AAR**: Android 8.0 (API 26)
> **Minimum Android version for the demo**: Android 10 (API 29)
> **Supported ABIs**: `arm64-v8a`, `armeabi-v7a`
> **Document verification date**: 2026-10-20

This document was verified against the current `3.3.9.9` release package and demo source code. The SDK package version follows the app's `versionName`; the AAR's internal library module version is `3.3.8.7`. This mismatch is the current packaging convention, not a misplaced file.

## Table of Contents

1. Release Package
2. Requirements
3. Integration Steps
4. Initialization
5. Common Workflows
6. API Reference
7. Demo Guide
8. Troubleshooting
9. Integration Checklist
10. Document Notes

---

## 1. Release Package

### 1.1 Archive Layout

```text
feasybeacon-sdk-android-3.3.9.9/
├── apk/
│   └── FeasyBeacon_V3.3.9.9.apk
├── sdk/
│   ├── FeasyBeaconLibrary-release.aar
│   ├── arm64-v8a/
│   │   └── libFeasyBeaconLibrary.so
│   └── armeabi-v7a/
│       └── libFeasyBeaconLibrary.so
└── demo/
    ├── settings.gradle
    ├── build.gradle
    ├── gradle.properties
    ├── gradlew
    ├── gradlew.bat
    ├── gradle/wrapper/
    └── feasybeacon/
        ├── build.gradle
        ├── proguard-rules.pro
        ├── libs/
        │   ├── FeasyBeaconLibrary-release.aar
        │   ├── arm64-v8a/libFeasyBeaconLibrary.so
        │   └── armeabi-v7a/libFeasyBeaconLibrary.so
        └── src/main/...
```

> This repository is the public app-layer repository. The directory mapping is: `feasybeacon/` corresponds to `demo/feasybeacon/` in the release package; `apk/` contains the release APK; `feasybeacon/libs/` contains the AAR. The `sdk/` directory from the release package does not need to be added to this repository.

The three top-level directories serve different purposes:

| Directory | Purpose |
|---|---|
| `apk/` | Prebuilt reference app for comparing scanning, connection, parameter configuration, and update behavior |
| `sdk/` | The AAR and native libraries that integrators actually need |
| `demo/` | Complete app source, already configured to use the SDK through the local AAR, and ready to open in Android Studio |

Note: `demo/` is not a minimal integration sample; it is the complete FeasyBeacon app source. It includes networking, Room, ads, forum, feedback, and other business code. Do not copy all of the demo's dependencies and manifest declarations when integrating the SDK.

### 1.2 AAR Structure

Measured contents of the current AAR:

| Item | Size | Description |
|---|---:|---|
| `classes.jar` | 306,463 B | 185 `.class` entries (including inner classes) |
| `jni/arm64-v8a/libFeasyBeaconLibrary.so` | 31,792 B | 64-bit native library |
| `jni/armeabi-v7a/libFeasyBeaconLibrary.so` | 21,876 B | 32-bit native library |
| `AndroidManifest.xml` | 1,558 B | Package name `com.feasycom`, `minSdkVersion 26` |
| `proguard.txt` | 0 B | **Empty file; integrators must add their own keep rules** |
| `res/` | Multiple files | SUOTA-related layouts, colors, strings, and image resources |
| `R.txt` | 275,368 B | Resource ID listing |

Current AAR checksums:

| Algorithm | Value |
|---|---|
| SHA-256 | `D48CE9F710C66EDF3016BAE1A897F09F3A25701AEAFF0FDC2C7D41DEDE5F7F1B` |
| MD5 | `E5730E90D01B371CA1BEC5086C915159` |

Hashes change on every rebuild; use the actual delivered file as the source of truth.

### 1.3 What the AAR Provides

**The AAR provides:**

- BLE scanning, connection, disconnection, and GATT read/write operations
- PIN authentication and encryption verification
- AT command queue and parameter read/write support
- Advertisement parsing for iBeacon, Eddystone, AltBeacon, AOA, and sensor data
- Beacon advertising slot and conditional advertising slot management
- DFU update service and packetized transfer
- SUOTA update protocol, base Activity, and related resources
- Common data classes: `BluetoothDeviceWrapper`, `FeasyBeacon`, `BeaconBean`, `CommandBean`, `DfuFileInfo`, and others


---

## 2. Requirements

| Item | Requirement |
|---|---|
| `minSdk` | 26 for the AAR; 29 for the demo |
| `compileSdk` | The demo uses 35; AAR metadata specifies `minCompileSdk=1` |
| `targetSdk` | The demo uses 35 |
| JDK | 17 |
| Gradle | The demo uses 8.13 |
| Android Gradle Plugin | The demo uses 8.13.0 |
| Kotlin | The demo uses 1.9.20. The AAR contains Kotlin-compiled `OTAService` and exception classes, so `kotlin-stdlib` is required |
| ABI | Only `arm64-v8a` and `armeabi-v7a`; x86/x86_64 emulators cannot run the native library |
| Runtime environment | The device must support BLE, Bluetooth must be enabled, and location services and permissions must meet OS requirements |


### 2.1 Dependencies

The AAR does not include a Maven POM and therefore does not bring in third-party dependencies automatically. At a minimum, core scanning and configuration require:

- `androidx.annotation`
- `org.jetbrains.kotlin:kotlin-stdlib`
- `androidx.appcompat` (the AAR's SUOTA resources reference AppCompat styles)

If you use SUOTA, the following dependencies are also recommended:

- `androidx.lifecycle`
- `androidx.fragment`
- `androidx.documentfile`

The most reliable approach is to start from the dependency set in `demo/feasybeacon/build.gradle` and trim it to your actual feature set.

---

## 3. Integration Steps

### 3.1 Adding the AAR and SO Files

**Option A: AAR only (recommended)**

The current AAR's `jni/` directory already contains `libFeasyBeaconLibrary.so` for both ABIs. If you use only the AAR, you normally do not need to copy the `.so` files separately.

Place the AAR in the app module's `libs/` directory:

```text
app/libs/FeasyBeaconLibrary-release.aar
```

**Option B: AAR plus external SO files (legacy integration)**

If your project already has a centralized process for managing `.so` files, you can also copy both:

```text
app/libs/FeasyBeaconLibrary-release.aar
app/libs/arm64-v8a/libFeasyBeaconLibrary.so
app/libs/armeabi-v7a/libFeasyBeaconLibrary.so
```

Then declare the JNI library directory in `build.gradle`:

```gradle
android {
    sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
        }
    }
}
```

Do not rename the `.so` files or replace them with libraries from another version. The JNI symbols depend on the fixed library name `FeasyBeaconLibrary`.

### 3.2 Gradle Configuration

```gradle
android {
    compileSdk 35

    defaultConfig {
        minSdk 26
        targetSdk 35

        ndk {
            abiFilters 'arm64-v8a', 'armeabi-v7a'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }
}

dependencies {
    implementation fileTree(dir: "libs", include: ["*.jar", "*.aar"])

    implementation "androidx.appcompat:appcompat:1.3.0"
    implementation "androidx.annotation:annotation:1.3.0"
    implementation "org.jetbrains.kotlin:kotlin-stdlib:1.9.20"

    // Recommended when using SUOTA
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2"
    implementation "androidx.fragment:fragment-ktx:1.6.2"
    implementation "androidx.documentfile:documentfile:1.0.1"
}
```

The dependency versions above are examples; align them with your existing project. The demo uses `appcompat 1.3.0` and `lifecycle 2.6.2` but does not declare `documentfile` separately. If your build cannot find `DocumentFile`, add `androidx.documentfile:documentfile` explicitly.

### 3.3 AndroidManifest Configuration

#### 3.3.1 Permissions Automatically Merged by the AAR

The AAR declares the following permissions, which are normally merged into the app manifest:

```xml
<uses-feature android:name="android.hardware.bluetooth_le" android:required="true" />

<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_LOCATION_EXTRA_COMMANDS" />
<uses-permission android:name="android.permission.BLUETOOTH_PRIVILEGED" />
<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
```

If some permissions are not needed, remove them from the app manifest with `tools:node="remove"`. For example:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <uses-permission
        android:name="android.permission.SYSTEM_ALERT_WINDOW"
        tools:node="remove" />

    <uses-permission
        android:name="android.permission.BLUETOOTH_PRIVILEGED"
        tools:node="remove" />
</manifest>
```

`BLUETOOTH_PRIVILEGED` and `SYSTEM_ALERT_WINDOW` are sensitive or protected permissions and are generally unavailable to ordinary apps. Basic scanning and parameter configuration do not require them.

The demo manifest also declares `WRITE_SECURE_SETTINGS`, `CAMERA`, and other permissions. `WRITE_SECURE_SETTINGS` is a protected permission and is not required for basic scanning or configuration. If you are only integrating the SDK, do not blindly copy all of the demo's permission declarations.

#### 3.3.2 Required Service Declarations

This is the most common integration issue. The AAR manifest does not declare `AtCommandService` or `OTAService`; the app must declare them itself:

```xml
<service
    android:name="com.feasycom.service.AtCommandService"
    android:enabled="true"
    android:exported="false" />

<service
    android:name="com.feasycom.service.OTAService"
    android:enabled="true"
    android:exported="false" />
```

Parameter read/write depends on `AtCommandService`, and DFU updates depend on `OTAService`. The SDK binds these services internally; the host app does not need to start them manually.

#### 3.3.3 Optional Permissions

```xml
<!-- Required for phone-as-a-beacon advertising -->
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />

<!-- Required for QR code scanning -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- Required for networking, firmware download, feedback, and similar features -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

On Android 12 (API 31) and above, if you do not derive location from scan results, add `neverForLocation` to `BLUETOOTH_SCAN`:

```xml
<uses-permission
    android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation"
    tools:node="replace" />
```

#### 3.3.4 Runtime Permissions

The SDK does not request core scan/connect permissions on behalf of the app. You typically need:

- Android 12 and above: `BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`
- Android 11 and below: location permission, and the system location switch must be enabled
- Phone-as-a-beacon advertising: `BLUETOOTH_ADVERTISE`

**Without the required permissions, scanning usually produces no callbacks**, which is easily mistaken for an SDK failure. The demo uses XXPermissions; integrators can also use `ActivityResultContracts.RequestMultiplePermissions`.

### 3.4 ProGuard/R8 Rules

The AAR's `proguard.txt` is empty. If the app enables `minifyEnabled true`, add the following rules:

```proguard
-keep public class com.feasycom.bean.** { *; }
-keep public class com.feasycom.controler.** { *; }
-keep public class com.feasycom.encrypted.** { *; }
-keep public class com.feasycom.service.** { *; }
-keep public class com.feasycom.suotalib.** { *; }
-keep public class com.feasycom.util.** { *; }
-keep public class com.feasycom.error.** { *; }
-keep public class com.feasycom.BuildConfig { *; }
```

Note that `controler` is the fixed spelling used by the library, not `controller`.

The repository's `proguard-rules.pro` is only a template and cannot replace the SDK keep rules above; the AAR's own `proguard.txt` is empty.

### 3.5 Running This Repository

1. Open the repository root in Android Studio.
2. Use JDK 17, Gradle 8.13, and Android Gradle Plugin 8.13.0.
3. Wait for Gradle Sync to complete.
4. Run the app on a BLE-capable physical device.

This repository has removed `signingConfigs`: debug builds use the Android default debug signature, and release builds produce an unsigned APK by default. To produce a signed build, configure your own keystore locally and do not commit the keystore or passwords.

The repository does not include `local.properties`; Android Studio generates it automatically. If the SDK path is incorrect, follow the IDE prompt to configure the Android SDK.

---

## 4. Initialization

### 4.1 Three-Step Initialization

```kotlin
val api = FscBeaconCentralApiImp.getInstance(context.applicationContext).apply {
    isShowLog(BuildConfig.DEBUG)
    initialize()
    setCallbacks(callbacks)
}
```

All three steps are required:

1. `getInstance(context)`: the first call must receive a `Context`; preferably use `applicationContext`.
2. `initialize()`: initializes the BLE scanner and GATT environment.
3. `setCallbacks(callbacks)`: registers callbacks for scanning, connection, AT commands, OTA, and related events.

The no-argument `getInstance()` overload only returns the existing singleton and does not populate `Context`. If the overload that accepts a `Context` has never been called, subsequent `connect()` calls return `false`, and `isBluetoothEnabled()` may throw a null-pointer exception.

Wrap the SDK in a global singleton, such as the demo's `BluetoothRepository`, and let it dispatch callbacks to screens. Do not let each screen hold its own `FscBeaconCentralApiImp`.

### 4.2 Complete Callback Stub Template

`FscBeaconCentralCallbacks` extends `FscBleCentralCallbacks` and has many methods. The following Kotlin template can be copied and adapted as needed:

```kotlin
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattService
import com.feasycom.bean.BluetoothDeviceWrapper
import com.feasycom.controler.beacon.FscBeaconCentralCallbacks
import java.util.ArrayList

class BeaconCallbacks : FscBeaconCentralCallbacks {

    override fun startScan() {}

    override fun stopScan() {}

    override fun connectProgressUpdate(device: BluetoothDevice?, status: Int) {}

    override fun blePeripheralFound(
        device: BluetoothDevice,
        wrapper: BluetoothDeviceWrapper?,
        rssi: Int,
        record: ByteArray?
    ) {
    }

    override fun blePeripheralConnected(gatt: BluetoothGatt?, device: BluetoothDevice?) {}

    override fun blePeripheralDisconnected(gatt: BluetoothGatt?, device: BluetoothDevice?) {}

    override fun atCommandCallBack(command: String?, param: String?, status: String?) {}

    override fun servicesFound(
        gatt: BluetoothGatt?,
        device: BluetoothDevice?,
        services: ArrayList<BluetoothGattService>?
    ) {
    }

    override fun characteristicForService(
        gatt: BluetoothGatt?,
        device: BluetoothDevice?,
        service: BluetoothGattService?,
        characteristic: BluetoothGattCharacteristic?
    ) {
    }

    override fun packetReceived(
        gatt: BluetoothGatt?,
        device: BluetoothDevice?,
        service: BluetoothGattService?,
        ch: BluetoothGattCharacteristic?,
        strValue: String?,
        hexString: String?,
        rawValue: ByteArray?,
        timestamp: String?
    ) {
    }

    override fun readResponse(
        gatt: BluetoothGatt?,
        device: BluetoothDevice?,
        service: BluetoothGattService?,
        ch: BluetoothGattCharacteristic?,
        strValue: String?,
        hexString: String?,
        rawValue: ByteArray?,
        timestamp: String?
    ) {
    }

    override fun sendPacketProgress(
        gatt: BluetoothGatt?,
        device: BluetoothDevice?,
        ch: BluetoothGattCharacteristic?,
        percentage: Int,
        sendByte: ByteArray?
    ) {
    }

    override fun otaProgressUpdate(percentage: Int, status: Int) {}

    override fun onDeviceInfoReceived(parameterName: String?, parameter: Any?) {}
}
```

### 4.3 Callback Threading

Scan, GATT, and AT callbacks are not guaranteed to run on the main thread. Typical practice:

- In `blePeripheralFound()`, only parse data and update lists; switch to the main thread when needed.
- Do not perform long-running work in `onDeviceInfoReceived()`.
- In `onAtCommandCallBack()`, only update state or UI.

### 4.4 Recommended Call Sequence

```text
getInstance(context)
  → initialize()
  → setCallbacks()
  → startScan()
  → blePeripheralFound()
  → connect(wrapper, pin)
  → connectProgressUpdate(1)
  → blePeripheralConnected()
  → getDeviceInfo(feasyBeacon, true)
  → onDeviceInfoReceived("BADVDATA" / "SADV")
  → setDeviceName()/setDevicePin()/setBroadcastInterval()...
  → saveBeaconInfo(currentBeaconList, currentConditionList)
  → onAtCommandCallBack(..., "2")
  → disconnect()
```

If you only need to scan and display devices, no connection is required. To modify parameters or perform an update, wait until the connection succeeds and device information has been queried.

---

## 5. Common Workflows

### 5.1 Scanning

```kotlin
if (!api.isBluetoothEnabled) {
    // Prompt the user to enable Bluetooth
    return
}

api.startScan()
```

Scan results are delivered through `blePeripheralFound()`:

```kotlin
override fun blePeripheralFound(
    device: BluetoothDevice,
    wrapper: BluetoothDeviceWrapper?,
    rssi: Int,
    record: ByteArray?
) {
    val d = wrapper ?: return

    when {
        d.feasyBeacon != null -> {
            // Configurable FeasyBeacon module
        }
        d.iBeacon != null -> {
            // iBeacon advertisement
        }
        d.eddystoneBeacon != null -> {
            // Eddystone UID / URL / TLM
        }
        d.altBeacon != null -> {
            // AltBeacon advertisement
        }
        d.aoaBeacon != null -> {
            // AOA advertisement
        }
    }

    d.thSensor?.let {
        // Temperature/humidity sensor data
    }
    d.gSensor?.let {
        // G-sensor data
    }
}
```

The scan implementation has several important details:

- `startScan(int duration)` currently ignores `duration` and calls `stopScan()` after a fixed 90-second interval. Call `stopScan()` yourself for precise control.
- `startScan()` returning `true` only means the call flow completed; it does not guarantee that a device was found. Rely on callbacks and the resulting list.
- `iBeacon`, `eddystoneBeacon`, `altBeacon`, and `aoaBeacon` are mutually exclusive. Setting one clears the other three.
- `thSensor`, `gSensor`, and `feasyBeacon` are additional information and can coexist with an advertisement type.

### 5.2 Connecting and PIN Authentication

```kotlin
val ok = api.connect(wrapper, "000000")
```

Common reasons for `connect()` to return `false`:

- `getInstance(context)` was not called first, so the SDK's internal `context` is `null`.
- `wrapper.getFeasyBeacon()` is `null`, meaning the device is not a configurable module.
- The PIN is not numeric.
- The PIN is empty or longer than six digits.

Connection progress is reported through `connectProgressUpdate(device, status)`:

| status | Meaning |
|---:|---|
| `0` | PIN verification in progress |
| `1` | PIN correct |
| `2` | PIN incorrect |
| `3` | Timeout |

Disconnecting:

```kotlin
api.disconnect("Business screen exiting")
```

The parameter of `disconnect(String loca)` is only a log tag, not a device address; pass any descriptive string. After disconnecting, the SDK clears its internal device name, module number, version, encryption mode, and advertising-slot state. Call `getDeviceInfo()` again after reconnecting.

The SDK does not provide automatic reconnection; the app layer must handle reconnection and retries.

### 5.3 Reading Device Information

```kotlin
api.getDeviceInfo(wrapper.feasyBeacon!!, true)
```

The second parameter, `true`, also queries TX Power. The result is not returned as a single object; it is delivered one parameter at a time through `onDeviceInfoReceived(parameterName, parameter)`.

Common parameter names and value types:

| parameterName | Typical type | Meaning |
|---|---|---|
| `Opened` | None or string | Connection or query started |
| `VERSION` | `String` | Firmware version |
| `NAME` | `String` | Device name |
| `PIN` | `String` | PIN |
| `ADVIN` | `String` | Advertising interval |
| `TXPOWER` | `String` | TX Power level or encoding |
| `KEYCFG` | `String[]` | Key interval and duration |
| `GSCFG` | `String[]` | G-sensor interval and duration; some modules also include a threshold |
| `BADVDATA` | `ArrayList<BeaconBean>` | Normal advertising slots |
| `SADV` | `ArrayList<BeaconBean>` | Conditional advertising slots |
| `BADVPARM` | `String` | Advertising parameters, comma-separated |
| `TLM` | `String` | `"0"` / `"1"` |
| `AOA` | `String` | `"0"` / `"1"` |
| `ADVEXT` | `String` | Extended advertising switch |
| `EXTEND` / `BWMODE` / `LED` / `BUZ` | `String` | Extended information or peripheral state |

`parameter` is an `Object`; cast it based on the parameter name before use. Do not perform long-running work in the callback, or subsequent AT queries will be affected.

Common `FeasyBeacon` fields:

| Field | Meaning |
|---|---|
| `module` | Module model number, as a string |
| `version` | Firmware version |
| `deviceName` | Device name |
| `deviceAddress` | Device address |
| `battery` | Battery level |
| `connectable` | Whether the device is connectable |
| `keyConfig` | Whether key configuration is supported |
| `gSensor` | Whether G-sensor is supported |
| `buzzer` | Whether a buzzer is supported |
| `led` | Whether LED is supported |
| `nfc` | Whether NFC is supported |
| `longRange` | Whether long-range mode is supported |
| `encryptionWay` | Encryption mode |

### 5.4 Modifying Parameters

Common setters:

```kotlin
api.setDeviceName("FeasyBeacon-001")
api.setDevicePin("123456")
api.setBroadcastInterval("200")
api.setDeviceTxPower("7")
api.setGSensorConfig("100", "5")
api.setKeyConfig("100", "5")
api.setLed("10")
```

These setters only assemble AT commands into `CommandBean`; **they do not send the commands immediately**. You must finally call:

```kotlin
api.saveBeaconInfo(currentBeaconList, currentConditionList)
```

Or, for batch commands, use:

```kotlin
api.saveBatch(commandBean)
```

Wait until `onAtCommandCallBack()` reports one of the following:

| status | Meaning |
|---|---|
| `"0"` | Failed |
| `"1"` | Succeeded |
| `"2"` | All commands completed |
| `"3"` | No change required |
| `"4"` | Timed out |

**Important: `saveBeaconInfo()` not only saves parameters such as the name and PIN; it also writes all 10 normal advertising slots and all 10 conditional advertising slots.**

Therefore, the correct sequence is:

1. Call `getDeviceInfo()` after a successful connection.
2. Wait for the `BADVDATA` and `SADV` callbacks to obtain the current slot data.
3. Modify parameters such as the name, PIN, and interval.
4. Pass the current slot lists to `saveBeaconInfo()`.

If you skip step 2 and pass empty or uninitialized lists, you may clear the device's existing advertising slots.

Other behavior:

- `setFirmwareKey()` sends the AT command immediately.
- `restore()` sends the factory-reset command immediately.
- `beaconBroadcastParameters()` queries the advertising parameters immediately.
- `setAppKey()` only writes the key to the app's local SharedPreferences; it does not write it to the device.
- `setTlm()`, `setAoa()`, and `setBroadcastExtensions()` assemble commands and still require `saveBeaconInfo()` to send them.
- `setBroadcastTxPower()` and `setBroadcastPHY()` only modify the in-memory `BeaconBean` and also require saving.

### 5.5 Managing Advertising Slots

After a successful connection, the SDK initializes 10 normal slots and 10 conditional slots:

```kotlin
val bean = BeaconBean("1", FeasyBeacon.BEACON_TYPE_I_BEACON).apply {
    uuid = "FDA50693-A4E2-4FB1-AFCF-C6EB07647825"
    major = "10001"
    minor = "10002"
    rssi = "2"
    txpower = "2"
    phy = "0"
    interval = 1000
    enable = true
    connectable = true
}

if (!api.isBeaconInfoFull) {
    api.addBeaconInfo(bean)
}
```

Common methods:

| Method | Description |
|---|---|
| `isBeaconInfoFull()` | Whether the normal slots are full |
| `isConditionBeaconInfoFUll()` | Whether the conditional slots are full (note the `FUll` spelling in the method name) |
| `addBeaconInfo(bean)` | Fills the first empty slot and reindexes the slots |
| `addConditionBeaconInfo(bean)` | Fills the first empty conditional slot |
| `updateBeaconInfo(bean, index)` | Updates by one-based index |
| `deleteBeaconInfo(index)` | Deletes by one-based index |
| `deleteConditionBeaconInfo(index)` | Deletes a conditional slot by one-based index |
| `saveBeaconInfo(...)` | Sends slots and parameters together |

Common advertising type constants:

| Constant | Value | Description |
|---|---|---|
| `BEACON_TYPE_I_BEACON` | `iBeacon` | iBeacon |
| `BEACON_TYPE_EDDYSTONE_URL` | `URL` | Eddystone URL |
| `BEACON_TYPE_EDDYSTONE_UID` | `UID` | Eddystone UID |
| `BEACON_TYPE_EDDYSTONE_TLM` | `TLM` | Eddystone TLM |
| `BEACON_TYPE_ALTBEACON` | `AltBeacon` | AltBeacon |
| `BEACON_TYPE_AOABEACON` | `AoaBeacon` | AOA |
| `BEACON_TYPE_CUSTOM` | `CustomBeacon` | Custom advertising |

Conditional advertising uses the `CONDITION_*` prefix, for example `CONDITION_iBeacon`, `CONDITION_URL`, `CONDITION_UID`, `CONDITION_AltBeacon`, and `CONDITION_CustomBeacon`.

Note: in this AAR version, the AOA slot-writing branch in `addBeaconInfo()` is commented out; the AOA switch `setAoa()` is still available. Confirm the actual supported mechanism with the module firmware before integrating AOA.

Indexing rules can be confusing:

- `BeaconBean.index` starts at `1`.
- `deleteBeaconInfo(index)` and `updateBeaconInfo(bean, index)` match `BeaconBean.index`.
- `setBroadcastTxPower(index, ...)` and `setBroadcastPHY(index, ...)` use the list index, starting at `0`.

### 5.6 Batch Configuration

```kotlin
val cmd = CommandBean()
cmd.setCommand("AT+NAME=FeasyBeacon-002")
cmd.setCommand("AT+ADVIN=200")
api.saveBatch(cmd)
```

`saveBatch()` immediately sends the commands contained in `CommandBean`.

Note that `CommandBean` stores commands in a `HashSet`, so iteration order is not guaranteed. If multiple AT commands have strict ordering dependencies, do not rely on its iteration order; confirm each command individually or implement serial sending in the app layer.

### 5.7 DFU/OTA

#### 5.7.1 Parsing Firmware

```kotlin
val info = api.checkDfuFile(dfuBytes) ?: return

val targetModule = info.type_model
val startVersion = info.versonStart
val endVersion = info.verson_soft_end
val bootloader = info.bootloader
```

`checkDfuFile()` may return `null`; always null-check before use.

The SDK only accepts firmware as a `byte[]` and does not download files from the network. The demo downloads firmware from `https://dfu.feasycom.com/{file_name}.dfu`. Integrators can reuse this flow or use their own firmware distribution method.

#### 5.7.2 Starting the Update

`startOtaUpdate()` requires an active connection and a non-null `dfuFile`. Internally, it will:

1. Decrypt the firmware.
2. Strip the 1024-byte firmware header.
3. Send the `EnterDFU` command.
4. Bind `OTAService` and start packetized transmission.

Therefore, do not disconnect before calling it. Recommended flow:

```kotlin
// 1. Connect to the device
api.connect(wrapper, "000000")

// 2. Start OTA in the connection-success callback
override fun blePeripheralConnected(gatt: BluetoothGatt?, device: BluetoothDevice?) {
    api.startOtaUpdate(dfuBytes, false)
}
```

If your app needs to enter the OTA screen first and then reconnect, follow the pattern used by the demo's `OtaActivity`:

1. Disconnect the normal connection when the screen initializes.
2. Call `connect()` again when the user starts the update.
3. Call `startOtaUpdate()` from `blePeripheralConnected()` / `onConnectedSuccess()`.

#### 5.7.3 Update Progress

```kotlin
override fun otaProgressUpdate(percentage: Int, status: Int) {
    when (status) {
        ConstantUtil.OTA_STATUS_BEGIN -> {
            // 110: started
        }
        ConstantUtil.OTA_STATUS_PROCESSING -> {
            // 121: in progress; percentage is the progress
        }
        ConstantUtil.OTA_STATUS_FINISH -> {
            // 10086: finished
        }
    }
}
```

The values `100` (`PACKAGE_SEND_FINISH`) and `101` (`FIFO_SEND_FINISH`) reported by `sendPacketProgress()` indicate packet/FIFO transmission completion, not the final OTA state.

The `reset` parameter controls whether the device resets or restores factory settings after the update. The exact behavior depends on the firmware; pass `true` or `false` according to your requirements.

#### 5.7.4 OTA Prerequisites

- The app must declare `com.feasycom.service.OTAService`.
- The device must be connected.
- The firmware target module must match the current device module.
- The firmware version range must match.
- Do not truncate the `.dfu` file or replace it with another format.

### 5.8 SUOTA

Some modules use SUOTA instead of standard DFU. Check first:

```kotlin
val module = wrapper.feasyBeacon?.module?.toIntOrNull() ?: return
val useSuota = OtaUtils.needsSuotaUpgrade(module)
```

Module numbers currently routed to SUOTA by `OtaUtils`:

```text
39, 41, 46, 54, 56, 68, 69, 71, 72, 73, 74, 75,
79, 98, 104, 108, 116, 117, 120, 121
```

High-level flow using `SuotaManager`:

```kotlin
val manager = SuotaManager(
    lifecycle,
    context,
    bluetoothDevice,
    object : SuotaManagerCallback() {
        override fun onConnectionStateChange(newStatus: SuotaProfile.SuotaManagerStatus) {}
        override fun onDeviceReady() {}
        override fun onUploadProgress(percent: Float) {}
        override fun onSuccess(totalElapsedSeconds: Double, imageUploadElapsedSeconds: Double) {}
        override fun onFailure(errorCode: Int) {}
    }
)

manager.connect()
```

After the device is connected and ready:

```kotlin
manager.setSuotaFile(SuotaFile(context, firmwareUri))
manager.initializeSuota(240, 3, 0, 1, 4, 2)
manager.startUpdate()
```

The `initializeSuota(240, 3, 0, 1, 4, 2)` call above is the parameter combination used by the demo. The arguments correspond to block size, MISO, MOSI, CS, SCK, and image bank. Different hardware or firmware may require different settings; do not apply them blindly.

SUOTA also requires attention to:

- File-selection permissions and storage paths.
- `SuotaManager.checkPermissions()` and `checkStorage()`.
- The error code from `onFailure()`.
- The reboot flow after a successful update.
- The AAR includes `RunAutoSuotaActivity` and SUOTA resources, but the demo's `SuotaActivity` is an app-layer wrapper example.

For integration, read the following demo files:

- `ui/suota/activities/SuotaActivity.kt`
- `ui/suota/fragments/AvailableFirmwareFragment.kt`
- `ui/suota/fragments/UpdateFirmwareFragment.kt`
- `ui/suota/bluetooth/SuotaCallback.kt`

### 5.9 Phone-as-a-Beacon Advertising

This functionality is not part of `FscBeaconCentralApi`. The demo uses Android's native `BluetoothLeAdvertiser`:

- `IBeaconAdvertiser`
- `AltBeaconAdvertiser`
- `EddystoneUIDAdvertiser`
- `EddystoneURLAdvertiser`
- `EddystoneTLMAdvertiser`
- `AoABeaconAdvertiser`

The entry point is `ui/beacon/CustomBroadcastActivity.kt`. If you only need to scan and configure devices, you do not need this code. To use it, declare `BLUETOOTH_ADVERTISE` in the manifest and request it at runtime.

---

## 6. API Reference

### 6.1 `FscBeaconCentralApi`

The implementation class is `FscBeaconCentralApiImp`, a singleton.

```java
static FscBeaconCentralApiImp getInstance(Context context)
static FscBeaconCentralApiImp getInstance()
```

#### 6.1.1 Scanning and Connection

| Method | Return type | Description |
|---|---|---|
| `initialize()` | `boolean` | Initializes the BLE environment |
| `isShowLog(boolean)` | `void` | Enables or disables SDK logging |
| `isBluetoothEnabled()` | `boolean` | Whether Bluetooth is enabled |
| `startScan()` | `boolean` | Starts scanning with an internal 90-second timeout |
| `startScan(int duration)` | `boolean` | Starts scanning; the current implementation ignores `duration` |
| `stopScan()` | `void` | Stops scanning |
| `connect(BluetoothDeviceWrapper, String pin)` | `boolean` | Connects to a device and verifies the PIN |
| `disconnect(String loca)` | `void` | Disconnects; `loca` is used only for logging |
| `isConnected()` | `boolean` | Whether a device is connected |
| `setCallbacks(FscBeaconCentralCallbacks)` | `void` | Registers callbacks |

#### 6.1.2 Device Information

| Method | Description |
|---|---|
| `getDeviceInfo(FeasyBeacon feasyBeacon, boolean queryTxPower)` | Queries device information; results are delivered through `onDeviceInfoReceived` |
| `setDeviceName(String)` | Sets the device name and adds the command to the pending queue |
| `setDevicePin(String)` | Sets the PIN and adds the command to the pending queue |
| `setBroadcastInterval(String)` | Sets the advertising interval and adds the command to the pending queue |
| `setDeviceTxPower(String)` | Sets the module TX Power and adds the command to the pending queue |
| `setGSensorConfig(String interval, String duration)` | Configures the G-sensor and adds the command to the pending queue |
| `setGSensorConfig(String interval, String duration, String threshold)` | Configures the G-sensor with a threshold |
| `setKeyConfig(String interval, String duration)` | Configures key-triggered advertising |
| `setLed(String)` | Sets the LED indication duration |

#### 6.1.3 Advertising Slots

| Method | Description |
|---|---|
| `isBeaconInfoFull()` | Whether the normal advertising slots are full |
| `isConditionBeaconInfoFUll()` | Whether the conditional advertising slots are full |
| `addBeaconInfo(BeaconBean)` | Adds a normal advertising slot |
| `addConditionBeaconInfo(BeaconBean)` | Adds a conditional advertising slot |
| `updateBeaconInfo(BeaconBean, String index)` | Updates a slot by one-based index |
| `deleteBeaconInfo(String index)` | Deletes a slot by one-based index |
| `deleteConditionBeaconInfo(String index)` | Deletes a conditional slot |
| `saveBeaconInfo(ArrayList<BeaconBean>, ArrayList<BeaconBean>)` | Saves normal and conditional slots and sends pending parameters |
| `beaconBroadcastParameters()` | Queries or synchronizes advertising parameters |
| `setBroadcastTxPower(String index, int p)` | Sets TX Power for a slot; index starts at 0 |
| `setBroadcastTxPower(String index, String p)` | String overload of the same operation |
| `setBroadcastPHY(String index, int p)` | Sets PHY for a slot; index starts at 0 |

#### 6.1.4 Other Capabilities

| Method | Description |
|---|---|
| `setTlm(boolean)` | Sets the TLM switch and adds the command to the pending queue |
| `setAoa(boolean)` | Sets the AOA switch and adds the command to the pending queue |
| `setBroadcastExtensions(boolean)` | Sets extended advertising and adds the command to the pending queue |
| `setFirmwareKey(String command)` | Immediately sends firmware-key-related AT commands |
| `setAppKey(String key)` | Writes the key to the app's local SharedPreferences; does not write it to the device |
| `restore()` | Immediately sends the factory-reset command |
| `saveBatch(CommandBean)` | Immediately sends a batch of AT commands |
| `ota()` | Assembles an `AT+OTA=...` command |
| `checkDfuFile(byte[])` | Parses a DFU file; may return `null` |
| `startOtaUpdate(byte[] dfuFile, boolean reset)` | Starts a DFU update; requires an active connection |

### 6.2 `FscBleCentralApi`

`FscBleCentralApi` is the lower-level BLE/AT interface and has fewer methods:

| Method | Description |
|---|---|
| `startScan()` | Starts scanning |
| `startScan(int duration)` | Starts scanning |
| `stopScan()` | Stops scanning |
| `disconnect(String loca)` | Disconnects |
| `isConnected()` | Whether a device is connected |
| `send(byte[] packet)` | Sends raw bytes |
| `stopSend()` | Stops sending |
| `sendATCommand(Set<String> commands)` | Sends a set of AT commands |

`FscBleCentralApiImp` also provides several advanced methods:

| Method or field | Description |
|---|---|
| `setSDKDataCallbacks(...)` | Replaces only the data callbacks |
| `setOTACallbacks(...)` | Replaces only the OTA callbacks |
| `getCallbacks()` | Returns the current callbacks |
| `getDevice()` | Returns the current `BluetoothDevice` |
| `getWriteCharacteristic()` | Returns the current write characteristic |
| `startOTA(byte[], boolean)` | Starts OTA |
| `checkDfuFile(byte[])` | Parses a DFU file |
| `EN_AUTO_QUERY` | Static switch; default `false` |
| `EN_AUTO_VERIFY` | Static switch; default `false` |

### 6.3 Callback Interfaces

Inheritance hierarchy:

```text
FscCentralCallbacks
   └── FscBleCentralCallbacks
          └── FscBeaconCentralCallbacks
```

#### `FscCentralCallbacks`

| Method | Trigger |
|---|---|
| `startScan()` | After `startScan()` is called |
| `stopScan()` | After `stopScan()` is called |
| `connectProgressUpdate(BluetoothDevice, int)` | Connection or PIN verification progress |

#### `FscBleCentralCallbacks`

| Method | Trigger |
|---|---|
| `blePeripheralFound(BluetoothDevice, BluetoothDeviceWrapper, int, byte[])` | A device is found during scanning |
| `blePeripheralConnected(BluetoothGatt, BluetoothDevice)` | Connection established |
| `blePeripheralDisconnected(BluetoothGatt, BluetoothDevice)` | Connection lost |
| `atCommandCallBack(String command, String param, String status)` | AT command result |
| `servicesFound(...)` | GATT services discovered |
| `characteristicForService(...)` | GATT characteristic discovered |
| `packetReceived(...)` | Data packet received |
| `readResponse(...)` | Read response received |
| `sendPacketProgress(...)` | Send progress |
| `otaProgressUpdate(int percentage, int status)` | OTA progress and status |

#### `FscBeaconCentralCallbacks`

| Method | Trigger |
|---|---|
| `onDeviceInfoReceived(String parameterName, Object parameter)` | Individual device-information result from `getDeviceInfo()` |

### 6.4 Constants

#### Beacon Types

```java
FeasyBeacon.BEACON_TYPE_I_BEACON          // "iBeacon"
FeasyBeacon.BEACON_TYPE_EDDYSTONE_URL     // "URL"
FeasyBeacon.BEACON_TYPE_EDDYSTONE_UID     // "UID"
FeasyBeacon.BEACON_TYPE_EDDYSTONE_TLM     // "TLM"
FeasyBeacon.BEACON_TYPE_ALTBEACON         // "AltBeacon"
FeasyBeacon.BEACON_TYPE_AOABEACON         // "AoaBeacon"
FeasyBeacon.BEACON_TYPE_CUSTOM            // "CustomBeacon"
```

#### Slot Count

```java
FeasyBeacon.BEACON_COUNT      // 10
ConstantUtil.MAX_BEACONS      // 10
```

#### AT Command Status

```java
CommandBean.COMMAND_FAILED       // "0"
CommandBean.COMMAND_SUCCESSFUL   // "1"
CommandBean.COMMAND_FINISH       // "2"
CommandBean.COMMAND_NO_NEED      // "3"
CommandBean.COMMAND_TIME_OUT     // "4"
```

#### PIN Verification Status

```java
CommandBean.PASSWORD_CHECK        // 0
CommandBean.PASSWORD_SUCCESSFUL   // 1
CommandBean.PASSWORD_FAILED       // 2
CommandBean.PASSWORD_TIME_OUT     // 3
```

#### OTA and Transmission Progress

```java
ConstantUtil.OTA_STATUS_BEGIN        // 110
ConstantUtil.OTA_STATUS_PROCESSING   // 121
ConstantUtil.OTA_STATUS_FINISH       // 10086
ConstantUtil.PACKAGE_SEND_FINISH     // 100
ConstantUtil.FIFO_SEND_FINISH        // 101
```

### 6.5 Data Models

#### `BluetoothDeviceWrapper`

| Field | Meaning |
|---|---|
| `address` | Bluetooth address |
| `name` | Advertised name |
| `rssi` | Signal strength |
| `scanRecord` | Raw advertising data |
| `completeLocalName` | Complete local name |
| `iBeacon` | iBeacon data |
| `eddystoneBeacon` | Eddystone data |
| `altBeacon` | AltBeacon data |
| `aoaBeacon` | AOA data |
| `feasyBeacon` | Configurable module information |
| `thSensor` | Temperature/humidity sensor data |
| `gSensor` | G-sensor data |

#### `FeasyBeacon`

See Section 5.3 for the main fields. The `module` field is a string; convert it to `Int` before making OTA/SUOTA decisions.

#### `BeaconBean`

Common fields:

| Field | Meaning |
|---|---|
| `index` | Slot index, starting at 1 |
| `beaconType` | Advertising type |
| `uuid` / `major` / `minor` | iBeacon parameters |
| `url` | Eddystone URL |
| `nameSpace` / `instance` / `reserved` | Eddystone UID parameters |
| `id1` / `id2` / `id3` | AltBeacon parameters |
| `manufacturerId` / `manufacturerReserved` | AltBeacon manufacturer fields |
| `rssi` | Advertising TX Power or signal field |
| `txpower` | Slot transmit power |
| `phy` | Physical layer |
| `interval` | Advertising interval |
| `enable` | Whether the slot is enabled |
| `connectable` | Whether the slot is connectable |
| `customBeaconData` | Custom advertising data |
| `customConditionData` | Custom conditional advertising data |
| `sensorType` / `actionType` / `intervals` / `timeout` | Conditional advertising parameters |

#### `DfuFileInfo`

| Field | Meaning |
|---|---|
| `bootloader` | Bootloader version |
| `versonStart` | Firmware start version (the field name is `verson`, not `version`) |
| `verson_soft_end` | Firmware end version |
| `type_model` | Target module number |

#### `CommandBean`

| Method | Description |
|---|---|
| `setCommand(String)` | Adds an AT command and deduplicates by command prefix |
| `getCommand()` | Returns the pending command set |

---

## 7. Demo Guide

The demo is the complete app source. Start with the following files:

| File | Purpose |
|---|---|
| `logic/BluetoothRepository.kt` | SDK singleton wrapper, callback dispatch, and entry points for scanning, connection, parameters, and OTA |
| `logic/interfaces/FscBleCallback.kt` | App-layer callback interface |
| `ui/beacon/BeaconFragment.kt` | Beacon scan list |
| `ui/sensor/SensorFragment.kt` | Sensor list |
| `ui/setting/SettingFragment.kt` | Configurable-device list, PIN input, and QR code entry |
| `ui/parameter/ParameterSettingActivity.kt` | Parameter reading, UI population, saving, advertising slots, and OTA/batch entry points |
| `ui/ota/OtaActivity.kt` | DFU file selection, download, validation, and update |
| `ui/suota/activities/SuotaActivity.kt` | SUOTA update flow |
| `ui/batch/config/BatchConfigActivity.kt` | Batch parameter selection |
| `ui/batch/scan/BatchScanActivity.kt` | Batch scanning and multi-select |
| `ui/batch/modify/BatchModifyActivity.kt` | Per-device writes and result reports |
| `ui/beacon/CustomBroadcastActivity.kt` | Phone-as-a-beacon advertising |
| `assets/Device.json` | Module models and parameter ranges |

Note: `Device.json` is app data in the demo and is not part of the AAR. The SDK itself does not perform complete parameter-range validation. Default values, available TX Power levels, advertising interval ranges, and similar constraints are usually determined by the app from the model table or its own business rules.

---

## 8. Troubleshooting

### 8.1 No Scan Callbacks

Check the following in order:

1. Whether you called `getInstance(context)`, `initialize()`, and `setCallbacks()`.
2. Whether you granted `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT`.
3. On Android 11 and below, whether you granted location permission and enabled the system location switch.
4. Whether Bluetooth is enabled.
5. Whether `stopScan()` was called too early after starting the scan.
6. Whether exceptions in `blePeripheralFound()` are being swallowed.

### 8.2 `connect()` Returns `false`

Check the following first:

- Whether you used the `getInstance()` overload that accepts a `Context`.
- Whether `wrapper.getFeasyBeacon()` is `null`.
- Whether the PIN is numeric and between 1 and 6 digits.

### 8.3 Device Does Not Change After Setting Parameters

`setDeviceName()`, `setDevicePin()`, `setBroadcastInterval()`, and similar methods only enqueue commands. You must call `saveBeaconInfo()` or `saveBatch()` to send them.

### 8.4 Advertising Slots Are Cleared After Saving Parameters

This usually happens because `getDeviceInfo()` was not called before saving, or the slot lists passed to `saveBeaconInfo()` were incomplete. Wait for the `BADVDATA` and `SADV` callbacks, then save with the current slots.

### 8.5 OTA Never Starts

Check:

- Whether `com.feasycom.service.OTAService` is declared.
- Whether the device was connected when `startOtaUpdate()` was called.
- Whether `disconnect()` was called incorrectly before the update.
- Whether the firmware target module matches the current device.
- Whether the firmware version range matches.
- Whether the `.dfu` file is complete.

### 8.6 SUOTA Fails

Check:

- Whether the module is in the list returned by `OtaUtils.needsSuotaUpgrade()`.
- Whether the firmware header matches the SUOTA type.
- Whether storage and file-read permissions are sufficient.
- Whether the GPIO, block size, and image bank passed to `initializeSuota()` match the hardware.
- The error code from `onFailure()`.

### 8.7 `UnsatisfiedLinkError`

Check:

- Whether the app contains `libFeasyBeaconLibrary.so`.
- Whether the ABI is `arm64-v8a` or `armeabi-v7a`.
- Whether the app is running on an x86 emulator.
- Whether the `.so` in the AAR was replaced with a different version.

### 8.8 Release Build Is Unsigned

This repository has removed `signingConfigs`; `assembleRelease` produces an unsigned APK by default. To produce a signed build, configure your own keystore locally. Do not commit the keystore or passwords to a public repository.

### 8.9 Why Do the Version Numbers Differ?

- Release package: `3.3.9.9`
- This repository's app: `3.3.9.9`
- AAR internal library module: `3.3.8.7`

This is the current packaging rule: the external package is named after the app's `versionName`, while the library module's own version has not been synchronized.

### 8.10 Why Does `startScan(10000)` Not Stop After 10 Seconds?

The current `FscBleCentralApiImp.startScan(int duration)` does not use `duration` and always uses a fixed 90-second timeout. Call `stopScan()` yourself for precise control.

---

## 9. Integration Checklist

- [ ] `FeasyBeaconLibrary-release.aar` has been added
- [ ] `minSdk >= 26`
- [ ] `abiFilters` contains only `arm64-v8a` and `armeabi-v7a`
- [ ] `AtCommandService` and `OTAService` are declared
- [ ] `BLUETOOTH_SCAN` and `BLUETOOTH_CONNECT` are requested
- [ ] Location permission and the location switch are handled on Android 11 and below
- [ ] `BLUETOOTH_ADVERTISE` is requested when using phone-as-a-beacon advertising
- [ ] `getInstance(context)`, `initialize()`, and `setCallbacks()` are called
- [ ] `wrapper.feasyBeacon != null` is checked before connecting
- [ ] `getDeviceInfo()` is called before modifying parameters, and complete slots are passed when saving
- [ ] The device is connected and `OTAService` is declared before OTA
- [ ] SDK keep rules are added when minification is enabled
- [ ] SUOTA is configured for the module model and hardware parameters

---

## 10. Document Notes

This document was statically verified against the current release package and source code, with emphasis on:

- The AAR package name, `minSdk`, JNI library, manifest, and resources.
- The public methods of `FscBeaconCentralApi` and `FscBleCentralApi`.
- The three-level callback interfaces.
- The DFU/SUOTA entry points and actual demo usage.
- The demo's Gradle, manifest, and signing configuration.

This document was not validated by running a Gradle build or real-device BLE/OTA/SUOTA tests on the authoring machine. If actual integration behavior differs from this document, prioritize the current AAR class signatures, device firmware version, and actual demo runtime behavior.

For delivery, place this English guide at `docs/FeasyBeacon_Integration_Guide_EN.md` and the Chinese guide in the same `docs/` directory. The current `sdk-package/pack-sdk.ps1` only assembles `apk/`, `sdk/`, and `demo/`; it does not automatically include `docs/` in the ZIP. If you want the documentation included automatically, update the packaging script.
