# underscores.py
# desc: Python equivalent of your Java "__" utilities (with light, Pythonic shims)

from __future__ import annotations

import csv
import io
import json
import math
import os
import random as _random
import shutil
import subprocess
import sys
import threading
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Callable, Iterable, List, Optional, Union

# -------- optional GUI (alert/prompt/file pickers) ----------
try:
    import tkinter as _tk
    from tkinter import filedialog as _filedialog, messagebox as _messagebox
    _HAS_TK = True
except Exception:
    _HAS_TK = False

# -------- optional imaging (Pillow) ----------
try:
    from PIL import Image
    _HAS_PIL = True
except Exception:
    _HAS_PIL = False

# -------- optional psutil for PID/process helpers ----------
try:
    import psutil  # type: ignore
    _HAS_PSUTIL = True
except Exception:
    _HAS_PSUTIL = False

# ============================================
# Lightweight shims for CArray / CHash / CMatrix / CVector / CFunction
# ============================================

class CArray(list):
    """List with a few convenience methods mirroring your Java wrapper."""
    def push(self, *values: Any) -> None:
        self.extend(values)

    def join(self, sep: str) -> str:
        return sep.join(str(x) for x in self)

    def length(self) -> int:
        return len(self)

    def _carray(self, i: int) -> Optional["CArray"]:
        try:
            v = self[i]
            return v if isinstance(v, CArray) else None
        except IndexError:
            return None

    def _double(self, i: int) -> float:
        return float(self[i])

    def _string(self, i: int) -> str:
        return str(self[i])

    def _nan(self, i: int) -> bool:
        try:
            return isinstance(self[i], float) and math.isnan(self[i])  # close enough
        except Exception:
            return False


class CHash(dict):
    """Dict with helpers roughly matching your Java wrapper."""
    def _string(self, key: str, default: str = "") -> str:
        return str(self.get(key, default))

    def _(self, key: Union[str, int], default: Any = None) -> Any:
        return self.get(key, default)

    def keys(self) -> CArray:  # type: ignore[override]
        return CArray(super().keys())

    def append(self, other: Optional["CHash"]) -> None:
        if other:
            self.update(other)


@dataclass
class CVector:
    data: List[float]
    def __init__(self, *v: float):
        if len(v) == 1 and isinstance(v[0], int):
            self.data = [0.0] * v[0]
        elif len(v) == 1 and isinstance(v[0], (list, tuple)):
            self.data = [float(x) for x in v[0]]
        else:
            self.data = [float(x) for x in v]
    def __iter__(self):
        return iter(self.data)
    def __len__(self):
        return len(self.data)
    def __getitem__(self, i: int) -> float:
        return self.data[i]


class CMatrix:
    """Row-major 2D matrix with ij access like in your Java code."""
    def __init__(self, rows: int = 0, cols: int = 0):
        self.data = [[0.0 for _ in range(cols)] for _ in range(rows)]
    def rowLength(self) -> int:
        return len(self.data)
    def columnLength(self) -> int:
        return len(self.data[0]) if self.data else 0
    def ij(self, i: int, j: int, value: Optional[float] = None) -> float:
        if value is None:
            return float(self.data[i][j])
        self.data[i][j] = float(value)
        return float(value)


class CObject(dict):
    pass


class CFunctionRegistry:
    _fns: dict[str, Callable[..., Any]] = {}

    @classmethod
    def set(cls, name: str, fn: Callable[..., Any]) -> None:
        cls._fns[name] = fn

    @classmethod
    def get(cls, name: str) -> Optional[Callable[..., Any]]:
        return cls._fns.get(name)


# =====================
# Console input helpers
# =====================

def is_empty() -> bool:
    return sys.stdin is None or sys.stdin.closed

def read_int() -> int:
    return int(sys.stdin.readline())

def read_double() -> float:
    return float(sys.stdin.readline())

def read_float() -> float:
    return float(sys.stdin.readline())

def read_long() -> int:
    return int(sys.stdin.readline())

def read_boolean() -> bool:
    return sys.stdin.readline().strip().lower() in ("1", "true", "yes", "y")

def read_char() -> str:
    return sys.stdin.read(1)

def read_byte() -> int:
    b = sys.stdin.buffer.read(1)
    return b[0] if b else -1

def read_string() -> str:
    return sys.stdin.readline().strip()

def has_next_line() -> bool:
    return not sys.stdin.closed

def read_line() -> str:
    return sys.stdin.readline().rstrip("\n")

def read_all() -> str:
    return sys.stdin.read()

# simple scanner-like helper
def readln() -> str:
    return input()

# ==================
# Printing utilities
# ==================

def echo(obj: Any) -> None: print(obj, end="")
def printbr(s: str = "") -> None: print(f"{s}<br />", end="")
def println(obj: Any = "") -> None: print(str(obj))
def printjs(js: Any) -> None: print(f"<script>{js}</script>", end="")
def _print(x: Any) -> None: print(x, end="")

def print_arr(arr: Iterable[Any], delimiter: str = ",") -> None:
    print(delimiter.join(str(x) for x in arr), end="")

def print_r(obj: Any, bstr: bool = False) -> str:
    def _recurse(o: Any) -> str:
        if isinstance(o, (list, tuple, CArray)):
            return ",".join(_recurse(x) for x in o)
        return str(o)
    s = _recurse(obj)
    if bstr:
        return s
    print(s, end="")
    return ""

def cont(obj: Any, alertmsg: str) -> None:
    _print(obj)
    alert(alertmsg)

# =========================
# Alerts / prompts (UI-ish)
# =========================

def alert(message: Any, title: str = "Alert") -> None:
    msg = str(message) if message is not None else ""
    if _HAS_TK:
        root = _tk.Tk(); root.withdraw()
        _messagebox.showinfo(title, msg)
        root.destroy()
    else:
        print(f"[{title}] {msg}")

def prompt(title: str, message: str, choices: Optional[List[str]] = None) -> Optional[str]:
    if _HAS_TK and choices:
        root = _tk.Tk(); root.withdraw()
        # No direct "choices" dialog in Tk; fall back to console listing
        print(f"{title}: {message}")
        for i, c in enumerate(choices):
            print(f"  {i+1}. {c}")
        root.destroy()
    # console fallback
    try:
        return input(f"{title}: {message} ").strip() or None
    except EOFError:
        return None

def prompt_file(file_or_dir: str = "file", title: str = "Open") -> Optional[str]:
    if _HAS_TK:
        root = _tk.Tk(); root.withdraw()
        path = None
        if file_or_dir == "dir":
            path = _filedialog.askdirectory(title=title)
        else:
            path = _filedialog.askopenfilename(title=title)
        root.destroy()
        return path or None
    # console fallback
    return input(f"{title} path: ").strip() or None

def prompt_path(title: str = "Choose Directory") -> Optional[str]:
    return prompt_file("dir", title)

def console_log(message: Any) -> None:
    print(f"console.log('{message}')")

def confirm(message: str) -> bool:
    if _HAS_TK:
        root = _tk.Tk(); root.withdraw()
        ans = _messagebox.askyesno("Confirm", message)
        root.destroy()
        return bool(ans)
    # console fallback
    ans = input(f"{message} [y/N]: ").strip().lower()
    return ans in ("y", "yes")

# =======
# timing
# =======

def time_ms() -> int:
    return int(time.time() * 1000)

def get_time() -> int:
    return time_ms()

def get_time_mms() -> float:
    # micro-milliseconds? matching Java nano/1000 behavior
    return time.time_ns() / 1000.0

def sleep(ms: int) -> None:
    time.sleep(ms / 1000.0)

# =====
# hash
# =====

def hash_code(obj: Any) -> int:
    return id(obj)

# ======================
# dir / file / path I/O
# ======================

def dirname(filename: str) -> str:
    return str(Path(filename).resolve().parent)

def get_home_path() -> str:
    return str(Path(".").resolve())

def get_path(filename: str) -> str:
    try:
        return str(Path(filename).resolve().parent)
    except Exception:
        return ""

def is_directory(pathname: str) -> bool:
    try:
        return Path(pathname).is_dir()
    except Exception:
        return False

def is_file(pathname: str) -> bool:
    try:
        return Path(pathname).exists()
    except Exception:
        return False

def traverse_path(start_path: str, fncallback: Callable[[Path], Any]) -> None:
    p = Path(start_path)
    if not p.exists():
        return
    for root, dirs, files in os.walk(p):
        root_path = Path(root)
        fncallback(root_path)
        for f in files:
            fncallback(root_path / f)

def get_file_contents(filename: str) -> str:
    try:
        return Path(filename).read_text(encoding="utf-8")
    except Exception:
        return ""

def set_file_contents(filename: str, contents: str) -> bool:
    try:
        Path(filename).write_text(contents or "", encoding="utf-8")
        return True
    except Exception:
        return False

def append_file_contents(filename: str, contents: str) -> bool:
    try:
        with open(filename, "a", encoding="utf-8") as f:
            f.write(contents or "")
        return True
    except Exception:
        return False

file_get_contents = get_file_contents
file_set_contents = set_file_contents

def file_exists(filename: str) -> bool:
    return Path(filename).exists()

def get_lines_from_file(
    filename: str,
    split_lines: bool = False,
    delimiter: Optional[str] = None,
    replacement: Optional[str] = None,
) -> Optional[CArray]:
    if not filename:
        return None
    out = CArray()
    try:
        with open(filename, encoding="utf-8") as f:
            for raw in f:
                line = raw.rstrip("\n")
                if not line.strip():
                    continue
                if replacement is not None and delimiter:
                    # naive: replace delimiters inside quoted sections
                    buf, in_q = [], False
                    for ch in line:
                        if ch == '"':
                            in_q = not in_q
                            buf.append(ch)
                        elif in_q and ch == delimiter:
                            buf.append(replacement)
                        else:
                            buf.append(ch)
                    line = "".join(buf)
                out.push(line if not split_lines else CArray(*(line.split(delimiter or ","))))
        return out
    except Exception as ex:
        alert(str(ex))
        return None

def load_csv_file(filepath: str) -> CArray:
    rows = CArray()
    try:
        with open(filepath, newline="", encoding="utf-8") as f:
            rdr = csv.reader(f)
            for row in rdr:
                rows.push(CArray(*row))
    except Exception as ex:
        println(ex)
        alert(ex)
    return rows

def save_csv_file(filepath: str, carray: CArray) -> bool:
    try:
        with open(filepath, "w", newline="", encoding="utf-8") as f:
            w = csv.writer(f)
            for i, row in enumerate(carray):
                if isinstance(row, CArray):
                    w.writerow(list(row))
        return True
    except Exception as ex:
        println(ex)
        alert(ex)
        return False

def file_delete(path: str) -> bool:
    try:
        Path(path).unlink(missing_ok=True)
        return True
    except Exception:
        return False

def file_copy(src: str, dst: str) -> bool:
    try:
        Path(dst).parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
        return True
    except Exception:
        return False

# ------------------------------
# file_path / dir_path registry
# ------------------------------

_m_filetodir: Optional[CHash] = None

def _ensure_file_registry() -> None:
    global _m_filetodir
    if _m_filetodir is not None:
        return
    base = Path(get_home_path())
    for rel in ("../c3dclassessdk.filenames.json", "../../c3dclassessdk.filenames.json"):
        p = (base / rel).resolve()
        if p.exists():
            try:
                _m_filetodir = CHash(json.loads(p.read_text(encoding="utf-8")))
                return
            except Exception:
                pass
    _m_filetodir = CHash()

def file_path(filename: Union[str, object]) -> str:
    _ensure_file_registry()
    if not isinstance(filename, str):
        filename = f"{filename.__class__.__name__}.py"
    return _m_filetodir._string(filename) if _m_filetodir else ""

def dir_path(filename_or_obj: Union[str, object]) -> str:
    path = file_path(filename_or_obj if isinstance(filename_or_obj, str) else f"{filename_or_obj.__class__.__name__}.py")
    return get_path(path) if path else ""

def check_file_path(path: str) -> str:
    p = file_path(path)
    return p or path

def check_dir_path(path: str) -> str:
    p = file_path(path)
    return p or path

# ---- out files next to resolved dir_path ----

def out(filename: str, contents: str) -> None:
    file_set_contents(filename, contents)

def out_append(filename: str, contents: str) -> None:
    append_file_contents(filename, contents)

def outln(filename: str, contents: str) -> None:
    append_file_contents(filename, contents + "\n")

def out_for(obj: object, contents: Any, append: bool = False) -> None:
    base = dir_path(obj)
    if not base:
        base = get_home_path()
    fname = f"{obj.__class__.__name__}.out"
    full = str(Path(base) / fname)
    (out_append if append else out)(full, str(contents))

def outln_for(obj: object, contents: Any) -> None:
    out_append(str(Path(dir_path(obj) or get_home_path()) / f"{obj.__class__.__name__}.out"), f"{contents}\n")

# ==============
# Image helpers
# ==============

def load_img_file(filename: str) -> Optional[CMatrix]:
    if not _HAS_PIL:
        return None
    try:
        img = Image.open(filename).convert("L")  # grayscale
        w, h = img.size
        pix = img.load()
        M = CMatrix(h, w)
        for y in range(h):
            for x in range(w):
                M.ij(y, x, float(pix[x, y]))
        return M
    except Exception:
        return None

def save_img_file(out_filename: str, img_format: str, M_or_img: Union[CMatrix, "Image.Image"]) -> bool:
    if not _HAS_PIL:
        return False
    try:
        if isinstance(M_or_img, CMatrix):
            w, h = M_or_img.columnLength(), M_or_img.rowLength()
            im = Image.new("L", (w, h))
            for y in range(h):
                for x in range(w):
                    v = int(M_or_img.ij(y, x))
                    im.putpixel((x, y), max(0, min(255, v)))
        else:
            im = M_or_img
        im.save(out_filename, format=img_format.upper())
        return True
    except Exception:
        return False

def resize_img_file(input_path: str, output_path: str, scaled_width: Optional[int] = None, scaled_height: Optional[int] = None, percent: Optional[float] = None) -> bool:
    if not _HAS_PIL:
        return False
    try:
        im = Image.open(input_path)
        if percent is not None:
            scaled_width = int(im.width * percent)
            scaled_height = int(im.height * percent)
        assert scaled_width and scaled_height
        im = im.resize((scaled_width, scaled_height))
        im.save(output_path)
        return True
    except Exception:
        return False

# ==========
# Allocation
# ==========

def _new(qualified_classname: str) -> Any:
    """Create instance from 'package.module:ClassName' or 'module.ClassName'."""
    import importlib
    modname, clsname = None, None
    if ":" in qualified_classname:
        modname, clsname = qualified_classname.split(":", 1)
    else:
        parts = qualified_classname.split(".")
        modname, clsname = ".".join(parts[:-1]), parts[-1]
    mod = importlib.import_module(modname)
    cls = getattr(mod, clsname)
    return cls()

# =======
# parse
# =======

def parse_int(s: str) -> int: return int(s)
def parse_float(s: str) -> float: return float(s)
def parse_double(s: str) -> float: return float(s)

# ==========================
# split / explode (strings)
# ==========================

def split(sep: str, s: str) -> CArray:
    if not sep or not s:
        return CArray()
    return CArray(*s.split(sep))

def explode(sep: str, s: str) -> CArray:
    return split(sep, s)

# =========
# commands
# =========

def exec_command(cmd: Union[str, List[str]]) -> Optional[CArray]:
    try:
        proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, shell=isinstance(cmd, str), text=True)
        out = CArray()
        assert proc.stdout is not None
        for line in proc.stdout:
            out.push(line.rstrip("\n"))
        proc.wait()
        return out
    except Exception as ex:
        print(ex, file=sys.stderr)
        return None

def exec_async_command(cmd: Union[str, List[str]]) -> bool:
    try:
        subprocess.Popen(cmd, stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL, shell=isinstance(cmd, str))
        print(f"SUCCESS: exec_async_command(): executed: {cmd}")
        return True
    except Exception as ex:
        print(f"ERROR: exec_async_command(): {ex}", file=sys.stderr)
        return False

# =========
# PIDs
# =========

def is_pid_running(pid: int) -> bool:
    if _HAS_PSUTIL:
        return psutil.pid_exists(pid)
    # Fallback: platform-specific
    try:
        if os.name == "nt":
            out = exec_command(f'tasklist /FI "PID eq {pid}"')
            return any(f" {pid} " in line for line in (out or []))
        else:
            proc = subprocess.run(["ps", "-p", str(pid)], capture_output=True, text=True)
            return str(pid) in proc.stdout
    except Exception:
        return False

def to_str_pids() -> str:
    if _HAS_PSUTIL:
        buf = io.StringIO()
        for p in psutil.process_iter(attrs=["pid", "name"]):
            buf.write(f"{p.info['pid']:>6}  {p.info['name']}\n")
        return buf.getvalue()
    try:
        if os.name == "nt":
            out = exec_command("tasklist") or CArray()
            return "\n".join(out)
        else:
            proc = subprocess.run(["ps", "aux"], capture_output=True, text=True)
            return proc.stdout
    except Exception:
        return ""

def get_pid() -> int:
    return os.getpid()

# =========================
# setInterval / setTimeout
# =========================

class _TimerStore:
    _next_id = 1
    _timers: dict[int, threading.Timer] = {}

    @classmethod
    def add(cls, t: threading.Timer) -> int:
        tid = cls._next_id
        cls._next_id += 1
        cls._timers[tid] = t
        t.start()
        return tid

    @classmethod
    def cancel(cls, tid: int) -> None:
        t = cls._timers.pop(tid, None)
        if t:
            t.cancel()

def setInterval(fn: Callable[[], Any], ms: int) -> int:
    def _wrapper():
        fn()
        # re-arm
        t = threading.Timer(ms / 1000.0, _wrapper)
        _TimerStore._timers[tid] = t
        t.start()
    t = threading.Timer(ms / 1000.0, _wrapper)
    tid = _TimerStore.add(t)
    return tid

def clearInterval(tid: int) -> None:
    _TimerStore.cancel(tid)

def setTimeout(fn: Callable[[], Any], ms: int) -> int:
    t = threading.Timer(ms / 1000.0, fn)
    return _TimerStore.add(t)

def clearTimeout(tid: int) -> None:
    _TimerStore.cancel(tid)

# ===========
# factories
# ===========

def carray_csv(filename: str) -> CArray: return load_csv_file(filename)
def carray_c(*capacity: int) -> CArray: return CArray()  # capacity not needed in Python
def carray_1(obj: Any) -> CArray: return CArray(obj)
def carray(*objects: Any) -> CArray: return CArray(*objects)
def args(*objects: Any) -> CArray: return CArray(*objects)
def a(*objects: Any) -> CArray: return CArray(*objects)

def chash(*objects: Any) -> CHash:
    """Accept pairs like ('k', v, 'k2', v2, ...) or a dict."""
    if len(objects) == 1 and isinstance(objects[0], dict):
        return CHash(objects[0])
    h = CHash()
    it = iter(objects)
    for k, v in zip(it, it):
        h[str(k)] = v
    return h

def json_file_2_chash(jsonfile: str) -> CHash:
    try:
        return CHash(json.loads(get_file_contents(jsonfile)))
    except Exception:
        return CHash()

def json_str_2_chash(s: str) -> CHash:
    try:
        return CHash(json.loads(s))
    except Exception:
        return CHash()

def params(*objects: Any) -> CHash: return chash(*objects)
def h(*objects: Any) -> CHash: return chash(*objects)

def cobject(*objects: Any) -> CObject: return CObject(objects=objects)
def obj(*objects: Any) -> CObject: return cobject(*objects)
def o(*objects: Any) -> CObject: return cobject(*objects)

def cvector(*v: float) -> CVector: return CVector(*v)
def v(*v: float) -> CVector: return CVector(*v)
def v_c(size: int) -> CVector: return CVector(size)

def cmatrix_from_carray(ca: CArray, skip: Optional[CArray] = None) -> Optional[CMatrix]:
    if not ca or ca._carray(0) is None:
        return None
    nr = ca.length()
    nc = ca._carray(0).length()
    sk = set(skip or [])
    M = CMatrix(nr, nc - len(sk))
    for i in range(nr):
        row = ca._carray(i)
        if row is None:
            continue
        col_idx = 0
        for j in range(nc):
            if j in sk:
                continue
            if not row._nan(j):
                M.ij(i, col_idx, float(row[j]))
            col_idx += 1
    return M

def cmatrix(*cvectors: CVector) -> CMatrix:
    if not cvectors:
        return CMatrix()
    rows = len(cvectors)
    cols = len(cvectors[0])
    M = CMatrix(rows, cols)
    for i, vec in enumerate(cvectors):
        for j, val in enumerate(vec):
            M.ij(i, j, val)
    return M

# ============
# misc helpers
# ============

def s(string: str) -> str:
    return f"\"{string}\""

def is_within(l: float, u: float, v: float) -> bool:
    return l <= v <= u

def cmp(a: Any, b: Any) -> int:
    return (a > b) - (a < b)

def eval_expr(expr: str) -> str:
    """Evaluate simple Python expression (safe-ish)."""
    try:
        # VERY limited builtins for safety
        allowed = {"__builtins__": {}}
        return str(eval(expr, allowed, {}))
    except Exception as ex:
        return str(ex)

def typeOf(obj: Any) -> str:
    if isinstance(obj, CArray): return "carray"
    if isinstance(obj, CHash): return "chash"
    if isinstance(obj, CVector): return "cvector"
    if isinstance(obj, CMatrix): return "cmatrix"
    if isinstance(obj, (float, int)): return "number"
    if isinstance(obj, str): return "string"
    if callable(obj): return "cfunction"
    return "object"

def random(min_inclusive: int, max_exclusive: int) -> int:
    # Match Java: (int)(min + Math.random()*(max-min)) ==> [min, max)
    if max_exclusive <= min_inclusive:
        return min_inclusive
    return min_inclusive + int(_random.random() * (max_exclusive - min_inclusive))
