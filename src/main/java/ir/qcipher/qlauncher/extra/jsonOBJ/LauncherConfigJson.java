package ir.qcipher.qlauncher.extra.jsonOBJ;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class LauncherConfigJson {
    @JsonProperty("themes")
    public List<Theme> themes;

    @JsonProperty("users")
    public List<User> users;

    public LauncherConfigJson() {}

    public LauncherConfigJson(List<Theme> themes, List<User> users) {
        this.themes = themes;
        this.users = users;
    }

    public List<Theme> getThemes() {
        return themes;
    }

    public void setThemes(List<Theme> themes) {
        this.themes = themes;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }
}
