import fire
from ccodeevaluator.cevaluatesubmissions import evaluate

#-------------------------------------------------------------
# name: main()
# desc: test the CLLM class
#-------------------------------------------------------------
def main(submissionnumber, params=None):
    grade = evaluate (
        "C:/Users/klewi/Desktop/assignment_3", 
        submissionnumber, 
        "markingRubric",
        params
    )
    return grade
# end main()

#----------------------------------------
# main entry point
#----------------------------------------
if __name__ == "__main__":
    fire.Fire()
# end if
