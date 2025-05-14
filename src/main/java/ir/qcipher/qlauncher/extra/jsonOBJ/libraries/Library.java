package ir.qcipher.qlauncher.extra.jsonOBJ.libraries;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

public class Library {
    @JsonProperty("name")
    public String name;

    @JsonProperty("downloads")
    public Downloads downloads;

    @JsonProperty("rules")
    public List<Rule> rules;

    @JsonProperty("natives")
    public Map<String, String> natives;

    @JsonProperty("extract")
    public Extract extract;
}
