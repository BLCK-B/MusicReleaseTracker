<p align="center">
  <img src="https://github.com/BLCK-B/MusicReleaseTracker/assets/123077751/f432e824-6772-401e-8419-90da707887f4" width="180px" alt="MusicReleaseTracker Image">
</p>

<h2 align="center">MusicReleaseTracker</h2>

<p align="center">
Efficient multi-source scraper that helps you find new music and follow latest songs. Available for windows, linux.
</p>
<p align="center">
This program is in active development and is not quite there yet. If it does not work as expected, file an issue or check back later, thanks.
</p>

<p align="center">
  <a href="https://github.com/BLCK-B/MusicReleaseTracker/releases/latest">
    <img src="https://img.shields.io/github/v/release/BLCK-B/MusicReleaseTracker?label=Download%20Latest%20Release&sort=semver" alt="Download Latest Release">
  </a>
</p>

##

<img src="https://github.com/BLCK-B/MusicReleaseTracker/assets/123077751/689c6c97-73b3-4482-9139-3d4107d1ae35" width="550px"/>

Vue.js preview screenshots
-
<img src="https://github.com/BLCK-B/MusicReleaseTracker/assets/123077751/415f0485-0673-45ff-a311-76cf05467516" width="480px"/>
<img src="https://github.com/BLCK-B/MusicReleaseTracker/assets/123077751/25992cb1-ba9a-4107-90e3-44621291bdac" width="480px"/>

##

Look at music releases from individual artists and see all songs ordered by date in "Combined view".
You will no longer have to search social media or streaming services in fear of missing a new song. MusicReleaseTracker is a free, open-source, no-nonsense program for music discovery.

Currently available sources: Beatport, Musicbrainz, Junodownload.

Setup
-

**dependencies:** A web browser. UI is rendered there.

**Windows:** Download and install. When launched, Spring terminal and browser window/tab will open.

**Linux:** Not set up yet.

---
**In MRT:** Add artists you want to track and for each artist optionally insert links according to instructions.
With links inserted, click refresh button to initiate the scraping process.

**Responsibility**: Delays during scraping prevent excessive traffic.
To avoid granting admin permission, unzip the installer. You can run the exe, but ensure a folder *MusicReleaseTracker* exists in appdata. 
I have no affiliation with Musicbrainz, Beatport, Junodownload. I can only recommend contributing to [crowdsourced](https://musicbrainz.org/doc/How_to_Contribute) projects and directly supporting artists via Bandcamp and other means.

---

**v5.1 and older**

**JDK not found [#5](https://github.com/BLCK-B/MusicReleaseTracker/issues/5)**: you can resolve this by [downloading JDK](https://www.oracle.com/java/technologies/downloads/). Tested with JDK20. Location must be `C:\Program Files\Java\jdk-20` ***exactly***.

**Windows:** Download and install. Won´t function without JDK.

**Linux**: Unzip tar. OpenJDK or alternative and OpenJFX are required. Direct way to launch the jar (XXX name of the jar):
```
java --module-path "/usr/lib/jvm/openjfx" --add-modules=javafx.controls,javafx.fxml -jar XXX.jar
```


License
-

MusicReleaseTracker is free software: you can redistribute it and/or modify it under the terms of the [GNU General Public License](https://www.gnu.org/licenses/gpl-3.0.html) as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
