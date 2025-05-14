package ir.qcipher.qlauncher.extra.jsonOBJ.libraries;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Downloads {
    @JsonProperty("artifact")
    public Artifact artifact;

    @JsonProperty("classifiers")
    public Map<String, Artifact> classifiers;
}