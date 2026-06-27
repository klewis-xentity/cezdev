::-------------------------------------------------------------------------------------------------------
:: name: convert_grade_file_from_csv_to_json.bat
:: desc: analyzes the grades by comparing the machine generated grades with 
:: 	 the human grades producing correlations matrices and histograms
:: usage: analyze_grades 
:: example: convert_grade_file_from_csv_to_json.bat
::-------------------------------------------------------------------------------------------------------
echo @off
set cautograderdirpath=C:/Users/kevle/Desktop/cezdev/cprojects/autograder
python %cautograderdirpath%/src/convert_grade_file_from_csv_to_json.py  %cautograderdirpath%/data/osfstorage-archive/grades.csv  %cautograderdirpath%/data/convert_grade_file_from_csv_to_json/grades.json