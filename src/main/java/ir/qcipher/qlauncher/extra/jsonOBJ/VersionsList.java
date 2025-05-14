package ir.qcipher.qlauncher.extra.jsonOBJ;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VersionsList {
    @JsonProperty("id")
    public String id;

    @JsonProperty("type")
    public String type;

    @JsonProperty("url")
    public String url;

    @JsonProperty("time")
    public String time;

    @JsonProperty("releaseTime")
    public String releaseTime;
}
