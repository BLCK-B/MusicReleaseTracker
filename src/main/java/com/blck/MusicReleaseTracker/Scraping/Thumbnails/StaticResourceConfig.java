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

package com.blck.MusicReleaseTracker.Scraping.Thumbnails;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    public String getPath() {
        final String slash = File.separator;
        String appData = null;
        final String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win"))
            appData = System.getenv("APPDATA");
        else if (os.contains("nix") || os.contains("nux") || os.contains("mac"))
            appData = System.getProperty("user.home");
        else
            throw new UnsupportedOperationException("unsupported OS");
        return "file:" + slash + appData + slash + "MusicReleaseTracker" + slash + "thumbnails";
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations(getPath());
    }
}