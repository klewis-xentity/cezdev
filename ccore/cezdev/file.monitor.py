r"""
name: file.monitor.py
desc: Monitors directories for file changes using watchdog library.
usage: python file.monitor.py <directories> [callbacks] [exclude-patterns]
examples:
    python file.monitor.py "C:\myproject"
    python file.monitor.py "C:\proj1,C:\proj2"
    python file.monitor.py "C:\myproject" "callback.bat"
    python file.monitor.py "C:\proj1,C:\proj2" "cb1.bat,cb2.bat"
    python file.monitor.py "C:\myproject" "callback.bat" "\.git,\.pyc"
"""

import sys
import re
import time
import subprocess
from datetime import datetime
from pathlib import Path

try:
    from watchdog.observers import Observer
    from watchdog.events import FileSystemEventHandler
except ImportError:
    print("[ERROR] watchdog library not installed.")
    print("Run: pip install watchdog")
    sys.exit(1)


class FileChangeHandler(FileSystemEventHandler):
    def __init__(self, exclude_pattern=None, watch_dirs=None, callbacks=None):
        super().__init__()
        self.exclude_pattern = re.compile(exclude_pattern) if exclude_pattern else None
        self.watch_dirs = watch_dirs or []
        self.callbacks = callbacks or []
    
    def _should_skip(self, filepath):
        if self.exclude_pattern:
            return self.exclude_pattern.search(filepath) is not None
        return False
    
    def _get_relative_path(self, filepath):
        for watch_dir in self.watch_dirs:
            try:
                return Path(filepath).relative_to(watch_dir)
            except ValueError:
                continue
        return Path(filepath)
    
    def _extract_platform_info(self, filepath):
        rel_path = self._get_relative_path(filepath)
        parts = rel_path.parts
        if len(parts) >= 2:
            return parts[0], parts[1]
        return None, None
    
    def _print_change(self, modified_type, filepath, item_kind):
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        filename = Path(filepath).name
        rel_path = self._get_relative_path(filepath)
        print(f"[{timestamp}] {modified_type} {item_kind}: {filename}")
        print(f"  path: {rel_path}")
    
    def _call_callbacks(self, modified_type, filepath):
        if not self.callbacks:
            print()
            return
        platform, platform_name = self._extract_platform_info(filepath)
        for callback in self.callbacks:
            callback_path = Path(callback)
            if callback_path.exists():
                print(f"  callback: {callback_path.name}")
                try:
                    cb_args = [modified_type, filepath, platform or "", platform_name or ""]
                    if callback_path.suffix.lower() in {".bat", ".cmd"}:
                        cmd = ["cmd", "/c", str(callback_path), *cb_args]
                    else:
                        cmd = [str(callback_path), *cb_args]

                    # Capture callback output so failures are visible in monitor logs.
                    result = subprocess.run(cmd, shell=False, check=False, capture_output=True, text=True)
                    if result.stdout:
                        print(result.stdout, end="" if result.stdout.endswith("\n") else "\n")
                    if result.stderr:
                        print(result.stderr, end="" if result.stderr.endswith("\n") else "\n")
                    if result.returncode != 0:
                        print(f"  [ERROR] Callback exited with code {result.returncode}: {callback}")
                except Exception as e:
                    print(f"  [ERROR] {e}")
            else:
                print(f"  [ERROR] Callback not found: {callback}")
        print()
    
    def on_created(self, event):
        if self._should_skip(event.src_path):
            return
        item_kind = "DIR" if event.is_directory else "FILE"
        self._print_change("CREATE", event.src_path, item_kind)
        self._call_callbacks("CREATE", event.src_path)
    
    def on_modified(self, event):
        if event.is_directory or self._should_skip(event.src_path):
            return
        self._print_change("UPDATE", event.src_path, "FILE")
        self._call_callbacks("UPDATE", event.src_path)
    
    def on_deleted(self, event):
        if self._should_skip(event.src_path):
            return
        item_kind = "DIR" if event.is_directory else "FILE"
        self._print_change("DELETE", event.src_path, item_kind)
        self._call_callbacks("DELETE", event.src_path)
    
    def on_moved(self, event):
        if self._should_skip(event.src_path) and self._should_skip(event.dest_path):
            return
        item_kind = "DIR" if event.is_directory else "FILE"
        self._print_change("RENAME", event.dest_path, item_kind)
        self._call_callbacks("RENAME", event.dest_path)


def main():
    if len(sys.argv) < 2:
        print("[ERROR] Directory path(s) not provided.")
        print("Usage: python file.monitor.py <directories> [callbacks] [exclude-patterns]")
        sys.exit(1)
    
    watch_dirs = [d.strip() for d in sys.argv[1].split(',') if d.strip()]
    callbacks = [c.strip() for c in sys.argv[2].split(',') if c.strip()] if len(sys.argv) > 2 else []
    exclude_pattern = sys.argv[3] if len(sys.argv) > 3 else None
    
    if exclude_pattern:
        exclude_pattern = '|'.join([p.strip() for p in exclude_pattern.split(',') if p.strip()])
    
    valid_dirs = [d for d in watch_dirs if Path(d).is_dir()]
    if not valid_dirs:
        print("[ERROR] No valid directories provided.")
        sys.exit(1)
    
    for d in valid_dirs:
        print(f"[INFO] Watching: {d}")
    for cb in callbacks:
        print(f"[INFO] Callback: {cb}")
    if exclude_pattern:
        print(f"[INFO] Excluding: {exclude_pattern}")
    print("[INFO] Press Ctrl+C to stop.")
    print()
    
    event_handler = FileChangeHandler(exclude_pattern, valid_dirs, callbacks)
    observer = Observer()
    for d in valid_dirs:
        observer.schedule(event_handler, d, recursive=True)
    observer.start()
    
    try:
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print()
        print("[INFO] Stopping...")
        observer.stop()
    
    observer.join()
    print("[INFO] Stopped.")


if __name__ == "__main__":
    main()
