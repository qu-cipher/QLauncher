package ir.qcipher.qlauncher.extra.jsonOBJ.libraries;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MinecraftLibraries {
    @JsonProperty("libraries")
    public List<Library> libraries;
}
