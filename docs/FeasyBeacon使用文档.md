# FeasyBeacon Android SDK 使用文档（AAR + Demo 版）

> **交付包**：`feasybeacon-sdk-android-3.3.9.9.zip`
> **SDK AAR**：`FeasyBeaconLibrary-release.aar`
> **SDK Java 包名**：`com.feasycom`
> **Demo applicationId**：`com.feasycom.feasybeacon`
> **AAR 最低系统版本**：Android 8.0（API 26）
> **Demo 最低系统版本**：Android 10（API 29）
> **支持 ABI**：`arm64-v8a`、`armeabi-v7a`
> **文档核对日期**：2026-10-20

本文按当前交付的 `3.3.9.9` 包和 `demo` 源码核对。SDK 包版本按 App 的 `versionName` 命名；AAR 内部库模块版本是 `3.3.8.7`，两者不一致是当前打包规则，不是文件放错。

> English version: [FeasyBeacon_Integration_Guide_EN.md](FeasyBeacon_Integration_Guide_EN.md)

## 目录

1. 交付内容
2. 环境要求
3. 集成步骤
4. 初始化
5. 典型流程
6. API 参考
7. Demo 导读
8. 常见问题
9. 接入检查清单
10. 文档说明

---

## 1. 交付内容

### 1.1 压缩包目录

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

> 本仓库是公开应用层仓库，目录对应关系是：`feasybeacon/` 对应发布包里的 `demo/feasybeacon/`；`apk/` 放发布 APK；`feasybeacon/libs/` 放 AAR。发布包里的 `sdk/` 不需要再放进本仓库。

三部分的作用：

| 目录 | 用途 |
|---|---|
| `apk/` | 已编译的参考 App，可用来对照扫描、连接、参数配置和升级现象 |
| `sdk/` | 接入方真正需要引入的 AAR 和原生库 |
| `demo/` | 完整 App 源码，已改成通过本地 AAR 使用 SDK，可直接用 Android Studio 打开 |

注意：`demo/` 不是“最小接入示例”，而是完整 FeasyBeacon App 的源码。它包含网络、Room、广告、论坛、反馈等业务代码。接入 SDK 时不要把 demo 的所有依赖和 Manifest 原样搬过去。

### 1.2 AAR 内部结构

当前交付的 AAR 实测信息：

| 条目 | 大小 | 说明 |
|---|---:|---|
| `classes.jar` | 306,463 B | 185 个 `.class` 条目（含内部类） |
| `jni/arm64-v8a/libFeasyBeaconLibrary.so` | 31,792 B | 64 位原生库 |
| `jni/armeabi-v7a/libFeasyBeaconLibrary.so` | 21,876 B | 32 位原生库 |
| `AndroidManifest.xml` | 1,558 B | 包名 `com.feasycom`，`minSdkVersion 26` |
| `proguard.txt` | 0 B | **空文件，接入方需要自行添加 keep 规则** |
| `res/` | 多个文件 | 包含 SUOTA 相关布局、颜色、字符串和图片资源 |
| `R.txt` | 275,368 B | 资源 ID 清单 |

当前 AAR 校验值：

| 算法 | 值 |
|---|---|
| SHA-256 | `D48CE9F710C66EDF3016BAE1A897F09F3A25701AEAFF0FDC2C7D41DEDE5F7F1B` |
| MD5 | `E5730E90D01B371CA1BEC5086C915159` |

每次重新打包，哈希都会变化；请以实际交付文件为准。

### 1.3 AAR 里有什么

**AAR 提供：**

- BLE 扫描、连接、断开、GATT 读写
- PIN 鉴权和加密校验
- AT 指令队列与参数读写
- iBeacon、Eddystone、AltBeacon、AOA、传感器广播数据解析
- Beacon 广播槽位和条件广播槽位管理
- DFU 升级服务和分包传输
- SUOTA 升级协议、基础 Activity 和相关资源
- 常用数据类：`BluetoothDeviceWrapper`、`FeasyBeacon`、`BeaconBean`、`CommandBean`、`DfuFileInfo` 等


---

## 2. 环境要求

| 项 | 要求 |
|---|---|
| `minSdk` | AAR 为 26；demo 为 29 |
| `compileSdk` | demo 使用 35；AAR 元数据 `minCompileSdk=1` |
| `targetSdk` | demo 使用 35 |
| JDK | 17 |
| Gradle | demo 使用 8.13 |
| Android Gradle Plugin | demo 使用 8.13.0 |
| Kotlin | demo 使用 1.9.20；AAR 内有 Kotlin 编译的 `OTAService` 和异常类，需要 `kotlin-stdlib` |
| ABI | 仅 `arm64-v8a`、`armeabi-v7a`；x86/x86_64 模拟器无法运行原生库 |
| 运行环境 | 设备支持 BLE，蓝牙已开启，定位服务和定位权限符合系统要求 |


### 2.1 依赖说明

AAR 不携带 Maven POM，因此不会自动带入第三方依赖。核心扫描/配置至少需要：

- `androidx.annotation`
- `org.jetbrains.kotlin:kotlin-stdlib`
- `androidx.appcompat`（AAR 的 SUOTA 资源引用了 AppCompat 样式）

如果使用 SUOTA，还建议加入：

- `androidx.lifecycle`
- `androidx.fragment`
- `androidx.documentfile`

最稳妥的方式是参考 `demo/feasybeacon/build.gradle` 的依赖组合，再按实际功能裁剪。

---

## 3. 集成步骤

### 3.1 引入 AAR 和 SO

**方式 A：只引入 AAR（推荐）**

当前 AAR 的 `jni/` 目录已经包含两个 ABI 的 `libFeasyBeaconLibrary.so`。如果只使用 AAR，通常不需要再单独复制 `.so`。

把 AAR 放到 App 模块的 `libs/`：

```text
app/libs/FeasyBeaconLibrary-release.aar
```

**方式 B：AAR + 外部 SO（兼容旧接入方式）**

如果工程已有统一管理 `.so` 的流程，也可以同时复制：

```text
app/libs/FeasyBeaconLibrary-release.aar
app/libs/arm64-v8a/libFeasyBeaconLibrary.so
app/libs/armeabi-v7a/libFeasyBeaconLibrary.so
```

并在 `build.gradle` 中声明：

```gradle
android {
    sourceSets {
        main {
            jniLibs.srcDirs = ['libs']
        }
    }
}
```

不要重命名 `.so`，也不要替换成其他版本的库。JNI 符号依赖固定库名 `FeasyBeaconLibrary`。

### 3.2 Gradle 配置

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

    // 使用 SUOTA 时建议加入
    implementation "androidx.lifecycle:lifecycle-runtime-ktx:2.6.2"
    implementation "androidx.fragment:fragment-ktx:1.6.2"
    implementation "androidx.documentfile:documentfile:1.0.1"
}
```

上面的依赖版本是示例，实际接入时应与现有工程统一。demo 使用的是 `appcompat 1.3.0`、`lifecycle 2.6.2`，但没有单独声明 `documentfile`；如果你的工程编译时提示找不到 `DocumentFile`，再显式加入 `androidx.documentfile:documentfile`。

### 3.3 AndroidManifest 配置

#### 3.3.1 AAR 自动合并的权限

AAR 自带以下权限，通常会通过 Manifest 合并进入 App：

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

如果不需要某些权限，可以在 App 的 Manifest 中用 `tools:node="remove"` 移除。例如：

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

`BLUETOOTH_PRIVILEGED` 和 `SYSTEM_ALERT_WINDOW` 属于敏感/受保护权限，普通应用通常拿不到。基础扫描和参数配置不需要它们。

demo 的 Manifest 还声明了 `WRITE_SECURE_SETTINGS`、`CAMERA` 等权限。`WRITE_SECURE_SETTINGS` 是受保护权限，基础扫描和配置不需要它；如果只是接入 SDK，不要盲目复制 demo 的全部权限声明。

#### 3.3.2 必须补充的两个 Service

这是最常见的接入问题。AAR 的 Manifest 没有声明 `AtCommandService` 和 `OTAService`，需要 App 自己声明：

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

参数读写依赖 `AtCommandService`，DFU 升级依赖 `OTAService`。SDK 内部会自行 `bindService`，接入方不需要手动启动。

#### 3.3.3 按需补充的权限

```xml
<!-- 手机自身作为 Beacon 外设广播时需要 -->
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />

<!-- 二维码扫描需要 -->
<uses-permission android:name="android.permission.CAMERA" />

<!-- 网络请求、固件下载、反馈等业务需要 -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Android 12（API 31）及以上，如果不需要从扫描结果推导位置，建议给 `BLUETOOTH_SCAN` 增加 `neverForLocation`：

```xml
<uses-permission
    android:name="android.permission.BLUETOOTH_SCAN"
    android:usesPermissionFlags="neverForLocation"
    tools:node="replace" />
```

#### 3.3.4 运行时权限

SDK 不会替 App 申请核心扫描/连接权限。通常需要：

- Android 12 及以上：`BLUETOOTH_SCAN`、`BLUETOOTH_CONNECT`
- Android 11 及以下：定位权限，并且系统定位开关要打开
- 手机端广播：`BLUETOOTH_ADVERTISE`

**未授权时扫描通常没有任何回调**，很容易被误判为 SDK 故障。demo 使用 XXPermissions 申请权限，接入方也可以使用 `ActivityResultContracts.RequestMultiplePermissions`。

### 3.4 混淆规则

AAR 的 `proguard.txt` 是空文件。如果 App 开启 `minifyEnabled true`，需要自行加入：

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

注意 `controler` 是库里的固定拼写，不是 `controller`。

本仓库的 `proguard-rules.pro` 只是模板，不能替代上面的 SDK keep 规则；AAR 自带的 `proguard.txt` 是空文件。

### 3.5 运行本仓库

1. 用 Android Studio 打开仓库根目录。
2. 使用 JDK 17，Gradle 8.13，AGP 8.13.0。
3. 等待 Gradle Sync 完成。
4. 连接支持 BLE 的真机运行。

本仓库已经移除 `signingConfigs`：debug 使用 Android 默认调试签名，release 默认生成未签名 APK。需要签名包时，请在自己的机器上配置 keystore，且不要提交 keystore 和密码。

仓库没有带 `local.properties`，Android Studio 会自动生成；如果 SDK 路径不对，按 IDE 提示重新设置 Android SDK。

---

## 4. 初始化

### 4.1 三步初始化

```kotlin
val api = FscBeaconCentralApiImp.getInstance(context.applicationContext).apply {
    isShowLog(BuildConfig.DEBUG)
    initialize()
    setCallbacks(callbacks)
}
```

三个动作缺一不可：

1. `getInstance(context)`：第一次必须传 `Context`，建议使用 `applicationContext`。
2. `initialize()`：初始化 BLE 扫描器和 GATT 环境。
3. `setCallbacks(callbacks)`：注册扫描、连接、AT 指令、OTA 等回调。

`getInstance()` 不带参数的版本只返回已有单例，不会补 `Context`。如果从未调用过带 `Context` 的版本，后续 `connect()` 会返回 `false`，`isBluetoothEnabled()` 也可能出现空指针。

建议把 SDK 包在一个全局单例里，例如 demo 的 `BluetoothRepository`，再由它把回调分发给页面。不要让每个页面各自持有 `FscBeaconCentralApiImp`。

### 4.2 完整回调空实现模板

`FscBeaconCentralCallbacks` 继承自 `FscBleCentralCallbacks`，方法较多。下面是一个可直接复制后按需改写的 Kotlin 模板：

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

### 4.3 回调线程

扫描回调、GATT 回调和 AT 回调不保证在主线程。典型做法是：

- 在 `blePeripheralFound()` 里只做数据解析和列表更新，必要时切主线程。
- 在 `onDeviceInfoReceived()` 里不要做耗时操作。
- 在 `onAtCommandCallBack()` 里只更新状态或 UI。

### 4.4 推荐调用顺序

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

如果只做扫描展示，不需要连接；如果要改参数或升级，必须等到连接成功并完成设备信息查询。

---

## 5. 典型流程

### 5.1 扫描

```kotlin
if (!api.isBluetoothEnabled) {
    // 提示用户打开蓝牙
    return
}

api.startScan()
```

扫描结果从 `blePeripheralFound()` 返回：

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
            // 可配置的 FeasyBeacon 模块
        }
        d.iBeacon != null -> {
            // iBeacon 广播
        }
        d.eddystoneBeacon != null -> {
            // Eddystone UID / URL / TLM
        }
        d.altBeacon != null -> {
            // AltBeacon 广播
        }
        d.aoaBeacon != null -> {
            // AOA 广播
        }
    }

    d.thSensor?.let {
        // 温湿度传感器数据
    }
    d.gSensor?.let {
        // 重力传感器数据
    }
}
```

扫描实现有几个需要知道的细节：

- `startScan(int duration)` 当前没有使用 `duration`，内部固定 90 秒后调用 `stopScan()`。需要精确控制时，自己调用 `stopScan()`。
- `startScan()` 返回 `true` 只表示调用流程走完，不代表一定扫到了设备；以回调和列表结果为准。
- `iBeacon`、`eddystoneBeacon`、`altBeacon`、`aoaBeacon` 四个字段互斥，设置其中一个会清空其他三个。
- `thSensor`、`gSensor`、`feasyBeacon` 是附加信息，可以和广播类型同时存在。

### 5.2 连接与 PIN

```kotlin
val ok = api.connect(wrapper, "000000")
```

`connect()` 返回 `false` 的常见原因：

- 没有先调用 `getInstance(context)`，SDK 内部 `context` 为 `null`。
- `wrapper.getFeasyBeacon()` 为 `null`，说明这不是可配置模块。
- PIN 不是纯数字。
- PIN 为空或长度超过 6 位。

连接进度通过 `connectProgressUpdate(device, status)` 返回：

| status | 含义 |
|---:|---|
| `0` | 密码校验中 |
| `1` | 密码正确 |
| `2` | 密码错误 |
| `3` | 超时 |

断开：

```kotlin
api.disconnect("业务页面退出")
```

`disconnect(String loca)` 的参数只是日志标记，不是设备地址，传任意描述字符串即可。断开后 SDK 会清空内部的设备名、模块号、版本、加密方式和广播槽位状态，重新连接后应重新调用 `getDeviceInfo()`。

SDK 不提供自动重连，业务层需要自己处理重连和重试。

### 5.3 读取设备信息

```kotlin
api.getDeviceInfo(wrapper.feasyBeacon!!, true)
```

第二个参数 `true` 表示同时查询 TX Power。结果不是一次性返回一个对象，而是通过 `onDeviceInfoReceived(parameterName, parameter)` 按参数逐条回调。

常见参数名和值类型：

| parameterName | 典型类型 | 含义 |
|---|---|---|
| `Opened` | 无或字符串 | 连接/查询开始 |
| `VERSION` | `String` | 固件版本 |
| `NAME` | `String` | 设备名 |
| `PIN` | `String` | PIN |
| `ADVIN` | `String` | 广播间隔 |
| `TXPOWER` | `String` | TX Power 档位/编码 |
| `KEYCFG` | `String[]` | 按键间隔、持续时间 |
| `GSCFG` | `String[]` | G-Sensor 间隔、持续时间，部分模块还带阈值 |
| `BADVDATA` | `ArrayList<BeaconBean>` | 普通广播槽位 |
| `SADV` | `ArrayList<BeaconBean>` | 条件广播槽位 |
| `BADVPARM` | `String` | 广播参数，逗号分隔 |
| `TLM` | `String` | `"0"` / `"1"` |
| `AOA` | `String` | `"0"` / `"1"` |
| `ADVEXT` | `String` | 扩展广播开关 |
| `EXTEND` / `BWMODE` / `LED` / `BUZ` | `String` | 扩展信息或外设状态 |

`parameter` 是 `Object`，使用前要按参数名做类型判断。不要在回调里做耗时操作，否则会影响后续 AT 查询。

`FeasyBeacon` 中常用的字段：

| 字段 | 含义 |
|---|---|
| `module` | 模块型号编号，字符串形式 |
| `version` | 固件版本 |
| `deviceName` | 设备名 |
| `deviceAddress` | 设备地址 |
| `battery` | 电量 |
| `connectable` | 是否可连接 |
| `keyConfig` | 是否支持按键配置 |
| `gSensor` | 是否支持 G-Sensor |
| `buzzer` | 是否支持蜂鸣器 |
| `led` | 是否支持 LED |
| `nfc` | 是否支持 NFC |
| `longRange` | 是否支持长距离 |
| `encryptionWay` | 加密方式 |

### 5.4 修改参数

常用 setter：

```kotlin
api.setDeviceName("FeasyBeacon-001")
api.setDevicePin("123456")
api.setBroadcastInterval("200")
api.setDeviceTxPower("7")
api.setGSensorConfig("100", "5")
api.setKeyConfig("100", "5")
api.setLed("10")
```

这些 setter 只是把 AT 指令组装进 `CommandBean`，**不会立即下发**。最终需要调用：

```kotlin
api.saveBeaconInfo(currentBeaconList, currentConditionList)
```

或者对批量命令使用：

```kotlin
api.saveBatch(commandBean)
```

必须等 `onAtCommandCallBack()` 收到：

| status | 含义 |
|---|---|
| `"0"` | 失败 |
| `"1"` | 成功 |
| `"2"` | 全部完成 |
| `"3"` | 无需修改 |
| `"4"` | 超时 |

**重要：`saveBeaconInfo()` 不只会保存名称、PIN 等参数，还会把 10 个普通广播槽位和 10 个条件广播槽位一起写入。**

因此正确流程是：

1. 连接成功后先调用 `getDeviceInfo()`。
2. 等 `BADVDATA`、`SADV` 回调拿到当前槽位数据。
3. 修改名称/PIN/间隔等参数。
4. 把当前槽位列表一起传给 `saveBeaconInfo()`。

如果跳过第 2 步，直接传空列表或未初始化的列表，可能把设备已有的广播槽位清空。

其他行为：

- `setFirmwareKey()` 会立即发送 AT 指令。
- `restore()` 会立即发送恢复出厂指令。
- `beaconBroadcastParameters()` 会立即查询广播参数。
- `setAppKey()` 只把 key 写入 App 本地 SharedPreferences，不是写设备。
- `setTlm()`、`setAoa()`、`setBroadcastExtensions()` 会组装命令，仍需要调用 `saveBeaconInfo()` 统一下发。
- `setBroadcastTxPower()`、`setBroadcastPHY()` 只修改内存中的 `BeaconBean`，也需要保存。

### 5.5 广播槽位管理

SDK 连接成功后会初始化 10 个普通槽位和 10 个条件槽位：

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

常用方法：

| 方法 | 说明 |
|---|---|
| `isBeaconInfoFull()` | 普通槽位是否已满 |
| `isConditionBeaconInfoFUll()` | 条件槽位是否已满（注意方法名里的 `FUll` 拼写） |
| `addBeaconInfo(bean)` | 填入第一个空槽位并重排索引 |
| `addConditionBeaconInfo(bean)` | 填入第一个空条件槽位 |
| `updateBeaconInfo(bean, index)` | 按 1 开始的索引更新 |
| `deleteBeaconInfo(index)` | 按 1 开始的索引删除 |
| `deleteConditionBeaconInfo(index)` | 按 1 开始的索引删除条件槽位 |
| `saveBeaconInfo(...)` | 把槽位和参数一起下发 |

常见广播类型常量：

| 常量 | 值 | 说明 |
|---|---|---|
| `BEACON_TYPE_I_BEACON` | `iBeacon` | iBeacon |
| `BEACON_TYPE_EDDYSTONE_URL` | `URL` | Eddystone URL |
| `BEACON_TYPE_EDDYSTONE_UID` | `UID` | Eddystone UID |
| `BEACON_TYPE_EDDYSTONE_TLM` | `TLM` | Eddystone TLM |
| `BEACON_TYPE_ALTBEACON` | `AltBeacon` | AltBeacon |
| `BEACON_TYPE_AOABEACON` | `AoaBeacon` | AOA |
| `BEACON_TYPE_CUSTOM` | `CustomBeacon` | 自定义广播 |

条件广播使用 `CONDITION_*` 前缀，例如 `CONDITION_iBeacon`、`CONDITION_URL`、`CONDITION_UID`、`CONDITION_AltBeacon`、`CONDITION_CustomBeacon`。

注意当前这版 AAR 的 `addBeaconInfo()` 里，AOA 槽位写入分支是被注释掉的；AOA 的开关 `setAoa()` 仍然存在。接入 AOA 前请先和模块固件确认实际支持方式。

索引规则容易混淆：

- `BeaconBean.index` 从 `1` 开始。
- `deleteBeaconInfo(index)`、`updateBeaconInfo(bean, index)` 按 `BeaconBean.index` 匹配。
- `setBroadcastTxPower(index, ...)`、`setBroadcastPHY(index, ...)` 使用列表下标，从 `0` 开始。

### 5.6 批量配置

```kotlin
val cmd = CommandBean()
cmd.setCommand("AT+NAME=FeasyBeacon-002")
cmd.setCommand("AT+ADVIN=200")
api.saveBatch(cmd)
```

`saveBatch()` 会立即下发 `CommandBean` 里的命令。

注意 `CommandBean` 内部使用 `HashSet` 保存命令，不保证遍历顺序。如果多条 AT 指令有严格顺序依赖，不要依赖它的遍历顺序，建议逐条确认或改成业务层串行发送。

### 5.7 DFU / OTA

#### 5.7.1 解析固件

```kotlin
val info = api.checkDfuFile(dfuBytes) ?: return

val targetModule = info.type_model
val startVersion = info.versonStart
val endVersion = info.verson_soft_end
val bootloader = info.bootloader
```

`checkDfuFile()` 可能返回 `null`，使用前必须判空。

SDK 只接收固件 `byte[]`，不负责从网络下载文件。demo 中的下载地址是 `https://dfu.feasycom.com/{file_name}.dfu`，接入方可以复用这个流程，也可以使用自己的固件分发方式。

#### 5.7.2 开始升级

`startOtaUpdate()` 要求当前已经连接，并且 `dfuFile` 不为空。内部会：

1. 解密固件；
2. 去掉 1024 字节固件头；
3. 发送 `EnterDFU` 指令；
4. 绑定 `OTAService` 开始分包发送。

因此不要在调用前先断开连接。推荐流程：

```kotlin
// 1. 连接设备
api.connect(wrapper, "000000")

// 2. 在连接成功回调里开始 OTA
override fun blePeripheralConnected(gatt: BluetoothGatt?, device: BluetoothDevice?) {
    api.startOtaUpdate(dfuBytes, false)
}
```

如果业务上需要先进入 OTA 页面再重新连接，可以像 demo 的 `OtaActivity` 一样：

1. 页面初始化时断开普通连接；
2. 用户点击开始升级时重新 `connect()`；
3. 在 `blePeripheralConnected()` / `onConnectedSuccess()` 里调用 `startOtaUpdate()`。

#### 5.7.3 升级进度

```kotlin
override fun otaProgressUpdate(percentage: Int, status: Int) {
    when (status) {
        ConstantUtil.OTA_STATUS_BEGIN -> {
            // 110，开始
        }
        ConstantUtil.OTA_STATUS_PROCESSING -> {
            // 121，进行中，percentage 为进度
        }
        ConstantUtil.OTA_STATUS_FINISH -> {
            // 10086，结束
        }
    }
}
```

`sendPacketProgress()` 里的 `100`（`PACKAGE_SEND_FINISH`）和 `101`（`FIFO_SEND_FINISH`）表示数据包/FIFO 发送完成，不是 OTA 最终状态。

`reset` 参数表示升级完成后是否执行复位/恢复出厂，具体行为以固件定义为准，按业务需求传 `true` 或 `false`。

#### 5.7.4 OTA 前置条件

- App 必须声明 `com.feasycom.service.OTAService`。
- 设备必须已连接。
- 固件目标模块要与当前设备模块一致。
- 固件版本范围要匹配。
- 不要把 `.dfu` 文件截断或替换成其他格式。

### 5.8 SUOTA

部分模块使用 SUOTA，而不是普通 DFU。先判断：

```kotlin
val module = wrapper.feasyBeacon?.module?.toIntOrNull() ?: return
val useSuota = OtaUtils.needsSuotaUpgrade(module)
```

当前 `OtaUtils` 中走 SUOTA 的模块编号：

```text
39, 41, 46, 54, 56, 68, 69, 71, 72, 73, 74, 75,
79, 98, 104, 108, 116, 117, 120, 121
```

使用 `SuotaManager` 的高层流程：

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

连接和设备就绪后：

```kotlin
manager.setSuotaFile(SuotaFile(context, firmwareUri))
manager.initializeSuota(240, 3, 0, 1, 4, 2)
manager.startUpdate()
```

上面的 `initializeSuota(240, 3, 0, 1, 4, 2)` 是 demo 使用的参数组合，分别对应 block size、MISO、MOSI、CS、SCK、image bank。不同硬件/固件可能需要不同配置，不要盲目套用。

SUOTA 还需要注意：

- 文件选择权限和存储路径；
- `SuotaManager.checkPermissions()`、`checkStorage()`；
- `onFailure()` 的错误码；
- 升级完成后的重启流程；
- AAR 已包含 `RunAutoSuotaActivity` 和 SUOTA 资源，但 demo 的 `SuotaActivity` 是 App 层的二次封装示例。

实际接入建议直接阅读 demo 的：

- `ui/suota/activities/SuotaActivity.kt`
- `ui/suota/fragments/AvailableFirmwareFragment.kt`
- `ui/suota/fragments/UpdateFirmwareFragment.kt`
- `ui/suota/bluetooth/SuotaCallback.kt`

### 5.9 手机自身作为 Beacon 广播

这部分不在 `FscBeaconCentralApi` 里。demo 使用 Android 原生 `BluetoothLeAdvertiser` 实现：

- `IBeaconAdvertiser`
- `AltBeaconAdvertiser`
- `EddystoneUIDAdvertiser`
- `EddystoneURLAdvertiser`
- `EddystoneTLMAdvertiser`
- `AoABeaconAdvertiser`

入口是 `ui/beacon/CustomBroadcastActivity.kt`。如果只需要扫描和配置设备，不需要这部分代码；如果要使用，必须在 Manifest 中声明 `BLUETOOTH_ADVERTISE` 并动态申请。

---

## 6. API 参考

### 6.1 `FscBeaconCentralApi`

实现类是 `FscBeaconCentralApiImp`，单例。

```java
static FscBeaconCentralApiImp getInstance(Context context)
static FscBeaconCentralApiImp getInstance()
```

#### 6.1.1 扫描与连接

| 方法 | 返回值 | 说明 |
|---|---|---|
| `initialize()` | `boolean` | 初始化 BLE 环境 |
| `isShowLog(boolean)` | `void` | 打开/关闭 SDK 日志 |
| `isBluetoothEnabled()` | `boolean` | 蓝牙是否开启 |
| `startScan()` | `boolean` | 开始扫描，内部 90 秒超时 |
| `startScan(int duration)` | `boolean` | 开始扫描；当前实现忽略 `duration` |
| `stopScan()` | `void` | 停止扫描 |
| `connect(BluetoothDeviceWrapper, String pin)` | `boolean` | 连接并校验 PIN |
| `disconnect(String loca)` | `void` | 断开；`loca` 仅用于日志 |
| `isConnected()` | `boolean` | 是否已连接 |
| `setCallbacks(FscBeaconCentralCallbacks)` | `void` | 注册回调 |

#### 6.1.2 设备信息

| 方法 | 说明 |
|---|---|
| `getDeviceInfo(FeasyBeacon feasyBeacon, boolean queryTxPower)` | 查询设备信息，结果走 `onDeviceInfoReceived` |
| `setDeviceName(String)` | 设置设备名，加入待发送命令 |
| `setDevicePin(String)` | 设置 PIN，加入待发送命令 |
| `setBroadcastInterval(String)` | 设置广播间隔，加入待发送命令 |
| `setDeviceTxPower(String)` | 设置模块发射功率，加入待发送命令 |
| `setGSensorConfig(String interval, String duration)` | 设置 G-Sensor，加入待发送命令 |
| `setGSensorConfig(String interval, String duration, String threshold)` | 带阈值的 G-Sensor 配置 |
| `setKeyConfig(String interval, String duration)` | 设置按键触发广播 |
| `setLed(String)` | 设置 LED 指示时长 |

#### 6.1.3 广播槽位

| 方法 | 说明 |
|---|---|
| `isBeaconInfoFull()` | 普通广播槽位是否已满 |
| `isConditionBeaconInfoFUll()` | 条件广播槽位是否已满 |
| `addBeaconInfo(BeaconBean)` | 添加普通广播槽位 |
| `addConditionBeaconInfo(BeaconBean)` | 添加条件广播槽位 |
| `updateBeaconInfo(BeaconBean, String index)` | 按 1 开始的索引更新 |
| `deleteBeaconInfo(String index)` | 按 1 开始的索引删除 |
| `deleteConditionBeaconInfo(String index)` | 删除条件槽位 |
| `saveBeaconInfo(ArrayList<BeaconBean>, ArrayList<BeaconBean>)` | 保存普通和条件广播槽位，同时下发待发送参数 |
| `beaconBroadcastParameters()` | 查询/同步广播参数 |
| `setBroadcastTxPower(String index, int p)` | 设置指定槽位 TX Power；index 从 0 开始 |
| `setBroadcastTxPower(String index, String p)` | 同上，字符串版本 |
| `setBroadcastPHY(String index, int p)` | 设置指定槽位 PHY；index 从 0 开始 |

#### 6.1.4 其他能力

| 方法 | 说明 |
|---|---|
| `setTlm(boolean)` | 设置 TLM 开关，加入待发送命令 |
| `setAoa(boolean)` | 设置 AOA 开关，加入待发送命令 |
| `setBroadcastExtensions(boolean)` | 设置扩展广播，加入待发送命令 |
| `setFirmwareKey(String command)` | 立即发送固件密钥相关 AT 指令 |
| `setAppKey(String key)` | 写入 App 本地 SharedPreferences，不写设备 |
| `restore()` | 立即发送恢复出厂指令 |
| `saveBatch(CommandBean)` | 立即下发批量 AT 指令 |
| `ota()` | 组装 `AT+OTA=...` 命令 |
| `checkDfuFile(byte[])` | 解析 DFU 文件，可能返回 `null` |
| `startOtaUpdate(byte[] dfuFile, boolean reset)` | 开始 DFU 升级，要求已连接 |

### 6.2 `FscBleCentralApi`

`FscBleCentralApi` 是底层 BLE/AT 接口，方法较少：

| 方法 | 说明 |
|---|---|
| `startScan()` | 开始扫描 |
| `startScan(int duration)` | 开始扫描 |
| `stopScan()` | 停止扫描 |
| `disconnect(String loca)` | 断开连接 |
| `isConnected()` | 是否已连接 |
| `send(byte[] packet)` | 发送原始字节 |
| `stopSend()` | 停止发送 |
| `sendATCommand(Set<String> commands)` | 发送一组 AT 指令 |

`FscBleCentralApiImp` 还提供一些高级方法：

| 方法/字段 | 说明 |
|---|---|
| `setSDKDataCallbacks(...)` | 只替换数据回调 |
| `setOTACallbacks(...)` | 只替换 OTA 回调 |
| `getCallbacks()` | 获取当前回调 |
| `getDevice()` | 获取当前 `BluetoothDevice` |
| `getWriteCharacteristic()` | 获取当前写特征 |
| `startOTA(byte[], boolean)` | 开始 OTA |
| `checkDfuFile(byte[])` | 解析 DFU |
| `EN_AUTO_QUERY` | 静态开关，默认 `false` |
| `EN_AUTO_VERIFY` | 静态开关，默认 `false` |

### 6.3 回调接口

继承关系：

```text
FscCentralCallbacks
   └── FscBleCentralCallbacks
          └── FscBeaconCentralCallbacks
```

#### `FscCentralCallbacks`

| 方法 | 触发时机 |
|---|---|
| `startScan()` | 调用 `startScan()` 后 |
| `stopScan()` | 调用 `stopScan()` 后 |
| `connectProgressUpdate(BluetoothDevice, int)` | 连接/密码校验进度 |

#### `FscBleCentralCallbacks`

| 方法 | 触发时机 |
|---|---|
| `blePeripheralFound(BluetoothDevice, BluetoothDeviceWrapper, int, byte[])` | 扫描到设备 |
| `blePeripheralConnected(BluetoothGatt, BluetoothDevice)` | 连接成功 |
| `blePeripheralDisconnected(BluetoothGatt, BluetoothDevice)` | 连接断开 |
| `atCommandCallBack(String command, String param, String status)` | AT 指令结果 |
| `servicesFound(...)` | 发现 GATT 服务 |
| `characteristicForService(...)` | 发现 GATT 特征 |
| `packetReceived(...)` | 收到数据包 |
| `readResponse(...)` | 读到数据 |
| `sendPacketProgress(...)` | 发送进度 |
| `otaProgressUpdate(int percentage, int status)` | OTA 进度和状态 |

#### `FscBeaconCentralCallbacks`

| 方法 | 触发时机 |
|---|---|
| `onDeviceInfoReceived(String parameterName, Object parameter)` | `getDeviceInfo()` 的逐项查询结果 |

### 6.4 常量

#### Beacon 类型

```java
FeasyBeacon.BEACON_TYPE_I_BEACON          // "iBeacon"
FeasyBeacon.BEACON_TYPE_EDDYSTONE_URL     // "URL"
FeasyBeacon.BEACON_TYPE_EDDYSTONE_UID     // "UID"
FeasyBeacon.BEACON_TYPE_EDDYSTONE_TLM     // "TLM"
FeasyBeacon.BEACON_TYPE_ALTBEACON         // "AltBeacon"
FeasyBeacon.BEACON_TYPE_AOABEACON         // "AoaBeacon"
FeasyBeacon.BEACON_TYPE_CUSTOM            // "CustomBeacon"
```

#### 槽位数量

```java
FeasyBeacon.BEACON_COUNT      // 10
ConstantUtil.MAX_BEACONS      // 10
```

#### AT 指令状态

```java
CommandBean.COMMAND_FAILED       // "0"
CommandBean.COMMAND_SUCCESSFUL   // "1"
CommandBean.COMMAND_FINISH       // "2"
CommandBean.COMMAND_NO_NEED      // "3"
CommandBean.COMMAND_TIME_OUT     // "4"
```

#### 密码校验状态

```java
CommandBean.PASSWORD_CHECK        // 0
CommandBean.PASSWORD_SUCCESSFUL   // 1
CommandBean.PASSWORD_FAILED       // 2
CommandBean.PASSWORD_TIME_OUT     // 3
```

#### OTA / 发送进度

```java
ConstantUtil.OTA_STATUS_BEGIN        // 110
ConstantUtil.OTA_STATUS_PROCESSING   // 121
ConstantUtil.OTA_STATUS_FINISH       // 10086
ConstantUtil.PACKAGE_SEND_FINISH     // 100
ConstantUtil.FIFO_SEND_FINISH        // 101
```

### 6.5 数据模型

#### `BluetoothDeviceWrapper`

| 字段 | 含义 |
|---|---|
| `address` | 蓝牙地址 |
| `name` | 广播名称 |
| `rssi` | 信号强度 |
| `scanRecord` | 原始广播数据 |
| `completeLocalName` | 完整本地名称 |
| `iBeacon` | iBeacon 数据 |
| `eddystoneBeacon` | Eddystone 数据 |
| `altBeacon` | AltBeacon 数据 |
| `aoaBeacon` | AOA 数据 |
| `feasyBeacon` | 可配置模块信息 |
| `thSensor` | 温湿度传感器数据 |
| `gSensor` | 重力传感器数据 |

#### `FeasyBeacon`

主要字段见 5.3 节。模块号 `module` 是字符串，做 OTA/SUOTA 判断时要先转成 `Int`。

#### `BeaconBean`

常用字段：

| 字段 | 含义 |
|---|---|
| `index` | 槽位索引，从 1 开始 |
| `beaconType` | 广播类型 |
| `uuid` / `major` / `minor` | iBeacon 参数 |
| `url` | Eddystone URL |
| `nameSpace` / `instance` / `reserved` | Eddystone UID 参数 |
| `id1` / `id2` / `id3` | AltBeacon 参数 |
| `manufacturerId` / `manufacturerReserved` | AltBeacon 厂商字段 |
| `rssi` | 广播 TX Power/信号字段 |
| `txpower` | 槽位发射功率 |
| `phy` | 物理层 |
| `interval` | 广播间隔 |
| `enable` | 是否启用 |
| `connectable` | 是否可连接 |
| `customBeaconData` | 自定义广播数据 |
| `customConditionData` | 自定义条件广播数据 |
| `sensorType` / `actionType` / `intervals` / `timeout` | 条件广播参数 |

#### `DfuFileInfo`

| 字段 | 含义 |
|---|---|
| `bootloader` | Bootloader 版本 |
| `versonStart` | 固件起始版本（字段名是 `verson`，不是 `version`） |
| `verson_soft_end` | 固件结束版本 |
| `type_model` | 目标模块编号 |

#### `CommandBean`

| 方法 | 说明 |
|---|---|
| `setCommand(String)` | 添加一条 AT 指令，并按命令前缀去重 |
| `getCommand()` | 获取待发送指令集合 |

---

## 7. Demo 导读

`demo` 是完整 App 源码，优先看下面这些文件：

| 文件 | 作用 |
|---|---|
| `logic/BluetoothRepository.kt` | SDK 单例封装、回调分发、扫描/连接/参数/OTA 入口 |
| `logic/interfaces/FscBleCallback.kt` | 页面层回调接口 |
| `ui/beacon/BeaconFragment.kt` | Beacon 扫描列表 |
| `ui/sensor/SensorFragment.kt` | 传感器列表 |
| `ui/setting/SettingFragment.kt` | 可配置设备列表、PIN 输入、二维码入口 |
| `ui/parameter/ParameterSettingActivity.kt` | 参数读取、回填、保存、广播槽位、OTA/批量入口 |
| `ui/ota/OtaActivity.kt` | DFU 文件选择、下载、校验和升级 |
| `ui/suota/activities/SuotaActivity.kt` | SUOTA 升级流程 |
| `ui/batch/config/BatchConfigActivity.kt` | 批量参数选择 |
| `ui/batch/scan/BatchScanActivity.kt` | 批量扫描和多选 |
| `ui/batch/modify/BatchModifyActivity.kt` | 逐台写入和结果报表 |
| `ui/beacon/CustomBroadcastActivity.kt` | 手机自身作为 Beacon 广播 |
| `assets/Device.json` | 模块型号和参数范围 |

需要注意：`Device.json` 是 demo App 的数据，不在 AAR 里。SDK 本身不做完整的参数范围校验；参数默认值、可选 TX Power、广播间隔范围等，通常由 App 根据型号表或自己的业务规则决定。

---

## 8. 常见问题

### 8.1 扫描没有任何回调

按顺序检查：

1. 是否调用了 `getInstance(context)`、`initialize()`、`setCallbacks()`。
2. 是否授予 `BLUETOOTH_SCAN`、`BLUETOOTH_CONNECT`。
3. Android 11 及以下是否授予定位权限，并打开系统定位开关。
4. 蓝牙是否开启。
5. 是否在扫描后过早调用了 `stopScan()`。
6. 是否把 `blePeripheralFound()` 里的异常吞掉了。

### 8.2 `connect()` 返回 `false`

优先检查：

- 是否使用了带 `Context` 的 `getInstance()`。
- `wrapper.getFeasyBeacon()` 是否为 `null`。
- PIN 是否是纯数字，长度是否在 1 到 6 位之间。

### 8.3 参数设置后设备没有变化

`setDeviceName()`、`setDevicePin()`、`setBroadcastInterval()` 等只是把命令放进队列，必须调用 `saveBeaconInfo()` 或 `saveBatch()` 才会下发。

### 8.4 保存参数后广播槽位被清空

通常是保存前没有先 `getDeviceInfo()`，或者传给 `saveBeaconInfo()` 的槽位列表不完整。先等待 `BADVDATA`、`SADV` 回调，再带着当前槽位一起保存。

### 8.5 OTA 一直不开始

检查：

- 是否声明了 `com.feasycom.service.OTAService`。
- 调用 `startOtaUpdate()` 时是否已经连接。
- 是否在调用前错误地 `disconnect()`。
- 固件目标模块是否和当前设备一致。
- 固件版本范围是否匹配。
- `.dfu` 文件是否完整。

### 8.6 SUOTA 失败

检查：

- 模块是否在 `OtaUtils.needsSuotaUpgrade()` 的列表中。
- 固件文件头和 SUOTA 类型是否匹配。
- 存储/文件读取权限是否足够。
- `initializeSuota()` 的 GPIO、block size、image bank 是否与硬件匹配。
- `onFailure()` 的错误码。

### 8.7 `UnsatisfiedLinkError`

检查：

- App 是否包含 `libFeasyBeaconLibrary.so`。
- ABI 是否是 `arm64-v8a` 或 `armeabi-v7a`。
- 是否在 x86 模拟器上运行。
- 是否把 AAR 里的 `.so` 替换成了其他版本。

### 8.8 release 构建没有签名

本仓库已经移除 `signingConfigs`，`assembleRelease` 默认生成未签名 APK。需要签名包时，请在本机配置自己的 keystore；不要把 keystore 和密码提交到公开仓库。

### 8.9 版本号为什么对不上

- 交付包名：`3.3.9.9`
- 本仓库 App：`3.3.9.9`
- AAR 内部库模块：`3.3.8.7`

这是当前打包规则：对外包按 App 的 `versionName` 命名，库模块自己的版本号没有同步改。

### 8.10 为什么 `startScan(10000)` 没有在 10 秒后停止

当前 `FscBleCentralApiImp.startScan(int duration)` 没有使用 `duration`，内部固定 90 秒超时。需要精确控制时，自己调用 `stopScan()`。

---

## 9. 接入检查清单

- [ ] 已放入 `FeasyBeaconLibrary-release.aar`
- [ ] `minSdk >= 26`
- [ ] `abiFilters` 只保留 `arm64-v8a`、`armeabi-v7a`
- [ ] 已声明 `AtCommandService` 和 `OTAService`
- [ ] 已申请 `BLUETOOTH_SCAN`、`BLUETOOTH_CONNECT`
- [ ] Android 11 及以下已处理定位权限和定位开关
- [ ] 使用手机广播时已申请 `BLUETOOTH_ADVERTISE`
- [ ] 已调用 `getInstance(context)`、`initialize()`、`setCallbacks()`
- [ ] 连接前确认 `wrapper.feasyBeacon != null`
- [ ] 修改参数前先 `getDeviceInfo()`，保存时带上完整槽位
- [ ] OTA 前确认已连接，并声明 `OTAService`
- [ ] 开启混淆时已加入 SDK keep 规则
- [ ] SUOTA 已按模块型号和硬件参数配置

---

## 10. 文档说明

本文按当前交付包和源码做静态核对，重点确认了：

- AAR 的包名、`minSdk`、JNI 库、Manifest 和资源；
- `FscBeaconCentralApi`、`FscBleCentralApi` 的公开方法；
- 三层回调接口；
- DFU/SUOTA 入口和 demo 的实际调用方式；
- demo 的 Gradle、Manifest 和签名配置。

本文没有在本机执行 Gradle 编译，也没有做真机 BLE、OTA、SUOTA 验证。实际接入时如果遇到与本文不一致的现象，优先以当前 AAR 的类签名、设备固件版本和 demo 的实际运行结果为准。

交付时建议把本文放到压缩包的 `docs/FeasyBeacon使用文档.md`。当前 `sdk-package/pack-sdk.ps1` 只组装 `apk/`、`sdk/`、`demo/`，不会自动把 `docs/` 放进 zip；如果希望文档随包自动生成，需要再调整打包脚本。
