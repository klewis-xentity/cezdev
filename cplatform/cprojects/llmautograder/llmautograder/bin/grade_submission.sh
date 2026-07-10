#!/bin/sh

# -------------------------------------------------------------------------------------------------------
# Name: grade_submission.sh
# Usage: grade_submission.sh <submission_id> [meta]
# Output: Outputs grading results to a JSON file in the data/grade_submission/<submission_id>/ directory.
# Example: grade_submission.sh 2
#          grade_submission.sh 2 meta
# -------------------------------------------------------------------------------------------------------

if [ -z "${1:-}" ]; then
  echo "Usage: $0 <submission_id> [meta]"
  exit 1
fi

submission_id="$1"
meta="$2"

BASEDIR="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)"
AUTOGRADER_DIR="$(CDPATH= cd -- "$BASEDIR/.." && pwd)"
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

submissions_path="$AUTOGRADER_DIR/data/assignment_3/submissions/$submission_id"
rubric_path="$AUTOGRADER_DIR/data/rubric_questions.txt"
template_code_path="$AUTOGRADER_DIR/data/assignment_3/template_code"

if [ ! -d "$submissions_path" ]; then
  echo "[ERROR] Submission folder not found: $submissions_path"
  echo "[INFO] Choose an existing folder under $AUTOGRADER_DIR/data/assignment_3/submissions"
  exit 2
fi

if [ ! -f "$rubric_path" ]; then
  echo "[ERROR] Rubric file not found: $rubric_path"
  exit 2
fi

if [ ! -d "$template_code_path" ]; then
  echo "[ERROR] Template code folder not found: $template_code_path"
  exit 2
fi

echo
echo "============================================================"
echo "  GRADING SUBMISSION: $submission_id"
echo "============================================================"
echo

if [ "$meta" = "meta" ]; then
  echo "Mode: Meta Data Grading"
  echo
  "$PYTHON_CMD" "$AUTOGRADER_DIR/src/grade_submission.py" grade_submission_meta_data "$submissions_path" "$rubric_path" "$template_code_path"
else
  echo "Mode: Full Submission Grading"
  echo
  "$PYTHON_CMD" "$AUTOGRADER_DIR/src/grade_submission.py" grade_submission "$submissions_path" "$rubric_path" "$template_code_path"
fi

grade_exit_code=$?

echo
echo "============================================================"
echo "  GRADING COMPLETE"
echo "============================================================"
exit "$grade_exit_code"
