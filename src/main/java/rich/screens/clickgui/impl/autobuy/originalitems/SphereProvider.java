
package rich.screens.clickgui.impl.autobuy.originalitems;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntArrayTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;
import rich.screens.clickgui.impl.autobuy.AutoBuyableItem;
import rich.screens.clickgui.impl.autobuy.items.customitem.CustomItem;
import rich.screens.clickgui.impl.autobuy.items.price.Defaultpricec;

public class SphereProvider {
    public static List<AutoBuyableItem> getSpheres() {
        ArrayList<AutoBuyableItem> spheres = new ArrayList<AutoBuyableItem>();
        List chaosLore = List.of(Component.literal((String)"Chaos distorts reality,"), (Object)Component.literal((String)"Intensifying your onslaught,"), (Object)Component.literal((String)"At the cost of life force."));
        spheres.add(SphereProvider.createSphere("[★] Chaos Sphere", "0000000b-0000-000b-0000-000b0000000b", "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODY0MTkwMCwKICAicHJvZmlsZUlkIiA6ICIxNzRjZmRiNGEzY2I0M2I1YmZjZGU0MjRjM2JiMmM2ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJtYXJhZWwxOCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9lN2E3YWU3Y2RjZjYxNmU4YjdhNDIyMWE2MjFiMjQzNTc1M2M2MGVkNmEyNThlYTA2MGRhZTMwMDJmZmU5ZTI4IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=", Defaultpricec.getPrice("Chaos Sphere"), chaosLore));
        List satirLore = List.of(Component.literal((String)"The Satyr's whisper sounds,"), (Object)Component.literal((String)"Speeding up the reckoning,"), (Object)Component.literal((String)"But restricting jump."));
        spheres.add(SphereProvider.createSphere("[★] Satyr Sphere", "0000000b-0000-000b-0000-000b0000000b", "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODYwODUyOCwKICAicHJvZmlsZUlkIiA6ICJkMTQ4NjFiM2UwZmM0Njk5OTFlMTcyNTllMzdiZjZhZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJyYXhpdG9jbCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83NzFhOWE0OThiNGZhNWVjNDkzNjJmOWJjODhlZGE0ZjUyYjA0ZGU0OWQ3NWFhM2NhMzMyYTFmZWExYWEwZTU3IiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=", Defaultpricec.getPrice("Satyr Sphere"), satirLore));
        List bestiaLore = List.of(Component.literal((String)"Wild beastly power"), (Object)Component.literal((String)"Sharpens reactions,"), (Object)Component.literal((String)"Strengthening your body."));
        spheres.add(SphereProvider.createSphere("[★] Beast Sphere", "0000000b-0000-000b-0000-000b0000000b", "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0MzgzNDkzMCwKICAicHJvZmlsZUlkIiA6ICI1MzUzNWIxN2M0ZDY0NWQ0YWUwY2U2ZjM4Zjk0NTFjYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJVYml2aXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTQxMWFjMTczODFiOWZjZTliYWIzYzcyYWZkYjdmMTk4NTcwZGFmNDczMmJkODExZDMxYzIyN2Q4MGZhMzliMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9", Defaultpricec.getPrice("Beast Sphere"), bestiaLore));
        List aresLore = List.of(Component.literal((String)"The spirit of Ares burns within,"), (Object)Component.literal((String)"Granting power in attack,"), (Object)Component.literal((String)"But demanding sacrifices."));
        spheres.add(SphereProvider.createSphere("[★] Ares Sphere", "0000000b-0000-000b-0000-000b0000000b", "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0Mzc3NDI1NSwKICAicHJvZmlsZUlkIiA6ICJhYWMxYjA2OWNkMjE0NWE2ODNlNzQxNzE4MDcxMGU4MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJqdXNhbXUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzE2YWRjNmJhZmNiNTdmZDcwN2RlZTdkZDZhNzM2ZmUxMjY3MTFkNTNhMWZkNmNlNzg5ZGE0MWIzYmUxM2YyYSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9", Defaultpricec.getPrice("Ares Sphere"), aresLore));
        List hydraLore = List.of(Component.literal((String)"The tenacity of dark depths"), (Object)Component.literal((String)"Protects the owner,"), (Object)Component.literal((String)"Granting strength in water."));
        spheres.add(SphereProvider.createSphere("[★] Hydra Sphere", "0000000b-0000-000b-0000-000b0000000b", "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODUzMjE4MywKICAicHJvZmlsZUlkIiA6ICI1OGZmZWI5NTMxNGQ0ODcwYTQwYjVjYjQyZDRlYTU5OCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTa2luREJuZXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2UzYzExOGQ2OTZkOTEwZTU0ZGUwMmNhNGQ4MDc1NDNmOWIxOGMwMDhjOTgzOGQyZmY2OTM3NzYyMmZiMWQzMiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9", Defaultpricec.getPrice("Hydra Sphere"), hydraLore));
        List icarLore = List.of(Component.literal((String)"Holds Icarus's will,"), (Object)Component.literal((String)"Turning risk into power,"), (Object)Component.literal((String)"And fury into strike."));
        spheres.add(SphereProvider.createSphere("[★] Icarus Sphere", "0000000b-0000-000b-0000-000b0000000b", "ewogICJ0aW1lc3RhbXAiIDogMTc1MDI3ODU4MjQ5MSwKICAicHJvZmlsZUlkIiA6ICJhZWNkODIxZTQyYzE0ZDJlOThmNTA1OTg1MWI5OWMzNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJSb2RyaVgyMDc1IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M2ODAzZTZkNTY2N2EyZDYxMDYyOGJjM2IzMmY4NjNjZGE0OTVjNDY1NjE2ZGU2NTVjYjMyOTkzM2I2MWFmNzciLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==", Defaultpricec.getPrice("Icarus Sphere"), icarLore));
        List eridaLore = List.of(Component.literal((String)"Eris's cold is eternal,"), (Object)Component.literal((String)"Brings luck in battle,"), (Object)Component.literal((String)"Strengthening spirit and body."));
        spheres.add(SphereProvider.createSphere("[★] Eris Sphere", "0000000b-0000-000b-0000-000b0000000b", "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM0Mzg2MTE4NywKICAicHJvZmlsZUlkIiA6ICJlZGUyYzdhMGFjNjM0MTNiYjA5ZDNmMGJlZTllYzhlYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVEZXZKYWRlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzZlNGUyZjEwNDdmM2VjNmU5ZTQ1OTE4NDczOWUzM2I3YzFmYzYzYWQ4MjAyYmRhYjlmMDI0NTA4YWRkMjNlNWIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==", Defaultpricec.getPrice("Eris Sphere"), eridaLore));
        List titanLore = List.of(Component.literal((String)"Titan's might is strong,"), (Object)Component.literal((String)"Grants the resistance of steel,"), (Object)Component.literal((String)"But weighs down the step."));
        spheres.add(SphereProvider.createSphere("[★] Titan Sphere", "0000000b-0000-000b-0000-000b0000000b", "ewogICJ0aW1lc3RhbXAiIDogMTc1MDM1NDQ1NTE5MiwKICAicHJvZmlsZUlkIiA6ICJkOTcwYzEzZTM4YWI0NzlhOTY1OGM1ZDQ1MjZkMTM0YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJDcmltcHlMYWNlODUxMjciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODFlOTY5ODQ1OGI3ODQxYzk2YWU0ZjI0ZWM4NGFlMDE3MjQxMDA2NDFjNTY0ZTJhN2IxODVmNDA2ZThlZDIzIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=", Defaultpricec.getPrice("Titan Sphere"), titanLore));
        List morozLore = List.of(Component.literal((String)"Permafrost shackles,"), (Object)Component.literal((String)"Granting the hardness of ice,"), (Object)Component.literal((String)"But robbing agility"));
        spheres.add(SphereProvider.createSphere("[\u2744] Frost Sphere", "0000000b-0000-000b-0000-000b0000000b", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjNmZDM4MTQxMDhkZDA0MmM4NzU1NWYwMjNkNTcwY2UyNmI4M2MwZTM1YjIxYTdiMTI4MWE3ZTA1NDVjZjllMCJ9fX0=", Defaultpricec.getPrice("Frost Sphere"), morozLore));
        return spheres;
    }

    private static AutoBuyableItem createSphere(String displayName, String headUuid, String texture, int price, List<Component> lore) {
        CompoundTag nbt = new CompoundTag();
        nbt.putBoolean("HideFlags", true);
        nbt.putBoolean("Unbreakable", true);
        CompoundTag skullOwner = new CompoundTag();
        UUID uuid = UUID.fromString(headUuid);
        int[] uuidArray = SphereProvider.uuidToIntArray(uuid);
        skullOwner.put("Id", new IntArrayTag(uuidArray));
        CompoundTag properties = new CompoundTag();
        ListTag textures = new ListTag();
        CompoundTag textureNbt = new CompoundTag();
        textureNbt.putString("Value", texture);
        textures.add(textureNbt);
        properties.put("textures", textures);
        skullOwner.put("Properties", properties);
        nbt.put("SkullOwner", skullOwner);
        return new CustomItem(displayName, nbt, Items.PLAYER_HEAD, price, null, lore);
    }

    private static int[] uuidToIntArray(UUID uuid) {
        long mostSig = uuid.getMostSignificantBits();
        long leastSig = uuid.getLeastSignificantBits();
        return new int[]{(int)(mostSig >> 32), (int)mostSig, (int)(leastSig >> 32), (int)leastSig};
    }
}

