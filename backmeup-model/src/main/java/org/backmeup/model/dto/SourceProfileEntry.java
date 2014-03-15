package org.backmeup.model.dto;

import java.util.HashMap;
import java.util.Map;

public class SourceProfileEntry {

    private final Long id;
    private final Map<String, String> options = new HashMap<>();

    public SourceProfileEntry(Long id) {
        this.id = id;
    }

    public SourceProfileEntry(String id) {
        this(Long.parseLong(id));
    }

    public Long getId() {
        return id;
    }

    public Map<String, String> getOptions() {
        return options;
    }

}