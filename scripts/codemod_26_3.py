#!/usr/bin/env python3
"""Idempotent mechanical renames for the 1.21.11 (mojmap) -> 26.3 port.
Run from the repo root: python scripts/codemod_26_3.py
Each rule was verified with javap against the 26.3 merged jar / fabric-api 0.167.0+26.3."""
import pathlib, re

RULES = [
    # loot number providers split into .floats / .ints and return Holders (26.1 loot type unrolling)
    (r"import net\.minecraft\.world\.level\.storage\.loot\.providers\.number\.ConstantValue;",
     "import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;"),
    (r"\bConstantValue\.exactly\(", "ContextIntProviders.exactly("),
    # fabric-networking-api-v1 6.x: playC2S/playS2C -> serverboundPlay/clientboundPlay
    (r"PayloadTypeRegistry\.playC2S\(\)", "PayloadTypeRegistry.serverboundPlay()"),
    (r"PayloadTypeRegistry\.playS2C\(\)", "PayloadTypeRegistry.clientboundPlay()"),
    # entity type constants moved from EntityType to EntityTypes
    (r"\bEntityType\.([A-Z][A-Z0-9_]+)\b", r"EntityTypes.\1"),
    (r"import net\.minecraft\.world\.entity\.EntityType;", "import net.minecraft.world.entity.EntityTypes;"),
]

for p in pathlib.Path("src").rglob("*.java"):
    s = o = p.read_text(encoding="utf-8")
    for a, b in RULES:
        s = re.sub(a, b, s)
    if s != o:
        p.write_text(s, encoding="utf-8")
        print("rewrote", p)
