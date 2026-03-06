
package rich.util.repository.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Generated;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import rich.util.config.impl.staff.StaffConfig;
import rich.util.repository.staff.Staff;

public final class StaffUtils {
    private static final List<Staff> staffList = new ArrayList<Staff>();

    public static void addStaff(String name) {
        if (!StaffUtils.isStaff(name)) {
            staffList.add(new Staff(name));
        }
    }

    public static void addStaffAndSave(String name) {
        StaffUtils.addStaff(name);
        StaffConfig.getInstance().save();
    }

    public static void removeStaff(String name) {
        staffList.removeIf(staff -> staff.getName().equalsIgnoreCase(name));
    }

    public static void removeStaffAndSave(String name) {
        StaffUtils.removeStaff(name);
        StaffConfig.getInstance().save();
    }

    public static boolean isStaff(Entity entity) {
        if (entity instanceof Player) {
            Player player = (Player)entity;
            return StaffUtils.isStaff(player.getName().getString());
        }
        return false;
    }

    public static boolean isStaff(String name) {
        return staffList.stream().anyMatch(staff -> staff.getName().equalsIgnoreCase(name));
    }

    public static void clear() {
        staffList.clear();
    }

    public static void clearAndSave() {
        StaffUtils.clear();
        StaffConfig.getInstance().save();
    }

    public static List<String> getStaffNames() {
        return staffList.stream().map(Staff::getName).collect(Collectors.toList());
    }

    public static int size() {
        return staffList.size();
    }

    public static void setStaff(List<String> names) {
        staffList.clear();
        for (String name : names) {
            staffList.add(new Staff(name));
        }
    }

    @Generated
    private StaffUtils() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Generated
    public static List<Staff> getStaffList() {
        return staffList;
    }
}

