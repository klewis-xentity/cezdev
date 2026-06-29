#-----------------------------------------------------------------------------------------------------------------
# name: grade_a_submission_by_id
# desc: grades a submission by passing in the id of the submission rubic quesions and template code artifacts
#-----------------------------------------------------------------------------------------------------------------
import fire
from cgradesubmission.cgradesubmission import grade_submission, grade_submission_meta_data

#----------------------------------------------------------------------------------------------------
# name: main()
# desc: passes the submission id to grade along with a set of rubic questions and template code 
#----------------------------------------------------------------------------------------------------
def main(strsubmissionbyidpath, strrubicquestionspath, strtemplatecodepath):
    grade_submission(
	strsubmissionbyidpath
	strrubicquestionspath
 	strtemplatecodepath
    )    
# end main()

#-------------------------------------------------------------
# name: main()
# desc: test the CLLM class
#-------------------------------------------------------------
#def meta(submissionnumber=3):
#    grade_submission_meta_data(
#        f"C:/Users/klewi/Desktop/assignment_3/submissions/{submissionnumber}", 
#        "C:/Users/klewi/Desktop/assignment_3/rubric_questions.txt", 
#        "C:/Users/klewi/Desktop/assignment_3/template_code"
#    )       
# end main()

#----------------------------------------
# main entry point
#----------------------------------------
if __name__ == "__main__":
    fire.Fire()
# end if
