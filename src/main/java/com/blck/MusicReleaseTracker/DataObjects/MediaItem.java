/*
 *         MusicReleaseTracker
 *         Copyright (C) 2023 - 2025 BLCK
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

import java.util.Optional;

/**
 * MediaItem may represent a {@code Song} or an {@code Album}. </br>
 * The shared parameters of a song and album are are date and name.
 */
public interface MediaItem {

    String getDate();

    String getName();

    String getAlbum();

    Optional<String> getThumbnailUrl();
}