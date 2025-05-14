package ir.qcipher.qlauncher.extra.jsonOBJ.libraries;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Rule {
    @JsonProperty("action")
    public String action;

    @JsonProperty("os")
    public OS os;
}