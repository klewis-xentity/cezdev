#!/bin/bash

# -------------------------------------------------------------------------------------------------------
# name: analyze_grades.sh
# desc: Analyzes the grades by comparing the machine-generated grades with 
#       the human grades, producing correlation matrices and histograms
# usage: ./analyze_grades.sh
# -------------------------------------------------------------------------------------------------------

# Get the directory where this script is located, then the project root
BASEDIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cautograderdirpath="$(cd "$BASEDIR/.." && pwd)"

# Call the Python script with paths relative to the project root
python \
  "$cautograderdirpath/src/analyze_grades.py" \
  "$cautograderdirpath/data/compare_graders/grades_evaluation_with_letters.csv" \
  "$cautograderdirpath/data/analyze_grades/grades_distribution" \
  "$cautograderdirpath/data/analyze_grades/grades_rank_correlation"
exit $?
