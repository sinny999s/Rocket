
package rich.util.mods.config.wave;

import antidaunleak.api.annotation.Native;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class HeartbeatManager {
    private static ScheduledExecutorService scheduler;
    private static String systemHwid;
    private static String profileHwid;
    private static String currentUsername;
    private static String currentUid;

    @Native(type=Native.Type.VMProtectBeginUltra)
    private static String g1() {
        char[] k = new char[]{'h', 't', 't', 'p', ':', '/', '/', '8', '7', '.', '1', '2', '0', '.', '1', '8', '6', '.', '1', '8', '6', ':', '3', '0', '0', '0'};
        StringBuilder sb = new StringBuilder();
        for (char c : k) {
            sb.append(c);
        }
        return sb.toString();
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private static String g2() {
        char[] k = new char[]{'V', 'M', '$', 'U', 'v', 'w', '9', 'u', '6', 'W', 'C', 'U', '6', '5', '9', '0', 'w', 'q', '6', 'u', 'j', 't', 'e', 'g', 's', 'a'};
        StringBuilder sb = new StringBuilder();
        for (char c : k) {
            sb.append(c);
        }
        return sb.toString();
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private static String g3() {
        char[] k = new char[]{'/', 'a', 'p', 'i', '/', 'r', 'e', 'g', 'i', 's', 't', 'e', 'r'};
        StringBuilder sb = new StringBuilder();
        for (char c : k) {
            sb.append(c);
        }
        return sb.toString();
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private static String g4() {
        char[] k = new char[]{'/', 'a', 'p', 'i', '/', 'h', 'e', 'a', 'r', 't', 'b', 'e', 'a', 't'};
        StringBuilder sb = new StringBuilder();
        for (char c : k) {
            sb.append(c);
        }
        return sb.toString();
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private static String g5() {
        char[] k = new char[]{'/', 'a', 'p', 'i', '/', 'o', 'f', 'f', 'l', 'i', 'n', 'e'};
        StringBuilder sb = new StringBuilder();
        for (char c : k) {
            sb.append(c);
        }
        return sb.toString();
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    public static void start(String sysHwid, String profHwid, String username, String uid) {
        systemHwid = sysHwid;
        profileHwid = profHwid;
        currentUsername = username;
        currentUid = uid;
        new Thread(() -> {
            HeartbeatManager.register();
            scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.scheduleAtFixedRate(HeartbeatManager::heartbeat, 0L, 10L, TimeUnit.SECONDS);
        }).start();
        Runtime.getRuntime().addShutdownHook(new Thread(HeartbeatManager::offline));
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private static void register() {
        try {
            String json = String.format("{\"secret\":\"%s\",\"systemHwid\":\"%s\",\"profileHwid\":\"%s\",\"username\":\"%s\",\"uid\":\"%s\"}", HeartbeatManager.g2(), HeartbeatManager.escape(systemHwid), HeartbeatManager.escape(profileHwid != null ? profileHwid : ""), HeartbeatManager.escape(currentUsername), HeartbeatManager.escape(currentUid));
            HeartbeatManager.sendPost(HeartbeatManager.g1() + HeartbeatManager.g3(), json);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private static void heartbeat() {
        try {
            String json = String.format("{\"secret\":\"%s\",\"systemHwid\":\"%s\",\"profileHwid\":\"%s\"}", HeartbeatManager.g2(), HeartbeatManager.escape(systemHwid), HeartbeatManager.escape(profileHwid != null ? profileHwid : ""));
            String response = HeartbeatManager.sendPost(HeartbeatManager.g1() + HeartbeatManager.g4(), json);
            if (response != null && (response.contains("\"kill\":true") || response.contains("\"banned\":true"))) {
                HeartbeatManager.shutdown();
            }
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private static void offline() {
        try {
            String json = String.format("{\"secret\":\"%s\",\"systemHwid\":\"%s\",\"profileHwid\":\"%s\"}", HeartbeatManager.g2(), HeartbeatManager.escape(systemHwid), HeartbeatManager.escape(profileHwid != null ? profileHwid : ""));
            HeartbeatManager.sendPost(HeartbeatManager.g1() + HeartbeatManager.g5(), json);
        }
        catch (Exception exception) {
            // empty catch block
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private static String sendPost(String urlStr, String json) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod(HeartbeatManager.d("UE9TVA=="));
            conn.setRequestProperty(HeartbeatManager.d("Q29udGVudC1UeXBl"), HeartbeatManager.d("YXBwbGljYXRpb24vanNvbg=="));
            conn.setRequestProperty(HeartbeatManager.d("VXNlci1BZ2VudA=="), HeartbeatManager.d("UmljaENsaWVudC8yLjA="));
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
            }
            int code = conn.getResponseCode();
            if (code != 200) return null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                StringBuilder response = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                return response.toString();
            }
        } catch (Exception e) {
            return null;
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private static void shutdown() {
        try {
            Runtime.getRuntime().halt(0);
        }
        catch (Throwable t) {
            System.exit(0);
        }
    }

    @Native(type=Native.Type.VMProtectBeginUltra)
    private static String d(String b) {
        try {
            return new String(Base64.getDecoder().decode(b), StandardCharsets.UTF_8);
        }
        catch (Exception e) {
            return "";
        }
    }
}

