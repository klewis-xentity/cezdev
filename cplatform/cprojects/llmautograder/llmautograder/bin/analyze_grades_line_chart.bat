::-------------------------------------------------------------------------------------------------------
:: name: analyze_grades_line_chart.bat
:: desc: analyzes the grades by comparing the machine generated grades with 
:: 	 the human grades producing a line chart
:: usage: analyze_grades_line_chart 
::-------------------------------------------------------------------------------------------------------
@echo off
setlocal EnableExtensions
for %%i in ("%~dp0..") do set "cautograderdirpath=%%~fi"
python "%cautograderdirpath%\src\analyze_grades_with_line_graph.py" "%cautograderdirpath%\data\compare_graders\grades_evaluation_with_letters.csv"