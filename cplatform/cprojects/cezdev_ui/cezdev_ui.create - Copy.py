import json
import os
import sys
import time
from pathlib import Path


def collect(root_path: str) -> dict:
    if not root_path:
        return {}
    p = Path(root_path)
    if not p.exists() or not p.is_dir():
        return {}
    out = {}
    for child in sorted((x for x in p.iterdir() if x.is_dir()), key=lambda z: z.name.lower()):
        out[child.name] = str(child.resolve())
    return out


def folder_key(root_path: str, fallback: str) -> str:
    if not root_path:
        return fallback
    name = Path(root_path).name.strip().lower()
    return name if name else fallback


def main() -> int:
    cvars_path = os.environ.get("CMETADATA_CVARS", "")
    if not cvars_path:
        print("[WARN] CMETADATA_CVARS is not set.")
        return 1

    clib_root = os.environ.get("CLIBRARYS", "") or os.environ.get("CLIBRARIES", "")

    env_root = os.environ.get("CENVIRONMENTS", "")
    proj_root = os.environ.get("CPROJECTS", "")

    environment_map = collect(env_root)
    library_map = collect(clib_root)
    project_map = collect(proj_root)

    env_key = folder_key(env_root, "cenvironments")
    lib_key = folder_key(clib_root, "clibraries")
    proj_key = folder_key(proj_root, "cprojects")

    payload = {
        env_key: [environment_map],
        lib_key: [library_map],
        proj_key: [project_map],
    }

    print(json.dumps(payload, separators=(",", ":")))

    db = {}
    cvars_file = Path(cvars_path)
    if cvars_file.exists():
        try:
            db = json.loads(cvars_file.read_text(encoding="utf-8-sig"))
        except Exception:
            db = {}

    now_ms = int(time.time() * 1000)

    key = "cezdev_ui.cplatform"
    db[key] = {
        "m_strname": key,
        "m_value": json.dumps(payload, separators=(",", ":")),
        "m_strtype": "string",
        "m_icreated": now_ms,
        "m_iupdated": -1,
        "m_iretrieved": -1,
    }
    print("[SAVED] cezdev_ui.cplatform")

    cvars_file.write_text(json.dumps(db, separators=(",", ":")), encoding="utf-8")
    return 0


if __name__ == "__main__":
    sys.exit(main())
