package ir.qcipher.qlauncher.extra.jsonOBJ.libraries;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class Extract {
    @JsonProperty("exclude")
    public List<String> exclude;
}