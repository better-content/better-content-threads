package com.bettercontent.learningsurfaces;

import java.util.Collection;

/** Fixed packaged card catalogue. Its authoring source is validated before packaging. */
public final class ThreadDefinitions {
    public static final ThreadDefinitions INSTANCE = new ThreadDefinitions();

    private ThreadDefinitions() {}

    public Collection<ThreadDefinition> all() { return ThreadArt.BY_ID.values(); }
    public ThreadDefinition get(String id) { return ThreadArt.BY_ID.get(id); }
    public boolean contains(String id) { return ThreadArt.BY_ID.containsKey(id); }
}
