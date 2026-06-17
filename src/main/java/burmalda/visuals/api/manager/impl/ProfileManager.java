package burmalda.visuals.api.manager.impl;

import java.time.Instant;

public class ProfileManager {
    public enum Group {
        PREMIUM,
        DEFAULT
    }

    public enum Role {
        ADMIN,
        MODERATOR,
        USER
    }

    private static String username;
    private static int uid;
    private static Instant expire;
    private static Group group = Group.DEFAULT;
    private static Role role = Role.USER;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        ProfileManager.username = username;
    }

    public static int getUid() {
        return uid;
    }

    public static void setUid(int uid) {
        ProfileManager.uid = uid;
    }

    public static Instant getExpire() {
        return expire;
    }

    public static void setExpire(Instant expire) {
        ProfileManager.expire = expire;
    }

    public static Group getGroup() {
        return group;
    }

    public static void setGroup(Group group) {
        ProfileManager.group = group;
    }

    public static Role getRole() {
        return role;
    }

    public static void setRole(Role role) {
        ProfileManager.role = role;
    }

    public static boolean isSubscriptionActive() {
        return expire != null && Instant.now().isBefore(expire);
    }
}
