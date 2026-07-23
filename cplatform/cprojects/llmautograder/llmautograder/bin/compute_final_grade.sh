#!/bin/bash

# -------------------------------------------------------------------------------------------------------
# Name: compute_final_grade.sh
# Usage: ./compute_final_grade.sh <submission_id> <rubric_filename>
# Output: Outputs grading results to a JSON file in data/compute_final_grade/m_grades.json.
# Example: ./compute_final_grade.sh 2 rubric.json
# -------------------------------------------------------------------------------------------------------

# Check for required arguments
if [ $# -ne 2 ]; then
  echo "Usage: $0 <submission_id> <rubric_filename>"
  exit 1
fi

submission_id="$1"
rubric_filename="$2"

# Get the directory where this script is located and the autograder root
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

# Define paths relative to the autograder root
graded_submission_file="$AUTOGRADER_DIR/data/grade_submission/${submission_id}.json"
if [ -f "$rubric_filename" ]; then
  rubric_file="$rubric_filename"
else
  rubric_file="$AUTOGRADER_DIR/data/rubic/markingRubric/${rubric_filename}"
fi

if [ ! -f "$graded_submission_file" ]; then
  echo "[ERROR] Graded submission file not found: $graded_submission_file"
  echo "[INFO] Choose an existing JSON file under $AUTOGRADER_DIR/data/grade_submission"
  exit 2
fi

if [ ! -f "$rubric_file" ]; then
  echo "[ERROR] Rubric file not found: $rubric_file"
  echo "[INFO] You can also pass a full or relative path to a rubric JSON file."
  echo "[INFO] Choose an existing rubric file under $AUTOGRADER_DIR/data/rubic/markingRubric"
  exit 2
fi

# Construct and run the command
"$PYTHON_CMD" "$AUTOGRADER_DIR/src/compute_final_grade.py" main "$graded_submission_file" "$rubric_file"

grade_exit_code=$?

echo
echo "============================================================"
if [ "$grade_exit_code" -eq 0 ]; then
  echo "  FINAL GRADE COMPLETE"
else
  echo "  FINAL GRADE FAILED: exit code $grade_exit_code"
fi
echo "============================================================"
exit "$grade_exit_code"
