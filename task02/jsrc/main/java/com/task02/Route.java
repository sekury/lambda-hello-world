package com.task02;

import java.util.Objects;

public final class Route {

    private final String method;
    private final String path;

    public Route(String method, String path) {
        this.method = method;
        this.path = path;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Route route = (Route) o;
        return Objects.equals(method, route.method) && Objects.equals(path, route.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, path);
    }
}
