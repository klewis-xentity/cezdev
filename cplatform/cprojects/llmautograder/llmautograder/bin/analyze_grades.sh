#!/bin/bash

# -------------------------------------------------------------------------------------------------------
# name: analyze_grades.sh
# desc: Analyzes the grades by comparing the machine-generated grades with 
#       the human grades, producing correlation matrices and histograms
# usage: ./analyze_grades.sh
# -------------------------------------------------------------------------------------------------------

# Get the directory where this script is located
BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

# Call the Python script with paths relative to this script
python "$BASEDIR/../src/analyze_grades.py" \
  "$BASEDIR/../data/compare_graders/grades_evaluation_with_letters.csv" \
  "$BASEDIR/../data/analyze_grades/grades_distribution" \
  "$BASEDIR/../data/analyze_grades/grades_rank_correlation"
