import os
import sys
from pathlib import Path

def needs_rebuild(build_file: str, src: str, extensions: list[str] | None = None) -> bool:
    """
    Determine if a rebuild is needed by comparing modification times.
    
    Args:
        build_file (str): The built artifact file (e.g., jar, exe, zip).
        src (str): Source directory OR single source file to check against.
        extensions (list[str] | None): Optional list of file extensions to consider.
                                       If None, all files are checked.
    
    Returns:
        bool: True if rebuild is needed, False otherwise.
    """
    build_path = Path(build_file)
    src_path = Path(src)

    # If build artifact doesn't exist → rebuild required
    if not build_path.exists():
        return True

    build_mtime = build_path.stat().st_mtime

    if src_path.is_file():
        # Compare single file directly
        return src_path.stat().st_mtime > build_mtime

    elif src_path.is_dir():
        # Walk directory and compare all matching files
        for root, _, files in os.walk(src_path):
            for f in files:
                if extensions is None or Path(f).suffix in extensions:
                    src_file = Path(root) / f
                    if src_file.stat().st_mtime > build_mtime:
                        return True
        return False

    else:
        raise FileNotFoundError(f"Source path does not exist: {src}")


if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Usage: python needs_rebuild.py <build_file> <src_path> [ext1,ext2,...]")
        sys.exit(1)

    build_file = sys.argv[1]
    src = sys.argv[2]
    extensions = None

    if len(sys.argv) > 3:
        extensions = [ext if ext.startswith(".") else f".{ext}" for ext in sys.argv[3].split(",")]

    try:
        result = needs_rebuild(build_file, src, extensions)
        print("True" if result else "False")
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)
