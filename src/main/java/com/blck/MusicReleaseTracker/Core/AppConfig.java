package com.blck.MusicReleaseTracker.Core;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app")
public record AppConfig(String version) {
}