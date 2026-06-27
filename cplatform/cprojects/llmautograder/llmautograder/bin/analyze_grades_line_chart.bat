::-------------------------------------------------------------------------------------------------------
:: name: analyze_grades.bat
:: desc: analyzes the grades by comparing the machine generated grades with 
:: 	 the human grades producing correlations matrices and histograms
:: usage: analyze_grades 
::-------------------------------------------------------------------------------------------------------
echo @off
set cautograderdirpath=C:/Users/kevle/Desktop/cezdev/cprojects/autograder
python %cautograderdirpath%/src/analyze_grades_with_line_graph.py %cautograderdirpath%/data/compare_graders/grades_evaluation_with_letters.csv