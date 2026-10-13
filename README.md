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

```
./gradlew build
```

The jar is written to `build/libs/AKvisuals-1.0.0.jar`. Put it in `mods/` alongside Fabric API.

## Status

Not yet compiled. The Yarn, Loader, and Fabric API versions in `gradle.properties` are placeholders, and the mixin target `getBasicProjectionMatrix` is unverified. Expect to fix these on the first build. Look for `// FILL:` comments.

## Warning

Kill aura on any server with anti-cheat such as Grim or Vulcan will get your account banned. Test on a private server or single-player world.

## License

MIT, see [LICENSE](LICENSE).
