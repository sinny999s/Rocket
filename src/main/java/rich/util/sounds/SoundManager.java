
package rich.util.sounds;

import lombok.Generated;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import rich.IMinecraft;
import rich.util.string.PlayerInteractionHelper;

public final class SoundManager
implements IMinecraft {
    public static SoundEvent KOLOKOLNIA_KILL = SoundEvent.createVariableRangeEvent((Identifier)Identifier.parse((String)"rich:kolokolnia_kill"));
    public static SoundEvent MOAN1 = SoundEvent.createVariableRangeEvent((Identifier)Identifier.parse((String)"rich:moan1"));
    public static SoundEvent MOAN2 = SoundEvent.createVariableRangeEvent((Identifier)Identifier.parse((String)"rich:moan2"));
    public static SoundEvent MOAN3 = SoundEvent.createVariableRangeEvent((Identifier)Identifier.parse((String)"rich:moan3"));
    public static SoundEvent MOAN4 = SoundEvent.createVariableRangeEvent((Identifier)Identifier.parse((String)"rich:moan4"));
    public static SoundEvent MODULE_DISABLE = SoundEvent.createVariableRangeEvent((Identifier)Identifier.parse((String)"rich:module_disable"));
    public static SoundEvent MODULE_ENABLE = SoundEvent.createVariableRangeEvent((Identifier)Identifier.parse((String)"rich:module_enable"));
    public static SoundEvent OFF = SoundEvent.createVariableRangeEvent((Identifier)Identifier.parse((String)"rich:off"));
    public static SoundEvent ON = SoundEvent.createVariableRangeEvent((Identifier)Identifier.parse((String)"rich:on"));
    public static SoundEvent CRIME = SoundEvent.createVariableRangeEvent((Identifier)Identifier.parse((String)"rich:crime"));
    public static SoundEvent METALLIC = SoundEvent.createVariableRangeEvent((Identifier)Identifier.parse((String)"rich:metallic"));
    public static SoundEvent WELCOME = SoundEvent.createVariableRangeEvent((Identifier)Identifier.parse((String)"rich:welcome"));

    public static void init() {
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)KOLOKOLNIA_KILL.location(), (Object)((Object)KOLOKOLNIA_KILL));
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)MOAN1.location(), (Object)((Object)MOAN1));
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)MOAN2.location(), (Object)((Object)MOAN2));
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)MOAN3.location(), (Object)((Object)MOAN3));
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)MOAN4.location(), (Object)((Object)MOAN4));
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)MODULE_DISABLE.location(), (Object)((Object)MODULE_DISABLE));
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)MODULE_ENABLE.location(), (Object)((Object)MODULE_ENABLE));
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)OFF.location(), (Object)((Object)OFF));
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)ON.location(), (Object)((Object)ON));
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)CRIME.location(), (Object)((Object)CRIME));
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)METALLIC.location(), (Object)((Object)METALLIC));
        Registry.register((Registry)BuiltInRegistries.SOUND_EVENT, (Identifier)WELCOME.location(), (Object)((Object)WELCOME));
    }

    public static void playSound(SoundEvent sound) {
        SoundManager.playSound(sound, 1.0f, 1.0f);
    }

    public static void playSound(SoundEvent sound, float volume, float pitch) {
        if (!PlayerInteractionHelper.nullCheck()) {
            SoundManager.mc.level.playSound(SoundManager.mc.player, SoundManager.mc.player.blockPosition(), sound, SoundSource.BLOCKS, volume, pitch);
        }
    }

    public static void playSoundDirect(SoundEvent sound, float volume, float pitch) {
        mc.getSoundManager().play(SimpleSoundInstance.forUI((SoundEvent)sound, (float)pitch, (float)volume));
    }

    @Generated
    private SoundManager() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}

