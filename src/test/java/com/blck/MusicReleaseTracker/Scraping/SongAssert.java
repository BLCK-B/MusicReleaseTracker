package com.blck.MusicReleaseTracker.Scraping;

import com.blck.MusicReleaseTracker.DataObjects.Song;
import org.assertj.core.api.AbstractAssert;

public class SongAssert extends AbstractAssert<SongAssert, Song> {

    public SongAssert(Song actual) {
        super(actual, SongAssert.class);
    }

    public static SongAssert assertThat(Song actual) {
        return new SongAssert(actual);
    }

    public SongAssert dataMatches(Song s) {
        if (!actual.toString().equalsIgnoreCase(s.toString()))
            failWithMessage(actual + " does not match " + s);
        return this;
    }
}
