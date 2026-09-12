# Better Content Threads

Forge 1.20.1 mod providing the Better Content 52-card Threads catalogue and the optional integration API `com.bettercontent.threads.api.ThreadSignals`.

## Trigger contract

The bundled `bc.threads.v3` catalogue contains 52 active identities. Every card declares a stable concept ID, authoritative owner, concise mechanical rule, bounded reveal/completion producer, and an `episode` correlation slot. There are exactly 108 route entries. Producers emit only completed, server-authoritative native actions:

```java
ThreadSignals.emit(player, "native_action", "bounded_value", episodeToken);
```

The reveal stores the episode token in lineage player state. A completion is accepted only for the same active card and exact token; tokens persist across ordinary reloads and clear on completion or generation transition. Single-player saves use World Lifecycle Manager's durable bound lineage store, allowing a future successor to inherit card history without combining unrelated saves. Player-state schema 4 is an intentional clean break from earlier experimental catalogue state. The legacy three-argument overload remains binary-compatible but cannot satisfy correlated catalogue routes.

No route may discover a card from login, elapsed play time, inventory presence, or a synthetic objective. Contextual signals are queued one at a time and the automatic notice never captures input. Version-pinned adapters consume native mechanic evidence: action-backed Create criteria, live PneumaticCraft pressure, a launch-ready Creating Space rocket, matching Ars spell save/effect signatures, Blood Magic altar and soul-network events, Goety soul events, Tinkers' actual tool/alloy outputs, and vanilla enchantment effects.

## Presentation contract

The live-game tease is an input-transparent, panel-free glyph lockup centered at the top third. It uses a small code glyph close to single-line white text with a black border, followed by the localized reader hotkey. The exact reveal copy is `Card unlocked: %s`. Its 3.2-second clock and bounded aspect/archive particles pause whenever another screen is open.

Opening Threads is voluntary. The default conflict-aware binding is `M`, rendered in a keycap beside the literal `Threads` label. The reader opens on the first unread plate, keeps the canonical suit order, marks every unread row with an explicit `OPEN` badge, and shows unread counts on the suit tabs. Unread cards remain sealed until click or Space, then the illustration crossfades over the neutral archive plate for 800 ms. Full 256×384 illustrations scale into their 2:3 frames, and details contain a descriptive title, mechanical rule, and practical next action, with no prose or invitation layer. Complete text wraps and scrolls with the wheel, Up/Down, and Page Up/Down; Left/Right change cards. Narrow catalogues use one column and expose a keyboard navigation hint. Retained art changes only at exact aspect-trace pixels; seven former sealed positions have new illustrations and normal card behavior.

Top-level world and server joins provide an opaque learning surface with an explicit heading, a wide spawn-generation progress bar, and a manual sixteen-lesson survival curriculum. Each 512×256 lesson illustration becomes the largest centered 2:1 background that fits the viewport without cropping; the remaining space is dark letterboxing. Parchment body text, muted categories, and gold action copy sit on a dark bottom caption sized to the actual wrapped text while the heading, progress, caption, and controls use the screen's full height. Previous/Next buttons and arrow keys move through lessons about the fading HUD, hydration, nutrition, food variety, metabolism, temperature, movement, sleep, revival, life stats, seasons, storms, pollution, structural physics, pillager campaigns, and Threads itself. Lessons resume at the next unseen entry across sessions; only eight seconds of rendered exposure records a loading lesson as seen. Fast joins and loading stalls leave it available for another attempt.

Loading lessons do not delay world generation. Every initial join carries the exact lesson into a paused World Ready plate once loading finishes; the player enters with the Begin control or Enter, Space, or Escape. Disconnects do not consume the episode, and dimension transitions do not create new episodes.

The Threads reader includes all sixteen spoiler-free lessons as a permanent reference alongside, but distinct from, the 52-card archive. Each `bc.loading_briefs.v3` lesson declares a stable concept, authoritative owner, and optional related Thread. Known related cards remain directly accessible without leaking unknown teaching copy. Native controls appear only for installed EMI/Ponder item targets or exact bound nutrition, RPG, and Trace Sight keys. Unsupported declared guide/Font/power/campaign/lifecycle targets stay hidden; the reader never closes merely to print a resource ID. Optional EMI/Ponder API classes are isolated so absence is safe. Movement lessons resolve current ParCool keys at render time.

The network uses protocol 10, adding bounded death context to the card protocol that removed prose and invitation fields. Client and server must use matching versions. Player-state schema 4, stable card IDs, route predicates, and the public `ThreadSignals` API are unchanged. Rules and lesson bodies have maximum lengths but no minimum word count; short explanations do not need padding.

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

## Death and pause-menu tips

The native death screen and Esc menu show separate, stable tips from one client catalogue:
`assets/better_content_threads/death_hints/catalogue.json`. Its 192 entries include 80 survival,
88 whole-pack discovery, and 24 light late-game teasers. Each `bc.death_hints.v1` entry has a stable
ID, shared concept ID, pool, text, context categories, required mod IDs, and mechanical sources.
Sources are workspace-relative paths or Minecraft 1.20.1 class references; they are authoring
provenance and never appear in the player UI. Resource packs may replace the catalogue.

Both surfaces share a persistent cycle of displayed IDs. Every eligible tip must be shown before
another cycle starts; exhausted contextual categories yield to unseen tips elsewhere. Recognized
categories prefer contextual advice on three of four eligible death selections. General selections
weight teasers at one in eight while both pools have unseen entries. Tips require every named mod.
At cycle boundaries, avoid the last death and Esc tips when alternatives exist.

The Esc tip is chosen on first use and remains stable through reopening, resizing, respawning,
reconnecting, and restarting. A confirmed death clears that choice once; the next Esc menu chooses
its replacement. The death screen has its own selection. Only actually rendered tips enter history;
a hidden selection reserves its ID without consuming it or forcing a new cycle. History schema 2
in `config/better-content-threads-death-hints.json` stores the cycle and current Esc tip atomically.
Schema 1 migrates its known recent IDs into the first cycle. This client-wide history is independent
of loading exposure, worlds, and player progression. Packet protocol remains 10.

The server classifies registered damage types/tags and attackers before sending the native death
screen packet. Revival's downed event retains the original category for bleed-out, giving up, or
finishing; revival, logout, respawn, and server shutdown clear episode memory. Missing context uses
general advice. Late context cannot replace the first rendered selection. No death-message text
is parsed and no Thread signals are emitted by hint presentation.

Hints wrap at normal font size and measure actual controls. The Esc menu moves its button group
up only as needed, retaining full-size controls and a top-right Threads doorway. The built-in pool
fits a 320×240 GUI; smaller or oversized resource-pack layouts omit tips that cannot fit without
recording them. Resizing never rerolls a selection. Normal and hardcore death controls and their
delays remain native. The label is translatable, and control tokens resolve current bindings.

`DeathHintsTest` and `HintLifecycleTest` cover content, shared cycles, persistence/migration,
eligibility, death/respawn boundaries, context, packet bounds, and layout. Visual fixtures render
production hints with native death geometry and single/multiplayer pause geometry without a
player/world; these are presentation checks, not end-to-end connection or respawn tests. Set
`-Dbc.learningVisual.deathHintsOnly=true` in `JAVA_TOOL_OPTIONS` for 15 death-hint frames, or
`-Dbc.learningVisual.tipsOnly=true` for 21 pause frames including the native PauseScreen and a
real-font fit check of all 192 tips. Use `-PlearningVisualHeight=960` to inspect GUI scales 4/3/2;
720 pixels checks the common minimum-height layout, where Minecraft may clamp scale 4 to 3.
Automated checks never inject mouse input.
