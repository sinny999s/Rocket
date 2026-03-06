
package rich.util.session;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.User;

public class SessionChanger {
    private static Consumer<User> sessionSetter;

    public static void setSessionSetter(Consumer<User> setter) {
        sessionSetter = setter;
    }

    public static void changeUsername(String newUsername) {
        if (sessionSetter == null || newUsername == null || newUsername.isEmpty()) {
            return;
        }
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + newUsername).getBytes());
        User newSession = new User(newUsername, uuid, "", Optional.empty(), Optional.empty());
        sessionSetter.accept(newSession);
    }

    public static String getCurrentUsername() {
        Minecraft mc = Minecraft.getInstance();
        if (mc != null && mc.getUser() != null) {
            return mc.getUser().getName();
        }
        return "";
    }
}

