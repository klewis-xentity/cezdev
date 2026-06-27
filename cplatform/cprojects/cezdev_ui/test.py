import sys
import os

# Show the package is importable
from c3dclasses.ccore.cutility.clogger import CLogger

logger = CLogger()
print("Loaded package module:", os.path.abspath(sys.modules["c3dclasses.ccore.cutility.clogger"].__file__))
print("CLogger instance created:", logger)
