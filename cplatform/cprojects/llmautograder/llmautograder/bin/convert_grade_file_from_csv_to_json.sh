#!/bin/bash

# -------------------------------------------------------------------------------------------------------
# name: convert_grade_file_from_csv_to_json.sh
# desc: Converts a grades CSV file into a JSON format
# usage: ./convert_grade_file_from_csv_to_json.sh
# -------------------------------------------------------------------------------------------------------

# Get the directory of this script and the autograder root
BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AUTOGRADER_DIR="$(cd "$BASEDIR/.." && pwd)"
SDK_PATH="$AUTOGRADER_DIR/src/c3dclassessdk_py"

if [ -n "${PYTHONPATH:-}" ]; then
  export PYTHONPATH="$SDK_PATH${PYTHONPATH:+:$PYTHONPATH}"
else
  export PYTHONPATH="$SDK_PATH"
fi

if command -v pythonx >/dev/null 2>&1; then
  PYTHON_CMD="pythonx"
elif command -v python >/dev/null 2>&1; then
  PYTHON_CMD="python"
elif command -v python3 >/dev/null 2>&1; then
  PYTHON_CMD="python3"
else
  echo "[ERROR] Python was not found. Install pythonx, python, or add python3 to PATH."
  exit 127
fi

# Define input and output paths relative to the script
csv_file="$AUTOGRADER_DIR/data/osfstorage-archive/grades.csv"
json_file="$AUTOGRADER_DIR/data/convert_grade_file_from_csv_to_json/grades.json"

if [ ! -f "$csv_file" ]; then
  echo "[ERROR] Grades CSV file not found: $csv_file"
  echo "[INFO] Place the grades.csv file under $AUTOGRADER_DIR/data/osfstorage-archive"
  exit 2
fi

echo
echo "============================================================"
echo "  CONVERTING GRADES CSV TO JSON"
echo "============================================================"
echo "  Input:  $csv_file"
echo "  Output: $json_file"
echo "============================================================"
echo

# Run the Python script
"$PYTHON_CMD" "$AUTOGRADER_DIR/src/convert_grade_file_from_csv_to_json.py" "$csv_file" "$json_file"

convert_exit_code=$?

echo
echo "============================================================"
if [ "$convert_exit_code" -eq 0 ]; then
  echo "  CONVERSION COMPLETE"
else
  echo "  CONVERSION FAILED: exit code $convert_exit_code"
fi
echo "============================================================"
exit "$convert_exit_code"
