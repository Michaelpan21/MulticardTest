package ru.multicard.service.cache;

import lombok.Getter;
import org.springframework.cache.annotation.Cacheable;

import java.util.Collections;
import java.util.List;

@Getter
@Cacheable("ids")
public final class CrashesIds {
    private final String key;
    private final List<Long> ids;

    public CrashesIds(String key, List<Long> ids) {
        this.key = key;
        this.ids = Collections.unmodifiableList(ids);
    }
}
