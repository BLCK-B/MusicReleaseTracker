<p align="center">
  <img src="https://github.com/BLCK-B/MusicReleaseTracker/assets/123077751/f432e824-6772-401e-8419-90da707887f4" width="180px" alt="MusicReleaseTracker Image">
</p>

<h2 align="center">MusicReleaseTracker</h2>

<p align="center">
Efficient multi-source scraper that helps you find new music and follow latest songs. Available for windows, linux.
</p>

<p align="center">
  <a href="https://github.com/BLCK-B/MusicReleaseTracker/releases/latest">
    <img src="https://img.shields.io/github/v/release/BLCK-B/MusicReleaseTracker?label=Download%20Latest%20Release&sort=semver" alt="Download Latest Release">
  </a>
</p>

##

<img src="https://github.com/BLCK-B/MusicReleaseTracker/assets/123077751/2299bf41-7b3f-4992-86b6-d828b8ff23b7" width="600px"/>

##

Look at releases from individual sources and artists, see all songs ordered by date in "Combined view".
You will no longer have to search social media or streaming services in fear of missing a new song. MusicReleaseTracker is a free, open-source and no-nonsense program for music discovery.

Delays during scraping prevent excessive traffic. Currently available sources: Beatport, Musicbrainz, Junodownload.

### Setup

**Windows**: download and launch exe installer.

**Linux**: jar is provided in tar. OpenJDK or alternative and OpenJFX are required. Direct way to launch the jar (where XXX is the name of the jar):
```
java --module-path "/usr/lib/jvm/openjfx" --add-modules=javafx.controls,javafx.fxml -jar XXX.jar
```

In the program, add artists and for each artist optionally insert links of sources according to instructions.
After the initial setup, click refresh button to initiate the scraping process.

### License

MusicReleaseTracker is free software: you can redistribute it and/or modify it under the terms of the [GNU General Public License](https://www.gnu.org/licenses/gpl-3.0.html) as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
