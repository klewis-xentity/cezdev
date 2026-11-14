## cpython environment

This folder contains helper scripts and packaging metadata for the Python environment used by C3DClasses SDK.

### Scripts

- `cpy.bat`: runs Python with passthrough arguments.
- `cpip.bat`: runs `python -m pip` with passthrough arguments.
- `cpy.move.bat`: copies Python sources and packaging metadata into the metadata project folder.
- `cpy.build.bat`: installs requirements, then installs the SDK in editable mode. Stops and returns a nonzero exit code if any installation fails.
- `cpy.create.bat`: initializes the Python project folder and runs move/build when needed, propagating failures.

### Requirements

- The default runtime is the bundled Python 3.13.13, configured through `python.home` and `python.bin` in `cplatform/cbinaries/cbinaries.create.bat`.
- Keep the configured runtime on a version supported by the native dependencies. With Python 3.15, `zstandard` 0.25.0 falls back to a source build requiring Microsoft C++ Build Tools; Python 3.13 can use its Windows wheel.
- `python.bat` must be available on `PATH` and `python.home` must point to a Python installation with pip.
- For `cpy.create.bat`, `CEZDEV_HOME`, `C3DCLASSES`, and `CMETADATA` should be set. For `cpy.build.bat`, set `C3DCLASSES_PYPATH` to the metadata project folder.
- Restart the cezdev shell after changing the runtime so its environment variables and `PATH` are refreshed.
