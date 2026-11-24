<div align="center">

  <img src="https://imglink.cc/cdn/BJytNaAdUp.png" alt="VideoFeed app logo" width="120" />

  # VideoFeed

  **A focused Android video experience for browsing videos from X profiles.**

  Discover profile videos, scroll through a continuous feed, and watch content in one place.

  [![Repository](https://img.shields.io/badge/GitHub-Repository-181717?logo=github)](https://github.com/justtech45/VideoFeed)
  [![Platform](https://img.shields.io/badge/Platform-Android-3DDC84?logo=android&logoColor=white)](https://www.android.com/)
  [![Language](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)

</div>

---

## Overview

**VideoFeed** is an Android application designed to make browsing videos from individual X (formerly Twitter) profiles more convenient. Search for a profile by username and explore its available videos through a dedicated, scrollable video feed.

The project aims to provide a straightforward viewing experience with a clean interface and built-in playback.

## Screenshots

<div align="center">
  <img src="https://imglink.cc/cdn/1z6gxgDPzy.jpg" alt="VideoFeed screenshot 1" width="31%" />
  <img src="https://imglink.cc/cdn/atKLUjDos8.jpg" alt="VideoFeed screenshot 2" width="31%" />
  <img src="https://imglink.cc/cdn/c2l-tDsz8E.jpg" alt="VideoFeed screenshot 3" width="31%" />
</div>

## Features

- **Profile-based discovery** — look up X profiles using their usernames.
- **Dedicated video feed** — browse videos associated with a selected profile.
- **Continuous scrolling** — move through the available video feed without manually opening every post.
- **In-app playback** — watch videos inside the application.
- **Video downloads** — save supported videos to your device.
- **Focused interface** — an experience centred on finding and watching profile videos.

> Feature availability can depend on the upstream services and endpoints used by the app. Changes to X or third-party services may affect functionality.

## Project Status

VideoFeed is under active development. Features, interface details, compatibility, and implementation may change between releases.

## Getting Started

### Requirements

- Android Studio
- Android SDK
- JDK compatible with the project's Gradle configuration
- Git

### Clone the repository

```bash
git clone https://github.com/justtech45/VideoFeed.git
cd VideoFeed
```

### Build the debug APK

On a configured development machine, run:

```bash
./gradlew assembleDebug
```

The debug APK is normally generated at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

You can also open the project directory in Android Studio and build it from there.

## Technology

- **Kotlin** — Android application development.
- **Android SDK** — native Android platform APIs.
- **Gradle** — project configuration and build automation.

## Acknowledgements & Credits

### Special thanks to the Nitter project

A very special and sincere thank-you to **[Zed (@zedeus)](https://github.com/zedeus)**, the creator of **[Nitter](https://github.com/zedeus/nitter)**.

Nitter is an important open-source project that demonstrated an alternative, privacy-focused way to access and browse X/Twitter content. Its work, ideas, and the wider open-source effort around it provided valuable inspiration during the development of VideoFeed.

**VideoFeed acknowledges and gives significant credit to Zed and the Nitter contributors** for their work and contribution to the open-source community.

Please visit the original project:

- **Nitter repository:** https://github.com/zedeus/nitter
- **Nitter licence:** GNU Affero General Public License v3.0 (AGPL-3.0)

VideoFeed is an independent project and is not an official Nitter application. The Nitter project and its contributors are not responsible for VideoFeed.

### Open-source community

Thank you to the Android, Kotlin, and open-source communities whose documentation, libraries, and shared knowledge help make projects like this possible.

## Disclaimer

VideoFeed is an independent, community-developed application. It is not affiliated with, endorsed by, or officially connected to X Corp. or Twitter.

X and Twitter are trademarks of their respective owners. Users are responsible for complying with applicable terms, laws, and content rights when accessing or saving content.

## Licence

VideoFeed original source code is licensed under the **MIT License**. See the [LICENSE](LICENSE) file.

### Third-party projects and libraries

- **[Nitter](https://github.com/zedeus/nitter)** - Special credit to Zed (@zedeus) and all Nitter contributors. Nitter is a separate project licensed under **AGPL-3.0**. VideoFeed independently implements its Android client and parser; it does not include Nitter server source code.
- **Third-party libraries** retain their respective licences and copyright notices.

VideoFeed is an independent project and is not an official Nitter application. The MIT licence for VideoFeed does not replace or change the licences of third-party projects or dependencies.

See the [Nitter AGPL-3.0 licence](https://github.com/zedeus/nitter/blob/master/LICENSE).
---

<div align="center">

  **VideoFeed** · Built with the open-source community in mind.

  [View source](https://github.com/justtech45/VideoFeed) · [Report an issue](https://github.com/justtech45/VideoFeed/issues)

</div>
