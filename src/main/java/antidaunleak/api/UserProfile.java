
package antidaunleak.api;

import antidaunleak.api.annotation.Native;
import java.util.HashMap;
import java.util.Map;

public class UserProfile {
    private static final UserProfile instance = new UserProfile();
    private final Map<String, String> cache = new HashMap<String, String>();
    private boolean nativeFailed = false;

    public static UserProfile getInstance() {
        return instance;
    }

    private UserProfile() {
        try {
            this.cache.put("username", this.getUsername());
            this.cache.put("hwid", this.getHwid());
            this.cache.put("role", this.getRole());
            this.cache.put("uid", this.getUid());
            this.cache.put("subTime", this.getSubsTime());
        }
        catch (UnsatisfiedLinkError e) {
            this.nativeFailed = true;
            this.cache.put("username", "null");
            this.cache.put("hwid", "null");
            this.cache.put("role", "null");
            this.cache.put("uid", "null");
            this.cache.put("subTime", "null");
        }
    }

    @Native(type=Native.Type.STANDARD)
    private native String getUsername();

    @Native(type=Native.Type.STANDARD)
    private native String getHwid();

    @Native(type=Native.Type.STANDARD)
    private native String getRole();

    @Native(type=Native.Type.STANDARD)
    private native String getUid();

    @Native(type=Native.Type.STANDARD)
    private native String getSubsTime();

    public String profile(String profile) {
        return this.cache.getOrDefault(profile, "");
    }
}

