package ir.qcipher.qlauncher.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.qcipher.qlauncher.extra.jsonOBJ.LauncherConfigJson;
import ir.qcipher.qlauncher.extra.jsonOBJ.User;

import java.io.IOException;
import java.nio.file.Path;

public class LauncherConfigUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static boolean removeUser(Path configPath, String username) {
        try {
            LauncherConfigJson config = mapper.readValue(configPath.toFile(), LauncherConfigJson.class);

            User user = new User();
            user.setName(username);

            boolean removed = config.users.remove(user);
            mapper.writeValue(configPath.toFile(), config);

            return removed;
        } catch (IOException e) {
            return false;
        }
    }
}
