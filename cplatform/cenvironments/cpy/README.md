## cpython environment

This folder contains helper scripts and packaging metadata for the Python environment used by C3DClasses SDK.

### Scripts

- `cpy.bat`: runs Python with passthrough arguments.
- `cpip.bat`: runs `python -m pip` with passthrough arguments.
- `cpycopy.bat`: copies Python sources into the metadata project folder and installs it in editable mode.
- `cpyinit.bat`: initializes the Python project folder and triggers `cpycopy.bat` if needed.

### Requirements

- Python must be available on `PATH`.
- For full `cpycopy.bat` behavior, `CEZDEV_HOME` and `CMETADATA` should be set.
