#!/usr/bin/env python3
"""Reproducible static inventory, not a claim of runtime equivalence or test coverage."""
import json
import re
from pathlib import Path

root = Path(__file__).resolve().parents[1]
definition = root / "src/modern/java/thaumcraft/config/ThaumcraftConfig.java"
source = definition.read_text()
groups = dict(re.findall(r"(\w+)\s*=\s*(client|server)\.", source))
names = re.findall(r"public static final ForgeConfigSpec\.\w+(?:<[^;]+?>)?\s+(\w+);", source)
files = sorted((root / "src/modern/java").rglob("*.java"))
entries = []
for name in names:
    references = []
    expression = re.compile(r"\bThaumcraftConfig\." + re.escape(name) + r"\b")
    for path in files:
        if path == definition:
            continue
        for number, line in enumerate(path.read_text().splitlines(), 1):
            if expression.search(line):
                references.append(dict(file=str(path.relative_to(root)), line=number))
    entries.append(dict(name=name, owner=groups.get(name, "unknown"), references=references))
result = dict(scope="Static references in modern production Java only; excludes legacy sources and fixtures. A reference is not proof of complete behavior.",
              declared=len(entries), without_references=[e["name"] for e in entries if not e["references"]], entries=entries)
target = root / "docs/CONFIGURATION-AUDIT.json"
target.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n")
print(json.dumps(dict(declared=result["declared"], without_references=result["without_references"]), indent=2))
