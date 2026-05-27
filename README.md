# Tamekind

**A vanilla+ passive-mob AI overhaul for animals that feel aware of the world.**

Tamekind is the passive-side counterpart to hostile AI overhauls. Animals herd, flee intelligently, seek habitats, remember danger and trust, and create living-world behavior without breaking vanilla farms.

## What it does today

### Awareness
- **Threat scanning** with per-prey predator pairings (`tamekind:predators_of/<entity>`) and a flat fallback (`tamekind:predators`). Tamed/sitting wolves, foxes, cats and ocelots are not threats.
- **Trust-aware player fear**: sprinting players scare nearby animals; feeding them builds trust that reduces flee distance, hitting them tears trust down. Trust holds for half its window then decays linearly to zero.
- **Damage and explosions** register a danger position even when blast damage is zero. Danger spreads to nearby same-type herd-mates with a cooldown to avoid spam.

### Reactions
- **Alert / freeze / drift** state before panic. Animals freeze and stare at distant threats, then drift slowly away in the second half of the alert. Animals in `tamekind:freezers` (rabbit, chicken, parrot by default) freeze longer and skip the drift. Babies freeze for half as long.
- **Panic escape selection** scores candidates against distance from threat, water neighbors (drowning-shore avoidance), boxed-in dead ends, soft-avoid blocks (snow, crops, farmland), grazing surfaces, rain exposure, and at night, block-light brightness. Cliff drops are checked up to a configurable depth.
- **Babies** use reduced panic radius and slower panic speed.
- **Stampede knockback** scales with herd size and skips leashed, mounted, named, breeding, vehicled, and baby entities.
- **Protective parents**: when a baby of the same type is hit, nearby adults are flagged as guarding and re-direct their panic toward the baby instead of fleeing.

### Habitat and rhythm
- **Shelter** during rain or darkness. Scoring prefers tagged shelter blocks, comfort blocks underfoot, lit positions at night, and penalises snow/soft-avoid surfaces. Thunderstorms shorten the scan cooldown and add a 1.4× approach speed.
- **Graze / rest** at full LOD, biased toward grazing or comfort blocks; rest sessions last 3× longer at night.
- **Drink** at water-edge positions occasionally.
- **Home position** is set on graze completion. `HomeReturnGoal` drifts animals back when they wander past `homeReturnMaxDistance`. Babies inherit home from the adult they anchor to.

### Herd coordination
- **Herdable** animals are tagged via `tamekind:herdable`.
- **Leaders** are picked deterministically per herd. Leaders run expensive scans for shelter, graze and water targets and write the result to memory; followers piggyback within the same window instead of re-scanning. Followers also abort herd-follow if the leader has an active danger memory (let panic spread take over).

### Breeding and farm safety
- **Crowd control**: between `breedingCrowdSoftLimit` and `breedingCrowdHardLimit` same-type animals nearby, refusal probability ramps linearly. Hard limit is absolute.
- **Movement-goal skip** for leashed, mounted, ridden, in-love (breeding), name-tagged, vehicled animals, and any entity type in `tamekind:disabled` or the runtime disable set.

### Persistence
- Animal memory (danger, home, guard, all three shared positions, trust map, danger-spread cooldown) is round-tripped through NBT.

## Tags

Entity tags
- `tamekind:herdable`, `tamekind:predators`, `tamekind:predators_of/<namespace>/<path>`, `tamekind:disabled`, `tamekind:freezers`

Block tags
- `tamekind:grazing_blocks`, `tamekind:shelter_blocks`, `tamekind:comfort_blocks`, `tamekind:water_blocks`, `tamekind:avoid_blocks`, `tamekind:soft_avoid_blocks`

All tags are `replace: false`, so datapacks and modpacks can append modded entries.

## Commands (`/tamekind ...`)

- `animal` — report the nearest animal's LOD, herd info, danger, trust toward the caller, home, guarding flag, leader
- `dump` — multi-line state dump of the nearest animal
- `list` — count animals within 64 blocks broken down by LOD
- `leader` — show the nearest animal's herd leader and current shared shelter
- `trust` — show the nearest animal's trust score toward the calling player
- `home set` / `home clear` — manage the nearest animal's home position
- `forget` — wipe danger/home/guard/shared positions for the nearest animal
- `disable <minecraft:type>` — toggle a runtime-only disable for an entity type (complements `tamekind:disabled` tag)
- `config` — print active config snapshot
- `profile` / `profile <vanilla+|realism|simulation>` — show or apply a config profile
- `reload` — re-read `config/tamekind.properties` from disk

## Config profiles

- **vanilla+** — quiet, tight ranges, no stampede or daily rhythm, longer LOD cache
- **realism** — defaults
- **simulation** — wider ranges, shorter LOD cache, all features on

Per-field overrides live in `config/tamekind.properties` (auto-generated on first run).

## Design pillars

- **Performance first.** AI LOD (`FULL` near players, `SIMPLE` mid-range, `SLEEP` far) gates expensive goals. Per-animal LOD cache avoids repeated player-distance scans.
- **Shared herd decisions.** Leaders pay for scans; followers reuse the result. Same idea for danger memory: one event, broadcast to the herd with cooldown.
- **Data-driven compatibility.** Behavior is steered by tags. Modded animals join the system by being added to `herdable`, `predators`, or the various block tags.
- **Vanilla farms still work.** Multiple opt-outs (leash, mount, name tag, breeding, vehicle, tag, runtime list) keep pens and farms predictable.

## Planned

### Behavior
- **Fire / lava proximity danger** — being within N blocks of an open flame or lava registers a danger memory even without taking damage.
- **Biome comfort tags** — `tamekind:comfortable_in/<biome>` per species. In their comfort biome animals graze longer and panic less; in hostile biomes they actively try to leave.
- **Shade-seeking on hot clear days** — animals tagged `tamekind:heat_sensitive` (desert dwellers etc.) treat exposed midday daylight like rain and look for shade. Mirrors the storm-shelter goal with different triggers.
- **Stampede crop damage** — when a panicking herd crosses farmland, trample 1–2 crops per second (configurable, default **off** so vanilla farms are safe).
- **Sound cues** *(default on)* — short alarm bleat / hoof stomp on the Alert → Panic transition so the player notices the cascade.
- **Lost babies** — a baby separated from its herd for too long bleats and stops moving (temporarily joins `freezers` behavior) so the player can find and return it.
- **Hibernation tier** — at extreme cold/heat, some species idle below SLEEP LOD. Performance win for big modded biomes.
- **Mother-calf bonding** — adult periodically slow-walks to and looks at its calf (low-priority goal). Pure visual.
- **Grazing actually eats** — while on `grazing_blocks` during a graze, low chance per second to convert grass → dirt (vanilla sheep behavior, extended to cows, horses, sheep-likes).
- **Wallowing pigs** — pigs on mud or water-edge blocks roll briefly with particles and slowed movement.
- **Group sleep cluster** — at night, herd-mates path toward each other within a small radius before settling — visible huddle.
- **Crouch-stalk** — tagged predators (fox, wolf) crouch when within stalking range of prey.
- **Limp when low-HP** — under N% health, max speed reduced and panic prefers shelter over distance.
- **Nesting for chickens** — chickens lay only adjacent to `tamekind:nest_blocks` (hay/leaves by default).
- **Mating displays** — rams/goats head-butt each other when same-sex breeding-ready adults are within 4 blocks.
- **Seasonal automatic breeding** — natural / unattended breeding only occurs in a configurable season window (paired with Serene Seasons when present). Player-driven feeding-to-breed is unaffected — farms keep working year-round.

### Pets and mounts
- **Mount loyalty extension** — feeding a tamed horse/donkey/llama extends trust well past the wild duration; high-loyalty mounts refuse to wander when unmounted.
- **Pet danger relay** — tamed wolves/cats/parrots near their owner inherit the owner's nearby animals' danger memory and look toward the threat (no aggression added).
- **Mount-aware herd** — a ridden leader doesn't broadcast shared shelter/graze targets; the rider is in control.
- **Follow-without-leash** — animals with high trust toward a nearby player slow-follow them at a comfortable distance.
- **Calmer breeding** — feeding from a trusted player has a higher chance of triggering love mode and shorter post-breed cooldown.
- **Mount obedience** — high-trust mounts have faster acceleration and tighter turning response for their trusted rider.
- **Hit forgiveness** — a trusted player's accidental hit drains trust but does not trigger panic or danger memory.

### Integrations
- **Serene Seasons** — read season state and bias behavior:
  - *Winter*: stronger shelter-seek, slower graze, comfort-block bias.
  - *Summer*: enable shade-seeking for heat-sensitive species.
  - *Autumn*: babies cluster tighter; parent-guard window extended.
  - *Spring*: looser breeding crowd thresholds; longer graze.
- **Warband (hostile companion mod)** — raids/alerts pull pasture animals into shelter; raid-flagged hostiles spread danger further.
- **Hearthfolk (villager companion mod)** — villagers known to the village boost nearby animals' trust similar to the owner-player.

### Tooling
- **Predator AI patches** — small mixins so hostile wolves and foxes actually hunt entries in `predators_of/<prey>`, turning the tag into a two-way contract.
- **`/tamekind trust map <player>`** — admin tool listing trust footprint across a chunk.
- **Habitat scoring helper** — consolidate the duplicated light/comfort/soft-avoid scoring across `HabitatShelterGoal`, `GrazeRestGoal`, `DrinkGoal`, and `PanicGoal`.
- **Datagen for default tags** instead of hand-written JSON.

## Requirements

- Fabric Loader 0.19.2+
- Fabric API
- Minecraft 26.1.x
- Java 25+

## License

MIT.
