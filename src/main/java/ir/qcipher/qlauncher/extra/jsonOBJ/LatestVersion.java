package ir.qcipher.qlauncher.extra.jsonOBJ;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LatestVersion {
    @JsonProperty("release")
    public String release;
    @JsonProperty("snapshot")
    public String snapshot;
}
