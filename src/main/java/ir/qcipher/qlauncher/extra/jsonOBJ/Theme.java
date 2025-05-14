package ir.qcipher.qlauncher.extra.jsonOBJ;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Theme {
    @JsonProperty("name")
    public String name;

    @JsonProperty("path")
    public String path;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
