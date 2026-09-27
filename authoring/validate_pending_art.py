#!/usr/bin/env python3
"""Validate the staged 52-card teaching roster without exporting missing art."""
import json
import re
from pathlib import Path

repo = Path(__file__).resolve().parent.parent
current = json.loads((repo / "authoring/discoveries.json").read_text())["cards"]
pending = json.loads((repo / "authoring/discoveries.pending-art.json").read_text())["cards"]
old_ids = {card["id"] for card in current}
new_ids = {card["id"] for card in pending}
expected_cuts = {
    "living_tree_fall", "surveyed_deposit", "road_destination", "persistent_injuries",
    "trauma_amplification", "self_treatment", "life_allocation_lost", "geology_sifting",
    "ore_rinsing", "pressure_refining", "autocrafting_finished", "programmed_machine",
    "wireless_delivery", "elemental_spell", "blood_altar_result", "occult_ritual",
    "soul_powered_spell", "spirit_ritual", "magical_engine", "campaign_arrival",
    "hostile_crossfire", "schematic_substitution",
}
expected_additions = {
    "find_recipes_in_emi", "first_hand_axe", "start_tinkers_tools", "find_dimensional_font",
    "ore_chunks_need_separation", "choose_return_point", "underground_pressure_builds",
    "fruit_fuels_a_sprint", "vegetables_buffer_exposure", "dry_food_for_journey",
    "boats_break_into_parts", "hotstone_dangerous_ground", "maintain_tinkers_tool",
    "lit_furnace_pollutes", "alloy_in_smeltery", "machine_blocks_open_roots",
    "plan_sustained_rotation", "font_trip_time_budget", "bring_planting_stock_home",
    "ratlantis_starts_logistics", "ratlantis_below_islands", "spirits_are_materials",
}

assert len(current) == len(pending) == 52
assert len(old_ids) == len(new_ids) == 52
assert len(new_ids - old_ids) == len(old_ids - new_ids) == 22
assert old_ids - new_ids == expected_cuts, old_ids - new_ids
assert new_ids - old_ids == expected_additions, new_ids - old_ids
assert sorted(card["order"] for card in pending) == list(range(1, 53))
assert {card["topic"] for card in pending} == {"world", "body", "materials", "industry", "magic", "travel", "lineage"}

for card in pending:
    assert re.fullmatch(r"[a-z0-9_]{1,48}", card["id"]), card["id"]
    assert re.fullmatch(r"[a-z0-9_.]{3,80}", card["concept_id"]), card["id"]
    assert 1 <= len(card["title"]) <= 64, card["id"]
    assert all(0 < len(card[field]) <= 512 for field in ("event", "cause", "action")), card["id"]
    assert card["art"] == f"better_content_threads:textures/gui/threads/{card['id']}.png", card["id"]
    assert card["scene"].strip() and card["evidence"]["adapter_sources"], card["id"]
    assert card["discovery_routes"], card["id"]
    for route in card["discovery_routes"]:
        assert re.fullmatch(r"[a-z0-9_]{1,32}", route["type"]), card["id"]
        assert route["value"] and len(route["value"]) <= 160, card["id"]
    if card["id"] in new_ids - old_ids:
        texture = repo / "src/main/resources/assets/better_content_threads/textures/gui/threads" / f"{card['id']}.png"
        assert not texture.exists(), f"Pending art unexpectedly exists for {card['id']}; review before promotion"

print("Pending roster valid: 52 cards, 22 replacements, 22 art masters still pending")
