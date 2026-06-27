::-------------------------------------------------------------------------------------------------------
:: Name: compute_final_grade.bat
:: Usage: compute_final_grade <submission_id>
:: Output: Outputs the grading results to a JSON file in the data/compute_final_grade/m_grades.json file.
:: Example: compute_final_grade 2
:: usage: python.exe ./src/compute_final_grade.py main \
::   "C:/Users/klewi/Desktop/cautograder/data/grade_submission/0.json" 
::   "C:/Users/klewi/Desktop/cautograder/data/rubic/markingRubric/rubric.json"
:: example: compare_graders submission_ids_test.txt
::-------------------------------------------------------------------------------------------------------
@echo off
set cautograderdirpath=C:/Users/kevle/Desktop/cezdev/cprojects/autograder
set submissionidsfilename=%1
python %cautograderdirpath%\src\compare_graders.py main %1