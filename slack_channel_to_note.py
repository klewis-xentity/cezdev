#!/usr/bin/env python3
"""
Fetch recent Slack channel messages and save them to Desktop\note.txt.

Setup:
  1. Create a Slack app/token with these scopes as needed:
     channels:read, channels:history, users:read
     For private channels use groups:read and groups:history instead.
  2. Set the token:
     setx SLACK_BOT_TOKEN "xoxb-your-token"
  3. Run:
     python slack_channel_to_note.py --channel nsdi-gp-devops --limit 10
"""

from __future__ import annotations

import argparse
import datetime as dt
import json
import os
import re
import subprocess
import sys
import urllib.parse
import urllib.request
from pathlib import Path


SLACK_API = "https://slack.com/api"


def slack_request(method: str, token: str, params: dict[str, str | int]) -> dict:
    query = urllib.parse.urlencode(params)
    request = urllib.request.Request(
        f"{SLACK_API}/{method}?{query}",
        headers={"Authorization": f"Bearer {token}"},
    )
    with urllib.request.urlopen(request, timeout=30) as response:
        payload = json.loads(response.read().decode("utf-8"))
    if not payload.get("ok"):
        raise RuntimeError(f"Slack API error from {method}: {payload.get('error')}")
    return payload


def find_channel_id(token: str, channel_name: str) -> str:
    cursor = ""
    while True:
        payload = slack_request(
            "conversations.list",
            token,
            {
                "types": "public_channel,private_channel",
                "exclude_archived": "true",
                "limit": 1000,
                "cursor": cursor,
            },
        )
        for channel in payload.get("channels", []):
            if channel.get("name") == channel_name.lstrip("#"):
                return channel["id"]
        cursor = payload.get("response_metadata", {}).get("next_cursor", "")
        if not cursor:
            raise RuntimeError(f"Channel not found: {channel_name}")


def user_map(token: str) -> dict[str, str]:
    users: dict[str, str] = {}
    cursor = ""
    while True:
        payload = slack_request("users.list", token, {"limit": 1000, "cursor": cursor})
        for member in payload.get("members", []):
            profile = member.get("profile", {})
            users[member["id"]] = (
                profile.get("real_name")
                or profile.get("display_name")
                or member.get("name")
                or member["id"]
            )
        cursor = payload.get("response_metadata", {}).get("next_cursor", "")
        if not cursor:
            return users


def clean_text(text: str, users: dict[str, str]) -> str:
    def replace_user(match: re.Match[str]) -> str:
        return "@" + users.get(match.group(1), match.group(1))

    text = re.sub(r"<@([A-Z0-9]+)>", replace_user, text)
    text = re.sub(r"<([^|>]+)\|([^>]+)>", r"\2", text)
    text = re.sub(r"<([^>]+)>", r"\1", text)
    return text.strip()


def slack_time(ts: str) -> str:
    timestamp = dt.datetime.fromtimestamp(float(ts))
    return timestamp.strftime("%Y-%m-%d %I:%M %p")


def desktop_path() -> Path:
    user_profile = os.environ.get("USERPROFILE")
    if not user_profile:
        raise RuntimeError("USERPROFILE is not set; cannot find Desktop.")
    return Path(user_profile) / "Desktop"


def main() -> int:
    parser = argparse.ArgumentParser(description="Save recent Slack channel messages to Desktop note.txt.")
    parser.add_argument("--channel", default="nsdi-gp-devops", help="Slack channel name without #")
    parser.add_argument("--limit", type=int, default=10, help="Number of recent messages to save")
    parser.add_argument("--output", default="", help="Optional output file path")
    parser.add_argument("--no-open", action="store_true", help="Do not open note.txt in Notepad")
    args = parser.parse_args()

    token = os.environ.get("SLACK_BOT_TOKEN")
    if not token:
        print("Set SLACK_BOT_TOKEN first. Example: setx SLACK_BOT_TOKEN \"xoxb-your-token\"", file=sys.stderr)
        return 2

    channel_id = find_channel_id(token, args.channel)
    users = user_map(token)
    history = slack_request(
        "conversations.history",
        token,
        {"channel": channel_id, "limit": args.limit, "inclusive": "true"},
    )

    lines = [
        f"#{args.channel} - latest {args.limit} Slack entries",
        f"Captured: {dt.datetime.now().strftime('%Y-%m-%d %I:%M %p')}",
        "",
    ]

    for message in reversed(history.get("messages", [])):
        author = users.get(message.get("user", ""), message.get("username", "Unknown"))
        text = clean_text(message.get("text", ""), users)
        lines.append(f"{author}, {slack_time(message['ts'])}:")
        lines.append(text or "[no text]")
        reply_count = message.get("reply_count", 0)
        if reply_count:
            lines.append(f"Replies: {reply_count}")
        lines.append("")

    output = Path(args.output) if args.output else desktop_path() / "note.txt"
    output.write_text("\n".join(lines), encoding="utf-8")
    print(f"Wrote {output}")

    if not args.no_open and os.name == "nt":
        subprocess.Popen(["notepad.exe", str(output)])

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
