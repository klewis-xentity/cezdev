#!/bin/bash

# -------------------------------------------------------------------------------------------------------
# Name: grade_submissions.sh
# Usage: ./grade_submissions.sh <submission_ids_filename> [meta]
# Output: Grades every submission id listed in the ids file by calling grade_submission.sh for each id.
# Example: ./grade_submissions.sh submission_ids_test.txt
#          ./grade_submissions.sh submission_ids_test.txt meta
# -------------------------------------------------------------------------------------------------------

# Ensure at least one argument is provided
if [ $# -lt 1 ]; then
  echo "Usage: $0 <submission_ids_filename> [meta]"
  exit 1
fi

submission_ids_filename="$1"
meta_flag="$2"

# Get the directory of the script and the autograder root
BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
AUTOGRADER_DIR="$(cd "$BASEDIR/.." && pwd)"
ids_dir_path="$AUTOGRADER_DIR/data/submission_ids"
idfilepath="$ids_dir_path/$submission_ids_filename"

# Check if file exists
if [ ! -f "$idfilepath" ]; then
  echo "[ERROR] Submission IDs file not found: $idfilepath"
  echo "[INFO] Choose an existing file under $ids_dir_path"
  exit 2
fi

echo
echo "============================================================"
echo "  BATCH GRADING"
echo "============================================================"
echo "  IDs file:   $idfilepath"
echo "  Autograder: $AUTOGRADER_DIR"
if [ "$meta_flag" = "meta" ]; then
  echo "  Mode:       Meta Data Grading"
else
  echo "  Mode:       Full Submission Grading"
fi
echo "============================================================"
echo

total=0
passed=0
failed=0
failed_ids=""

# Process each line in the file
while IFS= read -r submission_id || [ -n "$submission_id" ]; do
  # Skip empty lines and comment lines
  case "$submission_id" in
    ''|\#*) continue ;;
  esac

  total=$((total + 1))
  echo "------------------------------------------------------------"
  echo "  [$total] Grading submission ID $submission_id"
  echo "------------------------------------------------------------"
  if [ "$meta_flag" = "meta" ]; then
    "$BASEDIR/grade_submission.sh" "$submission_id" meta
  else
    "$BASEDIR/grade_submission.sh" "$submission_id"
  fi

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
echo "  BATCH GRADING SUMMARY"
echo "============================================================"
echo "  Total processed: $total"
echo "  Succeeded:       $passed"
echo "  Failed:          $failed"
if [ -n "$failed_ids" ]; then
  echo "  Failed IDs:     $failed_ids"
fi
echo "============================================================"
