#!/bin/bash

# -------------------------------------------------------------------------------------------------------
# name: convert_grade_file_from_csv_to_json.sh
# desc: Converts a grades CSV file into a JSON format
# usage: ./convert_grade_file_from_csv_to_json.sh
# -------------------------------------------------------------------------------------------------------

# Get the directory of this script
BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Define input and output paths relative to the script
csv_file="$BASEDIR/../data/osfstorage-archive/grades.csv"
json_file="$BASEDIR/../data/convert_grade_file_from_csv_to_json/grades.json"

# Run the Python script
python "$BASEDIR/../src/convert_grade_file_from_csv_to_json.py" "$csv_file" "$json_file"
