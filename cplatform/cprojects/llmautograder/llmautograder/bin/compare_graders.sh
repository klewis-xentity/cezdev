#!/bin/bash

# -------------------------------------------------------------------------------------------------------
# Name: compare_graders.sh
# Usage: ./compare_graders.sh <submission_ids_filename>
# Output: Compares human and machine grades and writes results to data/compare_graders/.
# Example: ./compare_graders.sh submission_ids_test.txt
# Runs: python ./src/compare_graders.py main <submission_ids_filename>
# -------------------------------------------------------------------------------------------------------

# Ensure a submission IDs filename is provided
if [ -z "$1" ]; then
  echo "Usage: $0 <submission_ids_filename>"
  exit 1
fi

# Get the directory of the script
BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Add the local SDK to PYTHONPATH
export PYTHONPATH="$BASEDIR/../src/c3dclassessdk_py${PYTHONPATH:+:$PYTHONPATH}"

# Run the Python script
python "$BASEDIR/../src/compare_graders.py" main "$1"
exit $?
