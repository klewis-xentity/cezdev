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

# Get the directory of this script
BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Construct full path to submission IDs file
idfilepath="$BASEDIR/../data/submission_ids/$submission_ids_filename"

# Check if the submission IDs file exists
if [ ! -f "$idfilepath" ]; then
  echo "File '$idfilepath' not found!"
  exit 1
fi

# Loop through each ID in the file
while IFS= read -r submission_id || [ -n "$submission_id" ]; do
  # Skip empty lines
  if [ -z "$submission_id" ]; then
    continue
  fi

  echo "Running compute_final_grade.sh for ID $submission_id"
  "$BASEDIR/compute_final_grade.sh" "$submission_id" "$rubric_filename"
done < "$idfilepath"

echo "All commands executed."
