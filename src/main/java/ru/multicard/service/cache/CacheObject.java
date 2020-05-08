package ru.multicard.service.cache;

import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public final class CacheObject {
    private final String key;
    private final List<Long> ids;

    public CacheObject(String key, List<Long> ids) {
        this.key = key;
        this.ids = Collections.unmodifiableList(ids);
    }
}
