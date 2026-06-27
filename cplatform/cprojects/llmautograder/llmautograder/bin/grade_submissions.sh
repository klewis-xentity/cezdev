#!/bin/bash

# -------------------------------------------------------------------------------------------------------
# Name: grade_a_submission.sh
# Usage: ./grade_a_submission.sh <submission_ids_filename> [meta]
# Output: Outputs grading results to JSON files in data/grade_submission/<submission_id>/ directories.
# Example: ./grade_a_submission.sh filename_of_ids.txt
#          ./grade_a_submission.sh filename_of_ids.txt meta
# -------------------------------------------------------------------------------------------------------

# Ensure at least one argument is provided
if [ $# -lt 1 ]; then
  echo "Usage: $0 <submission_ids_filename> [meta]"
  exit 1
fi

submission_ids_filename="$1"
meta_flag="$2"

# Get the directory of the script
BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Path to the submission IDs file
idfilepath="$BASEDIR/../data/submission_ids/$submission_ids_filename"

# Check if file exists
if [ ! -f "$idfilepath" ]; then
  echo "File '$idfilepath' not found!"
  exit 1
fi

# Process each line in the file
while IFS= read -r submission_id || [ -n "$submission_id" ]; do
  if [ -z "$submission_id" ]; then
    continue
  fi

  if [ "$meta_flag" = "meta" ]; then
    echo "Running grade_submission.sh meta for ID $submission_id"
    "$BASEDIR/grade_submission.sh" "$submission_id" meta
  else
    echo "Running grade_submission.sh for ID $submission_id"
    "$BASEDIR/grade_submission.sh" "$submission_id"
  fi
done < "$idfilepath"

echo "All commands executed."
