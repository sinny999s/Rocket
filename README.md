# Rocket Client

A modern Minecraft 1.21.11 Fabric utility client with a clean UI, custom HUD, and 80+ modules.

> **Note:** This is the last open-source release of Rocket Client. Future versions will be closed-source.

## Features

### Client
- **Custom Main Menu** — modern dark theme with blur, animations, and account manager
- **Custom ClickGUI** — searchable module panel with category tabs, configs, and smooth animations
- **Custom HUD** — replaceable hotbar, health/hunger/armor/XP bars, draggable elements
- **Custom Chat** — modern floating chat input bar with blur background and custom fonts
- **Custom Font Renderer** — MSDF-based font rendering with multiple font families

### Combat
Aura, CrystalAura, Surround, AutoCrystal, AutoTotem, AutoGApple, Criticals, Velocity, TriggerBot, BowSpammer, HitBox, HitSound, MaceTarget, ProjectileHelper, AntiBot, NoFriendDamage, ShiftTap, TapeMouse, AutoSwap, NoInteract

### Movement
AutoSprint, Speed, Fly, Jesus, LongJump, NoSlow, NoWeb, Spider, Step, ReverseStep, ElytraMotion, ElytraTarget, InventoryMove, Strafe, TargetStrafe, WaterSpeed, SuperFireWork

### Player
AutoTool, AutoEat, AutoPotion, AutoRespawn, AutoPilot, ChestStealer, FreeCam, FreeLook, ItemScroller, NameProtect, NoDelay, NoEntityTrace, NoFallDamage, NoPush

### Render
ESP (Glow/Corner/3D Box), StorageESP, BlockESP, TargetESP, Ambience, Arrows, BetterTooltips (shulker preview grid), BlockOverlay, CameraSettings, ChinaHat, ChunkAnimator, FullBright, GlassHands, HitEffect, ItemPhysic, JumpCircle, NoRender, Particles, SeeInvisible, SwingAnimation, ViewModel, Waypoints, WorldParticles

### World
Scaffold (Normal + GodBridge), PacketMine

### Misc
AutoBuy, AutoDuel, AutoLeave, AutoTpAccept, BetterChat, ClickFriend, ClickPearl, ClientSounds, ElytraHelper, RegionExploit, ServerHelper, ServerRPSpoofer, WindJump

## Building

**Requirements:** Java 21, Gradle

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/`.

## Tech Stack

- **Minecraft:** 1.21.11
- **Mod Loader:** Fabric (Loader 0.18.4)
- **Mappings:** Mojang (Official)
- **Loom:** Fabric Loom 1.15
- **Language:** Java 21

## License

This project is licensed under the [GNU Affero General Public License v3.0](LICENSE).

## Discord

Join the community: [https://discord.gg/W5XVwYY7GQ](https://discord.gg/W5XVwYY7GQ)
