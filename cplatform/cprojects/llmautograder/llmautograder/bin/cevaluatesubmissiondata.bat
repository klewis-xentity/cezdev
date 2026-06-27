::-------------------------------------------------------------------------------------------------------
:: name: cevaluatesubmissiondata.bat
:: desc: 
:: usage: cevaluatesubmissiondata 1
::-------------------------------------------------------------------------------------------------------
echo @off
set CESD=%CD%
cd /d %CEZDEV_PROJECTS%/cautograder_new
set submissionnum=%1
python ./example_evaluate_submission_data.py main %submissionnum%
cd /d %CESD%