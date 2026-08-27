# Better Content Threads

Forge 1.20.1 mod providing the Better Content 52-card Threads catalogue and the optional integration API `com.bettercontent.threads.api.ThreadSignals`.

## Trigger contract

The bundled catalogue contains 52 exact identities: 45 live cards and seven intentionally future Fragility cards. Every live reveal and completion route declares a bounded `producer` and an `episode` correlation slot. There are exactly 96 route entries. Producers emit only completed, server-authoritative native actions:

```java
ThreadSignals.emit(player, "native_action", "bounded_value", episodeToken);
```

The reveal stores the episode token in lineage player state. A completion is accepted only for the same active card and exact token; tokens persist across ordinary reloads and clear on completion or generation transition. The legacy three-argument overload remains binary-compatible but cannot satisfy correlated catalogue routes.

No route may discover a card from login, elapsed play time, inventory presence, or a synthetic objective. Contextual signals are queued one at a time and the automatic notice never captures input. Version-pinned adapters consume native mechanic evidence: action-backed Create criteria, live PneumaticCraft pressure, a launch-ready Creating Space rocket, matching Ars spell save/effect signatures, Blood Magic altar and soul-network events, Goety soul events, Tinkers' actual tool/alloy outputs, and vanilla enchantment effects.

## Presentation contract

The live-game tease is an input-transparent, panel-free glyph lockup centered at the top third. It uses a small code glyph close to single-line white text with a black border, followed by the localized reader hotkey. The exact reveal copy is `You've revealed the card: %s`. Its 3.2-second clock and bounded aspect/archive particles pause whenever another screen is open.

Opening Threads is voluntary. Unread cards remain sealed until click or Space, then develop for 1.8 seconds through equal archive-structure, unique-pigment, and aspect-trace passes. Title, prose, doorway, and facsimile controls remain hidden until development completes; the first click during development only finishes the plate, and read state changes only at completion. The finished frame always uses the untouched full runtime image.
