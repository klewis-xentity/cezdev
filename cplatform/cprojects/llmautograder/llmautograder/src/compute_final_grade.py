#-------------------------------------------------------------------------------------
# file: compute_final_grade.py
# desc: computes the final grade from the graded submission matrix 
# usage: cevalcode hw_5.pdf hw_5_1.py customer_accounts_test.py rubic.json
#-------------------------------------------------------------------------------------
import os
import sys
from c3dclasses.ccore.cutility.cutility import readJSONFromFilename, writeJSONToFilename

try:
    import fire
except ImportError:
    fire = None


def get_letter_grade(num_grade):
    if 80 <= num_grade <= 100:
        return "A"
    if 70 <= num_grade < 80:
        return "B"
    if 60 <= num_grade < 70:
        return "C"
    if 50 <= num_grade < 60:
        return "D"
    return "F"


def get_numeric_grade(submission, attribute="avg_num_grade"):
    weight = 0.25
    return sum(criteria[attribute] * weight for criteria in submission.values())


def get_numeric_grade_per_grader(submission):
    weight = 0.25
    graders_total_grade = {}
    for criteria in submission.values():
        for grader, grade in criteria["num_grades"].items():
            if grader not in graders_total_grade:
                graders_total_grade[grader] = 0
            graders_total_grade[grader] += weight * grade
    return graders_total_grade


def get_letter_grade_per_grader(num_grades):
    return [get_letter_grade(num_grade) for num_grade in num_grades]


def format_table(rows, headers):
    values = [headers] + [[str(value) for value in row] for row in rows]
    widths = [max(len(row[index]) for row in values) for index in range(len(headers))]
    border = "+" + "+".join("-" * (width + 2) for width in widths) + "+"

    def format_row(row):
        return "|" + "|".join(f" {str(value):<{widths[index]}} " for index, value in enumerate(row)) + "|"

    lines = [border, format_row(headers), border]
    lines.extend(format_row(row) for row in rows)
    lines.append(border)
    return "\n".join(lines)

#------------------------------------------------------------------------------
# name: CFinalSubmissionGrader 
# desc: defines the codeevaluator - evaluates the assignment submissions
#------------------------------------------------------------------------------
class CFinalSubmissionGrader:
    def __init__(self):
        self.m_jsonsubmission = None 
        self.m_jsonrubric = None # stores a detail object of the rubic for evaluating the assignment 
        self.m_submissionnumber = -1
        self.m_stroutputpathfile = os.path.dirname(os.path.dirname(__file__))
        self.m_strdatapath = f"{self.m_stroutputpathfile}/data" 
        os.makedirs(f"{self.m_strdatapath}/compute_final_grade", exist_ok=True)
        self.m_stroutputpathfile = f"{self.m_strdatapath}/compute_final_grade/m_grades.json"
        print(f"CFinalSubmissionGrader :: __init__() - Success")
    # end __init__()   
        
    def create(self, strgradedsubmissionfilename, strrubicfilname):   
        # create the objects needed to do the evaluation       
        self.m_jsonsubmission = readJSONFromFilename(strgradedsubmissionfilename)
        self.m_jsonrubric = readJSONFromFilename(strrubicfilname)
        strfilename = os.path.basename(strgradedsubmissionfilename)         # Extracts '0.json'
        self.m_submissionnumber = int(strfilename.split('.')[0])
        # initailize the data to be ready to evaluate
        print(f"CFinalSubmissionGrader :: create() - {strgradedsubmissionfilename}")
        print(f"CFinalSubmissionGrader :: create() - {strrubicfilname}")
        print(f"CFinalSubmissionGrader :: create() - Success")
        return True    
    # end create()
    
    #---------------------------------------------------------------------------------------
    # name: computeMetaScoreFactor()
    # desc: returns true if the code is compilable and complete (not just a template copy) 
    # usage: computeScoreForCriteria([3,4,5,6], "program_correctness") 
    #---------------------------------------------------------------------------------------
    def computeMetaScoreFactor(self):
        # check if the 
        print('CFinalSubmissionGrader :: computeMetaScoreFactor() - self.m_jsonsubmission["metadata"]["complete"] = ', self.m_jsonsubmission["metadata"]["complete"])
        print('CFinalSubmissionGrader :: computeMetaScoreFactor() - self.m_jsonsubmission["metadata"]["compilable"] = ', self.m_jsonsubmission["metadata"]["compilable"])
        Dfactor = 0.49 if(self.m_jsonsubmission["metadata"]["compilable"] == 0.0) else 1.0
        print(f"CFinalSubmissionGrader :: computeMetaScoreFactor() - DFactor = {Dfactor}")
        metascore = 0.30 + (self.m_jsonsubmission["metadata"]["compilable"] + self.m_jsonsubmission["metadata"]["complete"] * Dfactor)
        print(f"CFinalSubmissionGrader :: computeMetaScoreFactor() = {metascore}")
        return metascore
    # end computeMetaScoreFactor()
    
    #--------------------------------------------------------------------------------------------------------
    # name: computeScoreForCriteria_Sum()
    # desc: computes the score for a given criteria by summing the scores and taking the average of them.
    # usage: computeScoreForCriteria([3,4,5,6], "program_correctness")
    #--------------------------------------------------------------------------------------------------------
    def computeScoreForCriteria_Sum(self, arrquestionindex=None, criteria_name="", op=""):
        if(arrquestionindex is None):
            return 0.0
        # end if
        score = 0.0
        count = 0
        for strsubmission, scores in self.m_jsonsubmission["filenames"].items():    
            print(scores)
            for index in arrquestionindex:
                try:
                    score += scores[index]      
                    count += 1
                # end try
                except:
                    continue
                # end except
            # end for
        # end for
        sumCriteria = (score / count) if(count > 0) else 0 
        print(f"CFinalSubmissionGrader :: computeScoreForCriteria_Sum() = {sumCriteria}")
        return sum 
    # end computeScoreForCriteria()

    #-------------------------------------------------------------------------------
    # name: computeScoreForCriteria_Or()
    # desc: computes the score for a given criteria using an or like operation 
    # usage: computeScoreForCriteria([3,4,5,6], "program_correctness") 
    #-------------------------------------------------------------------------------
    def computeScoreForCriteria_Or(self, arrquestionindex=None, criteria_name=""):
        if(arrquestionindex is None):
            return 0.0
        # end if
        score = 0.0
        count = 0
        nquestions = len(arrquestionindex)
        for question_index in arrquestionindex:
            or_score = 0.0
            for strsubmission, scores in self.m_jsonsubmission["filenames"].items():    
                question_score = scores[question_index]
                try:
                    if(or_score < question_score):
                        or_score = question_score
                    # end if
                    count += 1
                except:
                    continue
                # end except
            # end for
            score += or_score
            # end if
        # end for
        orCriteria = (score / nquestions) if(nquestions > 0) else 0
        print(f"CFinalSubmissionGrader :: computeScoreForCriteria_Or() = {orCriteria}")
        return orCriteria
    # end computeScoreForCriteria_Or()
    
    #-------------------------------------------------------------------------------------------
    # name: computeScoreForCriteria_MaxGrade()
    # desc: Computes the score for a given criteria looking at the maximum number of grades
    # usage: computeScoreForCriteria([3, 4, 5, 6], "program_correctness") 
    #-------------------------------------------------------------------------------------------
    def computeScoreForCriteria_MaxGrade(self, arrquestionindex=None, criteria_name="", op=""):
        if arrquestionindex is None or len(arrquestionindex) == 0:
            return 0.0  # No questions provided
        # end if
        
        # Initialize variables
        grades = {
            "A": [],
            "B": [],
            "C": [],
            "D": [],
            "F": []
        }
        
        # Populate the grades dictionary
        for question_index in arrquestionindex:
            for strsubmission, scores in self.m_jsonsubmission.get("filenames", {}).items():
                try:
                    question_score = scores[question_index]
                    question_let_score = get_letter_grade(question_score*100)
                    grades[question_let_score].append(question_score)
                # end try
                except (KeyError, IndexError):
                    # Handle cases where scores or indicfsg are missing
                    continue
                # end except
            # end for
        # end for
        
        # Find the grade with the maximum number of scores
        max_let_grade = ""
        max_num_grades = 0
        for let_grade, num_grades in grades.items():
            if len(num_grades) > max_num_grades:
                max_num_grades = len(num_grades)
                max_let_grade = let_grade
            # end if
        # end for
            
        # Return the maximum score from the most common grade
        maxCriteria = max(grades[max_let_grade]) if max_let_grade and grades[max_let_grade] else 0.0
        print(f"CFinalSubmissionGrader :: computeScoreForCriteria_MaxGrade() = {maxCriteria}")
        return maxCriteria
    # end computeScoreForCriteria_MaxGrade()

    #------------------------------------------------------------------------------------------- 
    # name: computeScoreForCriteria_Majority()  
    # desc: Computes the score for a given criteria looking at the majority of grades
    # usage: computeScoreForCriteria([3, 4, 5, 6], "program_correctness") 
    #-------------------------------------------------------------------------------------------
    def computeScoreForCriteria_Majority(self, arrquestionindex=None, criteria_name="", percentage_threshold=0.5, op=""):
        if arrquestionindex is None or len(arrquestionindex) == 0:
            return 0.0  # No questions provided
        # end if
        
        # Initialize variables
        grades = {
            "A": [],
            "B": [],
            "C": [],
            "D": [],
            "F": []
        }
        
        # Populate the grades dictionary
        for question_index in arrquestionindex:
            for strsubmission, scores in self.m_jsonsubmission.get("filenames", {}).items():
                try:
                    question_score = scores[question_index]
                    question_let_score = get_letter_grade(question_score * 100)
                    grades[question_let_score].append(question_score)
                except (KeyError, IndexError):
                    # Handle cases where scores or indices are missing
                    continue
                # end except
            # end for
        # end for
        
        metadata = self.m_jsonsubmission.get("metadata")
        
        # Compute total number of scores
        total_scores = sum(len(scores) for scores in grades.values())
        # majority_threshold = total_scores * percentage_threshold
        majority_threshold = total_scores * metadata["similarity_percentage"]
        
        # Accumulate grades to reach the majority
        majority_scores = []
        accumulated_count = 0
        for grade in sorted(grades.keys(), key=lambda g: len(grades[g]), reverse=True):
            scores = grades[grade]
            majority_scores.extend(scores)
            accumulated_count += len(scores)
            if accumulated_count >= majority_threshold:
                break
        # end for
        
        # Compute the average of the majority scores
        majority_average = sum(majority_scores) / len(majority_scores) if majority_scores else 0.0 
        print(f"CFinalSubmissionGrader :: computeScoreForCriteria_Majority() = {majority_average}") 
        return majority_average
    # end computeScoreForCriteria_MaxGrade()


    #-------------------------------------------------------------------------------------------
    # name: computeScoreForCriteria_Majority()
    # desc: Computes the score for a given criteria looking majority 
    # usage: computeScoreForCriteria([3, 4, 5, 6], "program_correctness") 
    #-------------------------------------------------------------------------------------------
    def computeScoreForCriteria_MaxGrade(self, arrquestionindex=None, criteria_name="", op=""):
        if arrquestionindex is None or len(arrquestionindex) == 0:
            return 0.0  # No questions provided
        # end if
        
        # Initialize variables
        grades = {
            "A": [],
            "B": [],
            "C": [],
            "D": [],
            "F": []
        }
        
        # Populate the grades dictionary
        for question_index in arrquestionindex:
            for strsubmission, scores in self.m_jsonsubmission.get("filenames", {}).items():
                try:
                    question_score = scores[question_index]
                    question_let_score = get_letter_grade(question_score*100)
                    grades[question_let_score].append(question_score)
                # end try
                except (KeyError, IndexError):
                    # Handle cases where scores or indices are missing
                    continue
                # end except
            # end for
        # end for
        
        # Find the grade with the maximum number of scores
        max_let_grade = ""
        max_num_grades = 0
        for let_grade, num_grades in grades.items():
            if len(num_grades) > max_num_grades:
                max_num_grades = len(num_grades)
                max_let_grade = let_grade
            # end if
        # end for
         
        # Return the maximum score from the most common grade
        maxgrade = max(grades[max_let_grade]) if max_let_grade and grades[max_let_grade] else 0.0
        print(f"CFinalSubmissionGrader :: computeScoreForCriteria_Majority() = {maxgrade}") 
        return maxgrade
    # end computeScoreForCriteria_MaxGrade()

    def computeScoreForCriteria(self, arrquestionindex=None, criteria_name="", op=""):
        print(f"CFinalSubmissionGrader :: computeScoreForCriteria()") 
        if(op == "max_grade"):
            return self.computeScoreForCriteria_MaxGrade(arrquestionindex, criteria_name)
        # end if
        elif(op == "or"):
            return self.computeScoreForCriteria_Or(arrquestionindex, criteria_name)
        # end elif
        elif(op == "majority"):
            return self.computeScoreForCriteria_Majority(arrquestionindex, criteria_name, percentage_threshold=0.6)     
        return self.computeScoreForCriteria_Sum(arrquestionindex, criteria_name)
    # end computeScoreForCriteria()
    
    #-------------------------------------------------------------------------------------
    # name: doEvaluation()
    # desc: runs an evaluation algorithm against the evlaution matrix of the submission
    #-------------------------------------------------------------------------------------
    def doEvaluation(self):
        if(self.m_jsonrubric is None):
            return 0.0
        # end if
        metascorefactor = self.computeMetaScoreFactor()
        jsonrubic = self.m_jsonrubric
        for criteria_name, criteria in jsonrubic.items():
            question_indices = criteria["questions"] 
            evaluation_op = criteria["op"]
            score = self.computeScoreForCriteria(question_indices, criteria_name, evaluation_op)
            if(evaluation_op == "or" and self.m_jsonsubmission["metadata"]["compilable"] == 0.0):
                score = 0.49
            # end if
            if(self.m_jsonsubmission["metadata"]["complete"] == 0.0):
                criteria["grade"] = 0.30 * 100 # if the submission is not complete or similar to template give a score of 30
            else:
                criteria["grade"] = score * 100
            criteria["compilable"] = self.m_jsonsubmission["metadata"]["compilable"] 
            criteria["complete"] = self.m_jsonsubmission["metadata"]["complete"] 
            criteria["similarity_percentage"] = self.m_jsonsubmission["metadata"]["similarity_percentage"] 
            criteria["percentage_of_nontemplate_files"] = self.m_jsonsubmission["metadata"]["percentage_of_nontemplate_files"] 
            criteria["percentage_of_template_files"] = self.m_jsonsubmission["metadata"]["percentage_of_template_files"]    
            # end else 
        # end for
        # generate the table 
        total_score = self.generate_table(jsonrubic)   
        # save the machine score to the grades file
        self.save_rubic_scores()
        return total_score       
    # end doEvaluation()
        
    #----------------------------------------------------------------------------------------
    # name: generate_table()
    # desc: Function to generate the table and calculate total score from JSON input
    #----------------------------------------------------------------------------------------
    def generate_table(self, jsonrubic):
        # Create a list of rows for tabulation
        rows = []
        for name, criteria in jsonrubic.items():
            rows.append([
                name,
                float(criteria["weight"]),
                float(criteria["grade"]),
                float(criteria["weight"]) * float(criteria["grade"])  # Calculate weighted score per row
            ])
        # end for
        
        # Add a header to the table
        headers = ["Name", "Weight", "Grade", "Weighted Score"]
        
        # Calculate the total score
        total_score = sum(row[3] for row in rows)  # Sum the weighted scores
        
        print("=" * 80)
        print(f"Table for Submission ({self.m_submissionnumber})")
        print("=" * 80)

        # Print the formatted table
        print(format_table(rows, headers))
        
        # Print the total score separately
        print(f"Total Score (Machine): {total_score:.2f}")
         
        return total_score
    # end generate_table()
        
    #------------------------------------------------------------------------------------
    # name: save_rubic_scores()
    # desc: saves the rubic scores of each assignment and submittals to a json file
    #------------------------------------------------------------------------------------
    def save_rubic_scores(self):
        #strpath = os.path.dirname(os.path.dirname(__file__))
        #strgradesfilename = f"{strpath}/data/m_grades.json"
        jsongrades = None
        jsonrubic = self.m_jsonrubric
        strsubmissionnumber = str(self.m_submissionnumber)
        # open the json file to update or create
        try:
            # read the json file 
            jsongrades = readJSONFromFilename(self.m_stroutputpathfile)
        # end try
        except:
            jsongrades = {}
        # end except
        
        if(strsubmissionnumber not in jsongrades):
            jsongrades[strsubmissionnumber] = {}
        # end if
        
        # fill up the data
        for criteria, item in jsonrubic.items():
            if(criteria not in jsongrades[strsubmissionnumber]):
                jsongrades[strsubmissionnumber][criteria] = {}
            # end if
            
            # save the value of the data    
            jsongrades[strsubmissionnumber][criteria]["avg_num_grade"] = item["grade"]
            jsongrades[strsubmissionnumber][criteria]["avg_num_grade"] = item["grade"]
            jsongrades[strsubmissionnumber][criteria]["compilable"] = item["compilable"] 
            jsongrades[strsubmissionnumber][criteria]["complete"] = item["complete"] 
            jsongrades[strsubmissionnumber][criteria]["similarity_percentage"] = item["similarity_percentage"] 
            jsongrades[strsubmissionnumber][criteria]["percentage_of_nontemplate_files"] = item["percentage_of_nontemplate_files"] 
            jsongrades[strsubmissionnumber][criteria]["percentage_of_template_files"] = item["percentage_of_template_files"]    
        # end for
        
        # write the data back out to the file
        writeJSONToFilename(self.m_stroutputpathfile, jsongrades)
    # end save_rubic_scores()

    # used by the agent do get documents for answering questions
    def get_java_assignment_summaries(self, strquery):
        return self.m_cllm._chain(strquery, self.m_csvdbsubmissions.query(strquery))
    # end get_java_assignment_summaries() 
# end CFinalSubmissionGrader

#-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# name: evaluate()
# desc: evaluates by using the test suite and rubic to evaluate the folder containing SUTs
# usage: python.exe ./src/compute_final_grade.py main "C:/Users/klewi/Desktop/cautograder/data/grade_submission/0.json" "C:/Users/klewi/Desktop/cautograder/data/rubic/markingRubric/rubric.json"
#-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
def main(strsubmissionfilename, strrubicfilename, params=None):
    cfsg = CFinalSubmissionGrader()
    if(cfsg.create(strsubmissionfilename, strrubicfilename)):
        num_grade = cfsg.doEvaluation()
        let_grade = get_letter_grade(int(num_grade))
        
        print(f"Machine Grade: {num_grade} - {let_grade}" )

        hjsongrades = readJSONFromFilename(f"{cfsg.m_strdatapath}/convert_grade_file_from_csv_to_json/grades.json")  # Get the human grade
        hsubmission_key = f"{cfsg.m_submissionnumber}"
        if hsubmission_key not in hjsongrades:
            print(f"[WARNING] No human grade found for submission {hsubmission_key}. Skipping human grade comparison.")
            return let_grade

        hsubmission_grade = hjsongrades[hsubmission_key]
        total_hscore = get_numeric_grade(hsubmission_grade)
        total_hscore_per_grader = get_numeric_grade_per_grader(hsubmission_grade)
        letter_hscore_per_grader = get_letter_grade_per_grader(total_hscore_per_grader.values())
        print(total_hscore_per_grader)
        print(f"Human Average Grade: {total_hscore:.2f}")
        print("Human graders numeric scores: ", total_hscore_per_grader)
        print("Human graders letter scores: ", letter_hscore_per_grader)
        return let_grade
    # end if
    return None
# end evaluate()

#----------------------------------------
# main entry point
#----------------------------------------
if __name__ == "__main__":
    if fire is not None:
        fire.Fire()
    else:
        if len(sys.argv) < 2 or sys.argv[1] != "main":
            print(
                "Usage: compute_final_grade.py main "
                "<graded_submission_file> <rubric_file>"
            )
            sys.exit(1)
        main(*sys.argv[2:])
# end if
