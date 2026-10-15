# AKvisuals

Client-side Fabric mod for Minecraft 1.21.11. Kill aura, a spinning "china hat" over other players, screen stretch, and an in-game menu.

## Features

- **Kill aura**: attacks the nearest valid target in range. Players and mobs can be toggled independently. Rotation is client-side only.
- **China hat**: a rainbow wireframe cone over other players' heads. Animated, with adjustable speed.
- **Screen stretch**: OFF, 4:3, 16:9, or a custom W:H ratio.
- **Menu**: opened with RIGHT SHIFT (rebindable under Options > Controls > AKvisuals). Tabs for COMBAT, VISUALS, and CONFIG. Settings save to `config/akvisuals.json`.

## Requirements

- Minecraft 1.21.11
- Fabric Loader 0.16.0 or newer
- Fabric API
- Java 21

## Build

```bash
./gradlew build
```

If `gradlew` is missing the wrapper jar, generate it first (any Gradle 8.14+/9.x):

```bash
gradle wrapper --gradle-version 9.2.1
```

The jar is written to `build/libs/AKvisuals-1.21.11.jar`. Put it in `mods/` alongside Fabric API.

## Status

Build setup updated (Loom 1.14.10 + Gradle 9.2.1). Some targets still marked with `// FILL:` comments — verify mixin `getBasicProjectionMatrix` and tick-counter accessor on first run.

## Warning

Kill aura on any server with anti-cheat such as Grim or Vulcan will get your account banned. Test on a private server or single-player world.

## License

MIT, see [LICENSE](LICENSE).
