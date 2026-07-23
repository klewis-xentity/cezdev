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
        print(f"[Setup] Output grades file: {self.m_stroutputpathfile}")
    # end __init__()   
        
    def create(self, strgradedsubmissionfilename, strrubicfilname):   
        # create the objects needed to do the evaluation       
        self.m_jsonsubmission = readJSONFromFilename(strgradedsubmissionfilename)
        self.m_jsonrubric = readJSONFromFilename(strrubicfilname)
        strfilename = os.path.basename(strgradedsubmissionfilename)         # Extracts '0.json'
        self.m_submissionnumber = int(strfilename.split('.')[0])
        # initailize the data to be ready to evaluate
        print(f"[Setup] Loaded graded submission: {strgradedsubmissionfilename}")
        print(f"[Setup] Loaded rubric:            {strrubicfilname}")
        print(f"[Setup] Submission #{self.m_submissionnumber} is ready for grading")
        return True    
    # end create()
    
    #---------------------------------------------------------------------------------------
    # name: computeMetaScoreFactor()
    # desc: returns true if the code is compilable and complete (not just a template copy) 
    # usage: computeScoreForCriteria([3,4,5,6], "program_correctness") 
    #---------------------------------------------------------------------------------------
    def computeMetaScoreFactor(self):
        # check if the 
        complete = self.m_jsonsubmission["metadata"]["complete"]
        compilable = self.m_jsonsubmission["metadata"]["compilable"]
        print("[Meta Score] Evaluating submission metadata factors:")
        print(f"[Meta Score]   complete   = {complete}  (1.0 = full submission, 0.0 = template copy)")
        print(f"[Meta Score]   compilable = {compilable}  (1.0 = compiles, 0.0 = does not compile)")
        Dfactor = 0.49 if(compilable == 0.0) else 1.0
        print(f"[Meta Score]   completeness weight (Dfactor) = {Dfactor}")
        metascore = 0.30 + (compilable + complete * Dfactor)
        print(f"[Meta Score]   => meta score factor = {metascore:.3f}")
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
        print(f"[Criteria]   '{criteria_name}' (sum/average) score = {sumCriteria:.4f}")
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
        print(f"[Criteria]   '{criteria_name}' (or) score = {orCriteria:.4f}")
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
        print(f"[Criteria]   '{criteria_name}' (max grade) score = {maxCriteria:.4f}")
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
        print(f"[Criteria]   '{criteria_name}' (majority) score = {majority_average:.4f}") 
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
        print(f"[Criteria]   '{criteria_name}' (max grade) score = {maxgrade:.4f}") 
        return maxgrade
    # end computeScoreForCriteria_MaxGrade()

    def computeScoreForCriteria(self, arrquestionindex=None, criteria_name="", op=""):
        method = op if op else "sum"
        print(f"[Criteria] Scoring '{criteria_name}' using '{method}' method on questions {arrquestionindex}")
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
        print()
        metascorefactor = self.computeMetaScoreFactor()
        jsonrubic = self.m_jsonrubric
        print()
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
            print()
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
        
        # Print the formatted table
        print(format_table(rows, headers))
        
        # Show the formula and the step-by-step computation of the final grade
        print()
        print("Final Grade Formula:")
        print("  Total Score = Sum of ( weight x grade ) for each criteria")
        print()
        print("Computation:")
        terms = []
        for name, weight, grade, weighted in rows:
            print(f"  {name:<20} : {weight:.2f} x {grade:6.2f} = {weighted:6.2f}")
            terms.append(f"{weighted:.2f}")
        print("  " + "-" * 44)
        print(f"  Total Score (Machine) = {' + '.join(terms)} = {total_score:.2f}")
        print()

        # Show the numeric-to-letter grade reference table
        print("Numeric to Letter Grade Reference:")
        print("  +-------------------+--------+")
        print("  | Numeric Range     | Letter |")
        print("  +-------------------+--------+")
        print("  | 80 - 100          |   A    |")
        print("  | 70 - 79           |   B    |")
        print("  | 60 - 69           |   C    |")
        print("  | 50 - 59           |   D    |")
        print("  |  0 - 49           |   F    |")
        print("  +-------------------+--------+")
        print("=" * 80)
         
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
        
        print()
        print(f"Machine Grade: {num_grade} - {let_grade}" )
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
