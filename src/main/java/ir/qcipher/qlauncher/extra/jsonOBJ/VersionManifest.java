package ir.qcipher.qlauncher.extra.jsonOBJ;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class VersionManifest {
    @JsonProperty("latestVersion")
    public LatestVersion latestVersion;

    @JsonProperty("versions")
    public List<VersionsList> versionsLists;
}
