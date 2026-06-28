#!/usr/bin/env python3
"""
Save copied Slack text from the clipboard to Desktop\\note.txt and open Notepad.

Use:
  1. In Slack, select the messages you want.
  2. Press Ctrl+C.
  3. Run: python D:\\cezdev\\slack_clipboard_to_note.py
"""

from __future__ import annotations

import datetime as dt
import os
import subprocess
import sys
import tkinter as tk
from pathlib import Path


def get_clipboard_text() -> str:
    root = tk.Tk()
    root.withdraw()
    try:
        return root.clipboard_get()
    finally:
        root.destroy()


def desktop_note_path() -> Path:
    user_profile = os.environ.get("USERPROFILE")
    if not user_profile:
        raise RuntimeError("USERPROFILE is not set; cannot find Desktop.")
    return Path(user_profile) / "Desktop" / "note.txt"


def main() -> int:
    try:
        copied = get_clipboard_text().strip()
    except tk.TclError:
        print("Clipboard does not contain text. Copy the Slack messages first with Ctrl+C.", file=sys.stderr)
        return 2

    if not copied:
        print("Clipboard text is empty. Copy the Slack messages first with Ctrl+C.", file=sys.stderr)
        return 2

    note = "\n".join(
        [
            "Slack copied entries",
            f"Captured: {dt.datetime.now().strftime('%Y-%m-%d %I:%M %p')}",
            "",
            copied,
            "",
        ]
    )

    output = desktop_note_path()
    output.write_text(note, encoding="utf-8")
    print(f"Wrote {output}")

    if os.name == "nt":
        subprocess.Popen(["notepad.exe", str(output)])

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
