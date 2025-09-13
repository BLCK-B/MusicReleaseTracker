
package com.blck.MusicReleaseTracker.Scraping.Thumbnails;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

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
        // making thumbnails available statically from appdata
        registry.addResourceHandler("/thumbnails/**")
                .addResourceLocations(getPath());
        // not thumbnails - get frontend files from static/ next to root instead of from
        // resources to avoid graal compiler breaking our JS files
        registry.addResourceHandler("/frontend/**")
                .addResourceLocations("file:./static/")
                .setCachePeriod(3600)
                .resourceChain(true)
                .addResolver(new PathResourceResolver());
    }
}