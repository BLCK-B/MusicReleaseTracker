/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2024 BLCK
 *         This program is free software: you can redistribute it and/or modify
 *         it under the terms of the GNU General Public License as published by
 *         the Free Software Foundation, either version 3 of the License, or
 *         (at your option) any later version.
 *         This program is distributed in the hope that it will be useful,
 *         but WITHOUT ANY WARRANTY; without even the implied warranty of
 *         MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *         GNU General Public License for more details.
 *         You should have received a copy of the GNU General Public License
 *         along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.blck.MusicReleaseTracker.DataObjects;

import java.util.List;

public record Album(String album, List<Song> songs) implements MediaItem {
	public List<Song> getAlbumSongs() {
		return songs;
	}
	@Override
	public String getDate() {
		return songs.getFirst().getDate();
	}

	@Override
	public String getAlbum() {
		return album;
	}

	@Override
	public String getName() {
		return songs.getFirst().getName();
	}
}
