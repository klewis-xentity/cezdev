#!/bin/bash

# -------------------------------------------------------------------------------------------------------
# Name: compute_final_grades.sh
# Usage: ./compute_final_grades.sh <submission_ids_filename> <rubricfilename.json>
# Output: Outputs grading results to a JSON file in data/compute_final_grade/m_grades.json
# Example: ./compute_final_grades.sh filename_of_ids.txt rubricfilename.json
# -------------------------------------------------------------------------------------------------------

# Check for correct number of arguments
if [ $# -ne 2 ]; then
  echo "Usage: $0 <submission_ids_filename> <rubricfilename.json>"
  exit 1
fi

submission_ids_filename="$1"
rubric_filename="$2"

# Get the directory of this script and the autograder root
BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AUTOGRADER_DIR="$(cd "$BASEDIR/.." && pwd)"
ids_dir_path="$AUTOGRADER_DIR/data/submission_ids"
idfilepath="$ids_dir_path/$submission_ids_filename"

# Check if the submission IDs file exists
if [ ! -f "$idfilepath" ]; then
  echo "[ERROR] Submission IDs file not found: $idfilepath"
  echo "[INFO] Choose an existing file under $ids_dir_path"
  exit 2
fi

echo
echo "============================================================"
echo "  BATCH FINAL GRADING"
echo "============================================================"
echo "  IDs file:   $idfilepath"
echo "  Rubric:     $rubric_filename"
echo "  Autograder: $AUTOGRADER_DIR"
echo "============================================================"
echo

total=0
passed=0
failed=0
failed_ids=""

# Loop through each ID in the file
while IFS= read -r submission_id || [ -n "$submission_id" ]; do
  # Skip empty lines and comment lines
  case "$submission_id" in
    ''|\#*) continue ;;
  esac

  total=$((total + 1))
  echo "------------------------------------------------------------"
  echo "  [$total] Computing final grade for ID $submission_id"
  echo "------------------------------------------------------------"
  "$BASEDIR/compute_final_grade.sh" "$submission_id" "$rubric_filename"

  if [ $? -eq 0 ]; then
    passed=$((passed + 1))
    echo "  [OK] ID $submission_id"
  else
    failed=$((failed + 1))
    failed_ids="$failed_ids $submission_id"
    echo "  [FAILED] ID $submission_id"
  fi
  echo
done < "$idfilepath"

echo "============================================================"
echo "  BATCH FINAL GRADING SUMMARY"
echo "============================================================"
echo "  Total processed: $total"
echo "  Succeeded:       $passed"
echo "  Failed:          $failed"
if [ -n "$failed_ids" ]; then
  echo "  Failed IDs:     $failed_ids"
fi
echo "============================================================"
