
package rich.util.repository.friend;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import rich.util.config.impl.friend.FriendConfig;
import rich.util.repository.friend.Friend;

public final class FriendUtils {
    private static final List<Friend> friends = new ArrayList<Friend>();

    public static void addFriend(Player player) {
        FriendUtils.addFriend(player.getName().getString());
    }

    public static void addFriend(String name) {
        if (!FriendUtils.isFriend(name)) {
            friends.add(new Friend(name));
        }
    }

    public static void addFriendAndSave(String name) {
        FriendUtils.addFriend(name);
        FriendConfig.getInstance().save();
    }

    public static void removeFriend(Player player) {
        FriendUtils.removeFriend(player.getName().getString());
    }

    public static void removeFriend(String name) {
        friends.removeIf(friend -> friend.getName().equalsIgnoreCase(name));
    }

    public static void removeFriendAndSave(String name) {
        FriendUtils.removeFriend(name);
        FriendConfig.getInstance().save();
    }

    public static boolean isFriend(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            return FriendUtils.isFriend(player.getName().getString());
        }
        return false;
    }

    public static boolean isFriend(String friend) {
        return friends.stream().anyMatch(isFriend -> isFriend.getName().equalsIgnoreCase(friend));
    }

    public static void clear() {
        friends.clear();
    }

    public static void clearAndSave() {
        FriendUtils.clear();
        FriendConfig.getInstance().save();
    }

    public static List<String> getFriendNames() {
        return friends.stream().map(Friend::getName).collect(Collectors.toList());
    }

    public static int size() {
        return friends.size();
    }

    public static void setFriends(List<String> names) {
        friends.clear();
        for (String name : names) {
            friends.add(new Friend(name));
        }
    }

    @Generated
    private FriendUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static List<Friend> getFriends() {
        return friends;
    }
}

