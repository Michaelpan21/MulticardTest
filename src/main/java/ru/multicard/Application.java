package ru.multicard;

import org.apache.geode.cache.client.ClientRegionShortcut;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.gemfire.cache.config.EnableGemfireCaching;
import org.springframework.data.gemfire.config.annotation.ClientCacheApplication;
import org.springframework.data.gemfire.config.annotation.EnableCachingDefinedRegions;

@SpringBootApplication
@ClientCacheApplication(name = "GemFireApp", logLevel = "error")
@EnableCachingDefinedRegions(clientRegionShortcut = ClientRegionShortcut.LOCAL)
@EnableGemfireCaching
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
