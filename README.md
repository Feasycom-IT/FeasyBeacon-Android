# FeasyBeacon Android App（应用层源码）

FeasyBeacon 蓝牙信标配置 App 的**应用层源码**。蓝牙协议与设备操作能力由 **FeasyBeaconLibrary** 提供，本仓库**不包含 SDK 源码**，只以 AAR 形式引用：

```text
feasybeacon/libs/FeasyBeaconLibrary-release.aar
```

> Application-layer source of the FeasyBeacon beacon configuration app.
> The BLE/protocol layer is provided by **FeasyBeaconLibrary** as a prebuilt AAR — its source is **not** included in this repository.

---

## 下载安装包 / Download APK

| 版本 | 文件 | 大小 | SHA-256 |
|---|---|---|---|
| v3.3.9.15 | [apk/FeasyBeacon_V3.3.9.15.apk](apk/FeasyBeacon_V3.3.9.15.apk) | 17.67 MiB（18,525,347 B） | `04634702482401ECD21BD5B5FBDBFE920519E2F971107D7D68ED2D7F966DF591` |

- 安装包为已构建的 APK，可直接安装/覆盖升级；如需核验签名，请使用 `apksigner` 查看。
- 适用系统：**Android 10 及以上**（minSdk 29）。
- 源码版本和 APK 版本均为 **3.3.9.15**；AAR 内部库模块版本为 `3.3.9.13`。
- 说明：`apk/` 下的 APK 使用内部发布签名；公开源码已移除签名配置，因此 APK 与公开源码在签名上不完全一致。需要与源码完全一致的包，请自行编译并签名。

## 1. 引入 SDK / Add the SDK

1. 把 `FeasyBeaconLibrary-release.aar` 放到 `feasybeacon/libs/`（仓库已内置一份）。
2. 依赖已配置好，`feasybeacon/build.gradle` 中：

```groovy
implementation fileTree(dir: "libs", include: ["*.jar", "*.aar"])
```

3. AAR 的 SHA-256（请核对，防止被替换）：

```text
A3734C5DA5A198704F02BB629FC13F78441EF5591D43AD96292E785907821EEB
```

> AAR 已自带 `libFeasyBeaconLibrary.so`，本仓库没有再单独放置 `.so` 目录；不要替换或重命名原生库。

## 2. 编译 / Build

环境要求：**Android Studio（JDK 17）**、Android SDK **API 35**、Gradle **8.13**、AGP **8.13.0**、minSdk 29。

```bash
./gradlew :feasybeacon:assembleDebug
```

> 仓库根目录只包含 `:feasybeacon` 一个模块。

> ⚠️ 本仓库**已移除签名配置**（`signingConfigs` 中的 keystore 与密码属于内部资源）。
> `assembleRelease` 默认生成未签名 APK；需要签名包时，请在自己的机器上配置 `signingConfigs { release { ... } }`，
> 且**不要**把 keystore 文件和密码提交到公开仓库。
> Signing config has been removed on purpose. Configure your own keystore locally and never commit it.

## 3. 应用信息 / App info

| 项 | 值 |
|---|---|
| applicationId | `com.feasycom.feasybeacon` |
| versionName | 3.3.9.15 |
| versionCode | 33915 |
| minSdk / targetSdk | 29 / 36 |
| compileSdk | 36 |
| ABI | `arm64-v8a`、`armeabi-v7a` |

## 4. FeasyBeacon SDK 使用文档 / SDK documents

| 语言 | 文档 |
|---|---|
| 中文 | [docs/FeasyBeacon使用文档.md](docs/FeasyBeacon使用文档.md) |
| English | [docs/FeasyBeacon_Integration_Guide_EN.md](docs/FeasyBeacon_Integration_Guide_EN.md) |

> 使用文档包含初始化、权限、Manifest、扫描/连接、参数配置、广播槽位、批量配置、DFU/SUOTA 和常见问题。

## 5. 目录结构 / Layout

```text
.
├── apk/
│   └── FeasyBeacon_V3.3.9.15.apk
├── build.gradle                 # 根构建脚本
├── settings.gradle              # 只包含 :feasybeacon
├── gradle/wrapper/              # Gradle Wrapper
├── feasybeacon/                 # 应用层（Application module）
│   ├── build.gradle             # 依赖 FeasyBeaconLibrary-release.aar
│   ├── libs/FeasyBeaconLibrary-release.aar
│   └── src/main/...
└── docs/
    └── FeasyBeacon使用文档.md
```

## 6. 说明 / Notes

- 本仓库只发布**应用层源码、APK、SDK 使用文档和预编译 AAR**，不包含 FeasyBeaconLibrary 源码和签名密钥。
- 内网与公开源码均已移除 Bugly 依赖，保留 `CustomActivityOnCrash` 作为崩溃兜底。
- AAR 包含 BLE/GATT、AT 指令、加密、DFU 和 SUOTA；手机端 iBeacon/AltBeacon/Eddystone 广播在 `ui/setting/*Advertiser.java` 中实现。
- `Device.json` 是 App 层的型号参数表，不在 AAR 内。
- 完整接入说明见 [docs/FeasyBeacon使用文档.md](docs/FeasyBeacon使用文档.md)。
- This repository contains only public resources: app-layer source, APK, SDK documentation and the prebuilt AAR.
