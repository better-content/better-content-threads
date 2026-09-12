# Better Content Threads

Forge 1.20.1 contextual teaching: 52 cards explain what just happened, why it happened,
and a useful next step. Threads is an optional downstream consumer of gameplay events.
Providers never depend on Threads.

## Definitions and artwork

[authoring/discoveries.json](authoring/discoveries.json) is the editable card roster and
scene specification. Its 52 entries generate the bundled `bc.threads.v4` catalogue at
`src/main/resources/data/better_content_threads/threads/catalogue.json`. Seven topics
organize the discoveries: World, Body, Materials, Industry, Magic, Travel, and Lineage.
Each definition has one event/cause/action explanation and bounded discovery routes.
There are no gameplay reveal/completion stages, unknown slots, hourly limits, or quota scheduler.

[Art grammar](authoring/art-grammar.txt) requires concrete engraved mechanisms and
consequences. All card and loading art excludes humans, humanoids, body parts, humanlike
spirits, silhouettes, and mannequins. Nonhumanoid creatures may appear where the scene
requires them. Images contain no generated text, frames, or UI. Code owns typography.

Reviewed image-generation masters and exact prompts live in the external review bundle
`/home/dev/workspace_artifacts/reviews/better-content-threads/20260912-discovery-redesign/`.
Only deterministic derivatives ship: full-color 256×384 cards, grayscale thumbnails,
cosmetic item textures, and 512×256 loading illustrations. Rebuild them with:

```sh
python3 authoring/prepare_art.py /absolute/path/to/review-bundle
```

The model predicate uses each card's global order, 1 through 52. It is independent of topic.

## Committed evidence and persistence

Native providers publish typed events after actual effects. Version-pinned third-party
mixins observe completed output, movement, or work. Possession, proximity, inspection,
recipe previews, and setup alone cannot substitute for the outcome.

```java
ThreadSignals.emit(player, "native_action", "bounded_value", operationToken, context);
ThreadSignals.emit(server, ownerUuid, "native_action", "bounded_value", operationToken, context);
```

The server/UUID overload credits an owned operation at success even while its owner is
away or offline. Notices persist until reconnect. Ownership follows a submitted job,
configured apparatus, planted crop, or native operator; it never chooses the nearest player.
A successful event immediately discovers the whole explanation. Repeated delivery and
repeat encounters count at most once per card per generation.

World Lifecycle Manager stores history with the lineage. A server-world SavedData fallback
also preserves offline discoveries without relying on death-cloned player NBT. The archive
tracks unique lifetime discoveries and each card's generation count; the current score is
this generation's distinct discoveries. A successor starts a new score while preserving
known/read art. Ordinary death does not change generation. The World Condenser successor
card remains; integrated single-player Condenser lifecycle support is a separate future task.
Every card can be earned without another human.

## Reader and teaching

The default reader key is M, with the actual binding shown in notices. Opening selects and
automatically develops the oldest unread card. All plus seven topic filters show discovered
cards only, unread first. Continue or Space after reading selects and develops the next unread
card across filters. A press that finishes the short art animation is consumed; it cannot skip
that card's explanation. There is no timed advance. Text wraps and scrolls at narrow widths.

Notices are input-transparent and queue legitimate bursts. Their reading clock pauses behind
screens and urgent injury care. Optional native doorways open real supported interfaces;
Body uses the provider's overview request. Facsimiles are freely issued cosmetic copies and
grant no progression to recipients.

Seventeen loading lessons also remain available through Lessons. They teach fundamentals
before an event; cards explain an experienced result. Loading never delays world generation,
and World Ready waits for explicit Begin. Main-menu, Esc, and death tips have independent
selection and exposure histories. See [teaching surfaces](authoring/teaching-surfaces.md)
for current context priorities, injury integration, and compact death-screen behavior.

Network protocol 11 requires matching client/server versions. The new state is an intentional
clean break; no old card migration or compatibility identities are supplied.

## Verification

Run `./gradlew verifyFull stageRuntimeJar` before committing or pushing. The full lane includes
four native GameTests against the production catalogue: immediate discovery, rejection of
setup/invalid evidence, duplicate delivery, and offline persistence after reload. Unit tests
cover lineage scores, unread traversal, packets, teaching selection, chemical ownership, and
pinned mixin target descriptors and invocation anchors. Native producers have their own
repository-local outcome tests and staged runtime JARs.

Each native GameTest run retains its token, reports and isolated fixture under `build/gametest/`.
A failed or incomplete report fails verification. Preserve failed fixtures for diagnosis.

`./gradlew runLearningVisual` runs the isolated real-client review fixture; it is excluded from
the runtime JAR. Journal-only and combined-death modes exercise compact/wide production screens
without opening a world. Screenshots require manual inspection. These fixtures do not claim
full-pack compatibility or runtime exercise of every optional integration. Pack deployment,
pack suites, and fresh distributions require a separate explicit instruction.
