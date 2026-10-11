"""Read-only storage inventory. No player identifiers, payloads, deletion or repair."""
import argparse
import json
from pathlib import Path

MAX_JSON_BYTES = 16 * 1024 * 1024


def inventory(world, deep=False):
    world = Path(world).resolve(strict=True)
    groups = {}
    for name, relative in {
        "utilities": "data/zianutilities",
        "manager": "data/zianmanager",
        "rct_progress": "data/zianrct-progress",
        "gts": "ziangts-v2",
    }.items():
        root = world / relative
        files = [] if not root.exists() or root.is_symlink() else [
            p for p in root.rglob("*")
            if p.is_file() and not p.is_symlink() and p.resolve().is_relative_to(world)
        ]
        sizes = [p.stat().st_size for p in files]
        groups[name] = {"files": len(sizes), "bytes": sum(sizes), "largestBytes": max(sizes, default=0)}
    ledgers = {}
    for name, relative in {
        "manager_rewards": "data/zianmanager/claims.json",
        "manager_keys": "data/zianmanager/keys.json",
        "rct_rewards": "data/zianrct-rewards.json",
        "rct_trials": "data/zianrct-legendary-trials.json",
    }.items():
        path = world / relative
        if not path.is_file() or path.is_symlink():
            ledgers[name] = {"present": False}
            continue
        size = path.stat().st_size
        info = {"present": True, "bytes": size, "byteLimit": MAX_JSON_BYTES}
        if size > MAX_JSON_BYTES:
            info["warning"] = "exceeds reviewed JSON byte limit; inspect offline"
        else:
            try:
                data = json.loads(path.read_text(encoding="utf-8"))
                if not isinstance(data, dict):
                    raise ValueError("expected object")
                if data.get("layout") == "per-player-v1":
                    directory = path.parent / data["directory"]
                    if not directory.is_dir() or directory.is_symlink() or directory.resolve().parent != path.parent.resolve():
                        raise ValueError("invalid shard directory")
                    shards = [p for p in directory.glob("*.json") if not p.is_symlink()]
                    sizes = [p.stat().st_size for p in shards]
                    info.update(backend="per-player-v1", ownerFiles=len(shards), shardBytes=sum(sizes),
                                largestShardBytes=max(sizes, default=0), byteLimitPerOwner=MAX_JSON_BYTES,
                                ownersNearByteLimit=sum(s >= int(MAX_JSON_BYTES * .8) for s in sizes))
                    if name.endswith("rewards"):
                        info["claimLimitPerOwner"] = 20000
                    if deep:
                        rows = pending = 0
                        for shard in shards:
                            if shard.stat().st_size > MAX_JSON_BYTES:
                                raise ValueError("oversized shard")
                            content = json.loads(shard.read_text(encoding="utf-8"))
                            if name.endswith("rewards"):
                                values = content["claims"]
                                pending += sum(any(p.get("phase") != "DELIVERED" for p in c["parts"]) for c in values)
                            elif name.endswith("keys"):
                                values = content["entries"].values()
                                pending += sum(c["phase"] != "PAID" for c in values)
                            else:
                                values = content["entries"]
                                pending += sum(c["phase"] != "DELIVERED" for c in values)
                            rows += len(values)
                        info.update(entries=rows, pendingOrReview=pending)
                    ledgers[name] = info
                    continue
                if name.endswith("rewards"):
                    claims = data["claims"]
                    if not isinstance(claims, list):
                        raise ValueError("expected claims array")
                    info.update(claims=len(claims), claimLimit=20000)
                    pending = sum(any(p.get("phase") != "DELIVERED" for p in c["parts"]) for c in claims)
                    info["pendingOrReview"] = pending
                    if len(claims) >= 16000:
                        info["warning"] = "approaching claim limit; do not delete closed cycles"
                elif name.endswith("keys"):
                    entries = data["entries"]
                    phases = {}
                    for record in entries.values():
                        phase = record.get("phase", "UNKNOWN")
                        phases[phase] = phases.get(phase, 0) + 1
                    info.update(entries=len(entries), entryLimit=200000, phases=phases)
                if size >= int(MAX_JSON_BYTES * 0.8):
                    info["byteWarning"] = "approaching byte limit"
            except (ValueError, KeyError, TypeError, AttributeError):
                info["warning"] = "unsupported or invalid snapshot; inspect without rewriting"
        ledgers[name] = info
    weekly = world / "data/zianutilities/progression_quests_v1/weekly"
    return {"groups": groups, "sharedLedgers": ledgers,
            "weeklyPeriods": sum(p.is_dir() and not p.is_symlink() for p in weekly.iterdir()) if weekly.is_dir() else 0,
            "mode": "read-only", "note": "Live snapshots are observational; use a stopped-server backup before maintenance."}


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("world", type=Path)
    parser.add_argument("--deep", action="store_true", help="Read owner snapshots for totals; prefer an offline backup for large worlds.")
    args = parser.parse_args()
    print(json.dumps(inventory(args.world, args.deep), indent=2))
