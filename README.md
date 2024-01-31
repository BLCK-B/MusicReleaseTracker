<p align="center">
  <img src="https://github.com/BLCK-B/MusicReleaseTracker/assets/123077751/f432e824-6772-401e-8419-90da707887f4" width="180px" alt="MusicReleaseTracker Image">
</p>

<h2 align="center">MusicReleaseTracker</h2>

<p align="center">
No-nonsense multi-source scraper that helps you find new music and follow latest songs. Available for Windows, Linux, macOS*.
</p>

<p align="center">
  <a href="https://github.com/BLCK-B/MusicReleaseTracker/releases/latest">
    <img src="https://img.shields.io/github/v/release/BLCK-B/MusicReleaseTracker?label=Download%20Latest%20Release" alt="Download Latest Release">
  </a>
</p>

##

<img src="https://github.com/BLCK-B/MusicReleaseTracker/assets/123077751/82621c39-73fb-43c0-b5b9-eab7313eb417" width="480px"/>
<img src="https://github.com/BLCK-B/MusicReleaseTracker/assets/123077751/3de8e98a-eee8-4eca-baf3-06fa56a9543e" width="480px"/>
<img src="https://github.com/BLCK-B/MusicReleaseTracker/assets/123077751/b9120ff1-e74a-4eae-b702-699fcf2b769d" width="480px"/>

##

Look at music releases from individual artists and see all songs ordered by date in "Combined view".
You will no longer have to search social media or streaming services in fear of missing a new song. MusicReleaseTracker is a free open-source tool for music discovery.

Currently available sources: Beatport, Musicbrainz, Junodownload, Youtube.

Setup
-

**dependencies:** A web browser: UI is rendered there. Works best with firefox. Linux: OpenJDK.

**Windows:** Download and install. When launched, Spring terminal and browser window/tab will open.

**Linux:** Unzip tar. Run jar by ```java -jar XXX.jar```

**macOS:** *Should be same as Linux. Not tested. Could someone please confirm?

---

**Responsibility**:

Delays during scraping prevent excessive traffic.
To avoid granting admin permission, can run the exe from unzipped installer.

I have no affiliation with Musicbrainz, Beatport, Junodownload, Youtube. I can only recommend contributing to [crowdsourced](https://musicbrainz.org/doc/How_to_Contribute) projects and directly supporting artists via Bandcamp and other means.

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
