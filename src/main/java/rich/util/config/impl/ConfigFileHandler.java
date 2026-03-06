
package rich.util.config.impl;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import rich.util.config.impl.ConfigPath;
import rich.util.config.impl.consolelogger.Logger;

public class ConfigFileHandler {
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public void createDirectories() {
        try {
            Files.createDirectories(ConfigPath.getConfigDirectory(), new FileAttribute[0]);
        }
        catch (IOException e) {
            Logger.error("AutoConfiguration: Failed to create directories!");
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public boolean write(String content) {
        this.lock.writeLock().lock();
        try {
            Path configFile = ConfigPath.getConfigFile();
            Path tempFile = configFile.resolveSibling(String.valueOf(configFile.getFileName()) + ".tmp");
            Files.writeString((Path)tempFile, (CharSequence)content, (Charset)StandardCharsets.UTF_8, (OpenOption[])new OpenOption[0]);
            Files.move(tempFile, configFile, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            boolean bl = true;
            return bl;
        }
        catch (IOException e) {
            Logger.error("AutoConfiguration: Write failed! " + e.getMessage());
            boolean bl = false;
            return bl;
        }
        finally {
            this.lock.writeLock().unlock();
        }
    }

    public String read() {
        this.lock.readLock().lock();
        try {
            Path configFile = ConfigPath.getConfigFile();
            if (!Files.exists(configFile, new LinkOption[0])) {
                String string = null;
                return string;
            }
            String string = Files.readString((Path)configFile, (Charset)StandardCharsets.UTF_8);
            return string;
        }
        catch (IOException e) {
            Logger.error("AutoConfiguration: Read failed! " + e.getMessage());
            String string = null;
            return string;
        }
        finally {
            this.lock.readLock().unlock();
        }
    }

    public boolean exists() {
        return Files.exists(ConfigPath.getConfigFile(), new LinkOption[0]);
    }
}

