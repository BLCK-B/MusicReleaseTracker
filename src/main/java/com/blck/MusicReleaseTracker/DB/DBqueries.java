package com.blck.MusicReleaseTracker.DB;

import com.blck.MusicReleaseTracker.Core.SourcesEnum;
import com.blck.MusicReleaseTracker.DataObjects.Song;
import com.blck.MusicReleaseTracker.DataObjects.TableModel;
import com.blck.MusicReleaseTracker.Scraping.Scrapers.Scraper;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public interface DBqueries {

    List<String> getArtistList();

    void insertIntoArtistList(String name);

    List<TableModel> loadTable(SourcesEnum source, String name);

    List<TableModel> loadCombviewTable();

    String getArtistSourceID(String name, SourcesEnum source);

    void updateArtistSourceID(String name, SourcesEnum source, String ID);

    void clearArtistDataFrom(String name, String table);

    void removeArtist(String name);

    void truncateScrapeData(boolean all);

    ArrayList<Song> getAllSourceTableData();

    LinkedList<Scraper> getAllScrapers();

    void batchInsertSongs(ArrayList<Song> songList, SourcesEnum source, int limit);

    void vacuum();
}
