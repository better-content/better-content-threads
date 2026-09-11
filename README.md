# Better Content Threads

Forge 1.20.1 mod providing the Better Content 52-card Threads catalogue and the optional integration API `com.bettercontent.threads.api.ThreadSignals`.

## Trigger contract

The bundled `bc.threads.v2` catalogue contains 52 active identities. Every card declares a stable concept ID, authoritative owner, concise mechanical rule, bounded reveal/completion producer, and an `episode` correlation slot. There are exactly 108 route entries. Producers emit only completed, server-authoritative native actions:

```java
ThreadSignals.emit(player, "native_action", "bounded_value", episodeToken);
```

The reveal stores the episode token in lineage player state. A completion is accepted only for the same active card and exact token; tokens persist across ordinary reloads and clear on completion or generation transition. Single-player saves use World Lifecycle Manager's durable bound lineage store, allowing a future successor to inherit card history without combining unrelated saves. Player-state schema 4 is an intentional clean break from earlier experimental catalogue state. The legacy three-argument overload remains binary-compatible but cannot satisfy correlated catalogue routes.

No route may discover a card from login, elapsed play time, inventory presence, or a synthetic objective. Contextual signals are queued one at a time and the automatic notice never captures input. Version-pinned adapters consume native mechanic evidence: action-backed Create criteria, live PneumaticCraft pressure, a launch-ready Creating Space rocket, matching Ars spell save/effect signatures, Blood Magic altar and soul-network events, Goety soul events, Tinkers' actual tool/alloy outputs, and vanilla enchantment effects.

## Presentation contract

The live-game tease is an input-transparent, panel-free glyph lockup centered at the top third. It uses a small code glyph close to single-line white text with a black border, followed by the localized reader hotkey. The exact reveal copy is `You've revealed the card: %s`. Its 3.2-second clock and bounded aspect/archive particles pause whenever another screen is open.

Opening Threads is voluntary. The default conflict-aware binding is `M`, rendered in a keycap beside the literal `Threads` label. The reader opens on the first unread plate, keeps the canonical suit order, marks every unread row with an explicit `REVEAL` badge, and shows unread counts on the suit tabs. Unread cards remain sealed until click or Space, then the illustration crossfades over the neutral archive plate for 800 ms. Full 256×384 illustrations scale into their 2:3 frames, and details present the rule and next action before lore. Complete text wraps and scrolls with the wheel, Up/Down, and Page Up/Down; Left/Right change cards. Narrow catalogues use one column and expose a keyboard navigation hint. Retained art changes only at exact aspect-trace pixels; seven former sealed positions have new illustrations and normal card behavior.

Top-level world and server joins provide an opaque learning surface with an explicit heading, a wide spawn-generation progress bar, and a manual sixteen-lesson survival curriculum. Each 512×256 lesson illustration becomes the largest centered 2:1 background that fits the viewport without cropping; the remaining space is dark letterboxing. Parchment body text, muted categories, and gold action copy sit on a dark bottom caption sized to the actual wrapped text while the heading, progress, caption, and controls use the screen's full height. Previous/Next buttons and arrow keys move through lessons about the fading HUD, hydration, nutrition, food variety, metabolism, temperature, movement, sleep, revival, life stats, seasons, storms, pollution, structural physics, pillager campaigns, and Threads itself. Lessons resume at the next unseen entry across sessions; only eight seconds of rendered exposure records a loading lesson as seen. Fast joins and loading stalls leave it available for another attempt.

Loading lessons never impose mandatory onboarding. The world opens immediately when ready unless the player explicitly selects Keep Reading; that choice carries the exact lesson into a voluntary paused plate with an Enter World control. Disconnects do not consume the episode, and dimension transitions do not create new episodes.

The Threads reader includes all sixteen spoiler-free lessons as a permanent reference alongside, but distinct from, the 52-card archive. Each `bc.loading_briefs.v3` lesson declares a stable concept, authoritative owner, and optional related Thread. Known related cards remain directly accessible without leaking unknown teaching copy. Native controls appear only for installed EMI/Ponder item targets or exact bound nutrition, RPG, and Trace Sight keys. Unsupported declared guide/Font/power/campaign/lifecycle targets stay hidden; the reader never closes merely to print a resource ID. Optional EMI/Ponder API classes are isolated so absence is safe. Movement lessons resolve current ParCool keys at render time.

## Verification

Run `./gradlew verifyFull stageRuntimeJar` before committing. The full lane includes four
server GameTests against the loaded production catalogue: correlated reveal/completion,
rejection of another episode, duplicate signal delivery, and persisted player NBT reload
after clearing the in-memory cache. These use Forge server players without a connected
client; they do not verify rendering or the optional World Lifecycle Manager disk store.

`gametest/profiles/full.txt` lists the required runtime test IDs. Each invocation retains
an isolated fixture under `build/gametest/<run-token>/`, including `execution.json`,
world files, and logs. Verification requires the current token, a finished report, and
exactly the expected discovered and successfully executed tests. Failed fixtures remain
available until explicitly removed; `clean` removes build artifacts. The fast lane also
checks rejection of missing, incomplete, stale, malformed, duplicate, or failed evidence.

For local visual review, `./gradlew runLearningVisual` captures the production reader,
lesson library, loading captions, generation and arrival controls at GUI scales 4, 3,
and 2 in `build/learning-visual/screenshots/`. The `learningVisual` source set is never
included in the runtime JAR. It needs a display, opens no world, and sends no pointer
input. Inspect the captures manually; this fixture does not verify pack integration,
actual gameplay triggers, or native guides supplied by other mods.
