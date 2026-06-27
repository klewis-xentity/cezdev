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

# Get the directory where this script is located
BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Define paths relative to the script directory
graded_submission_file="$BASEDIR/../data/grade_submission/${submission_id}.json"
rubric_file="$BASEDIR/../data/rubic/markingRubric/${rubric_filename}"

# Construct and run the command
python "$BASEDIR/../src/compute_final_grade.py" main "$graded_submission_file" "$rubric_file"
