#!/bin/bash

# -------------------------------------------------------------------------------------------------------
# Name: compute_final_grade.sh
# Usage: ./compute_final_grade.sh <submission_id>
# Output: Outputs the grading results to a JSON file in the data/compute_final_grade/m_grades.json file.
# Example: ./compute_final_grade.sh 2
# Runs: python ./src/compare_graders.py main <submission_id>
# -------------------------------------------------------------------------------------------------------

# Ensure a submission ID is provided
if [ -z "$1" ]; then
  echo "Usage: $0 <submission_id>"
  exit 1
fi

# Get the directory of the script
BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Run the Python script
python "$BASEDIR/../src/compare_graders.py" main "$1"
