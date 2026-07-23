::-------------------------------------------------------------------------------------------------------
:: name: analyze_grades.bat
:: desc: analyzes the grades by comparing the machine generated grades with 
:: 	 the human grades producing correlations matrices and histograms
:: usage: analyze_grades 
::-------------------------------------------------------------------------------------------------------
@echo off
setlocal EnableExtensions
for %%i in ("%~dp0..") do set "cautograderdirpath=%%~fi"
python ^
  "%cautograderdirpath%\src\analyze_grades.py" ^
  "%cautograderdirpath%\data\compare_graders\grades_evaluation_with_letters.csv" ^
  "%cautograderdirpath%\data\analyze_grades\grades_distribution" ^
  "%cautograderdirpath%\data\analyze_grades\grades_rank_correlation"