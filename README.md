# redeye Build World

A small NeoForge 1.21.1 mod for the redeye modpack (built against NeoForge 21.1.265). It gives [World Switcher](https://modrinth.com/mod/4JiVuE1B) worlds one of the flat generators below when they are created with that seed. Each flat preset has bedrock at the bottom, the top of the floor at y=62, the `minecraft:plains` biome, and no structures, features (lakes, ores, trees) or starting mobs. Already generated chunks are left as they are. Any other seed, including existing build worlds on seed `20261021`, keeps normal World Switcher generation.

| Seed | Preset |
| --- | --- |
| `766337` | Checkerboard: white and light grey concrete, one-block squares |
| `766338` | Plains: grass block, 3 dirt, stone below |
| `766339` | Stone: smooth stone on top, stone below |
| `766340` | Ponder grid: white concrete with a light grey concrete line on every chunk border |
| `20261021` and any other seed | Normal World Switcher generator |

The checkerboard and the ponder grid are a painted flat generator (`redeye_buildworld:painted_flat`). Its codec stores `preset` (`checkerboard` or `ponder_grid`) with the flat settings, so a restart reads the same pattern back. Plains and stone use the vanilla flat generator, whose codec stores the layers.

Build: `./build.sh <Minecraft 1.21.1 Mojang-named jar> <worldswitcher-1.5.2.jar> <libraries dir>` (JDK 21).
