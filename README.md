# Drakorid Stream

Native Android streaming app for drakorid.co (drama Korea / China / etc).

**Stack**: Kotlin 2.0 + Jetpack Compose + Material 3, ExoPlayer (Media3), Retrofit + OkHttp, Hilt DI, Room.

## Repo structure

```
.
├── android/                   # Android Studio project
│   ├── app/                 # :app module
│   ├── gradle/libs.versions.toml
│   ├── .github/workflows/   # CI: build.yml + release.yml
│   ├── scripts/set-version.sh
│   └── SETUP.md             # How to bootstrap gradle-wrapper.jar
└── planning/PLANNING.md     # 10-week roadmap
```

## Quick start

```bash
cd android
./gradlew assembleDebug              # build APK
./gradlew installDebug               # build + install to connected device
```

See `android/SETUP.md` for first-time wrapper bootstrap, and `android/README.md` for the full build/release/CI guide.

## Status

- [x] M1 foundation: project structure, MD3 theme, navigation, network layer, HomeScreen
- [ ] M2 browse: SearchScreen, CategoryList, DramaDetail with comments
- [ ] M3 player: ExoPlayer + quality selector 720p/1080p
- [ ] M4 history + downloads: Room + Media3 DownloadManager
- [ ] M5 polish + test

## Backend

This app consumes the public drakorid.co endpoints. No API key, no auth required.
See the recon report at `/data/data/com.termux/files/home/design-analysis/` for the
full endpoint catalog (`ENDPOINTS.md`) and architecture analysis (`REPORT.md`).