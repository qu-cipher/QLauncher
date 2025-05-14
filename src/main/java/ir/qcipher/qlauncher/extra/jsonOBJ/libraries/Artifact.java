package ir.qcipher.qlauncher.extra.jsonOBJ.libraries;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Artifact {
    @JsonProperty("path")
    public String path;

    @JsonProperty("sha1")
    public String sha1;

    @JsonProperty("size")
    public long size;

    @JsonProperty("url")
    public String url;
}