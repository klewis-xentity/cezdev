#-------------------------------------------------------------------------------------
# file: ccodeevaluator.py
# desc: defines an object that evaluate code that defines an sut 
#       with code that defines a test case and outputs feedback   
# usage: cevalcode hw_5.pdf hw_5_1.py customer_accounts_test.py rubic.json
#       cevalcode hw_5.pdf hw_5_1.py customer_accounts_test.py rubic.json
#-------------------------------------------------------------------------------------
import fire
import os
from cllm.cllm import CLLM
from cllm.cvectordatabase import CSimpleVectorDatabase
from langchain.tools import Tool
from cutility.cutility import extractTextFromFilename, readJSONFromFilename, writeJSONToFilename, writeTextToFilename
from cutility.cgrade import get_numeric_grades, get_letter_grade, get_numeric_grade, get_numeric_grade_per_grader, get_letter_grade_per_grader, get_let_grade, get_num_grade
import pandas as pd
from tabulate import tabulate

#------------------------------------------------------------------------------
# name: CCodeEvaluator 
# desc: defines the codeevaluator - evaluates the assignment submissions
#------------------------------------------------------------------------------
class CEvaluateSubmissions:
    def __init__(self, params=None):
        self.m_jsonsubmission = None 
        self.m_strsubmission = None
        self.m_jsonrubric = None # stores a detail object of the rubic for evaluating the assignment 
        self.m_strrubic = None
        self.m_strcachepath = "" # where the rubic file and submissions are stored
        self.m_submissionnumber = ""
    # end __init__()   
        
    def create(self, strassignmentpath, strsubmissionnumber, strrubicfilename):   
        if not os.path.exists(strassignmentpath):
            return False
        # end if
        self.m_strcachepath = f"{strassignmentpath}/.cache"
        if not os.path.exists(strassignmentpath):
            return False
        # end if
        strsubmissiondatafilename =  f"{self.m_strcachepath}/submissions/{strsubmissionnumber}/submission.json"
        if not os.path.exists(strsubmissiondatafilename):
            return False
        # end if
        strrubicdatafilename =  f"{self.m_strcachepath}/rubic/{strrubicfilename}/rubric.json"
        if not os.path.exists(strrubicdatafilename):
            return False
        # end if
        # create the objects needed to do the evaluation       
        self.m_jsonsubmission = readJSONFromFilename(strsubmissiondatafilename)
        self.m_jsonrubric = readJSONFromFilename(strrubicdatafilename)
        self.m_submissionnumber = strsubmissionnumber
        # initailize the data to be ready to evaluate
        return True    
    # end create()
    
    #---------------------------------------------------------------------------------------
    # name: isCompleteAndCompilable()
    # desc: returns true if the code is compilable and complete (not just a template copy) 
    # usage: computeScoreForCriteria([3,4,5,6], "program_correctness") 
    #---------------------------------------------------------------------------------------
    def computeMetaScoreFactor(self):
        # check if the 
        print('self.m_jsonsubmission["metadata"]["complete"]', self.m_jsonsubmission["metadata"]["complete"])
        print('self.m_jsonsubmission["metadata"]["compilable"]', self.m_jsonsubmission["metadata"]["compilable"])
        
        Dfactor = 0.49 if(self.m_jsonsubmission["metadata"]["compilable"] == 0.0) else 1.0
        return 0.30 + (self.m_jsonsubmission["metadata"]["compilable"] + self.m_jsonsubmission["metadata"]["complete"] * Dfactor)
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
        return (score / count) if(count > 0) else 0 
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
        return (score / nquestions) if(nquestions > 0) else 0
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
                    question_let_score = get_let_grade(question_score*100)
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
        
        print("grades:", grades)
        
        # Return the maximum score from the most common grade
        return max(grades[max_let_grade]) if max_let_grade and grades[max_let_grade] else 0.0
    # end computeScoreForCriteria_MaxGrade()


    #------------------------------------------------------------------------------------------- 
    # name: computeScoreForCriteria_MaxGrade()  
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
                    question_let_score = get_let_grade(question_score * 100)
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
                    question_let_score = get_let_grade(question_score*100)
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
        
        print("grades:", grades)
        
        # Return the maximum score from the most common grade
        return max(grades[max_let_grade]) if max_let_grade and grades[max_let_grade] else 0.0
    # end computeScoreForCriteria_MaxGrade()


    def computeScoreForCriteria(self, arrquestionindex=None, criteria_name="", op=""):
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
        
        #print('self.m_jsonsubmission["metadata"]["complete"]', self.m_jsonsubmission["metadata"]["complete"])
        #print('self.m_jsonsubmission["metadata"]["compilable"]', self.m_jsonsubmission["metadata"]["compilable"])
        #Dfactor = 0.49 if(self.m_jsonsubmission["metadata"]["compilable"] == 0.0) else 1.0
        #return 0.30 + (self.m_jsonsubmission["metadata"]["compilable"] + self.m_jsonsubmission["metadata"]["complete"] * Dfactor)
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
        print(tabulate(rows, headers=headers, tablefmt="grid")) 
        
        # Print the total score separately
        print(f"Total Score (Machine): {total_score:.2f}")
         
        return total_score
    # end generate_table()
        
    #------------------------------------------------------------------------------------
    # name: save_rubic_scores()
    # desc: saves the rubic scores of each assignment and submittals to a json file
    #------------------------------------------------------------------------------------
    def save_rubic_scores(self):
        strspecfilename = self.m_strcachepath
        strpath = os.path.dirname(strspecfilename)
        strgradesfilename = f"{strpath}/m_grades.json"
        print(strgradesfilename)
        jsongrades = None
        jsonrubic = self.m_jsonrubric
        strsubmissionnumber = str(self.m_submissionnumber)
        
        # open the json file to update or create
        try:
            # read the json file 
            jsongrades = readJSONFromFilename(strgradesfilename)
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
        writeJSONToFilename(strgradesfilename, jsongrades)
    # end save_rubic_scores()

    # used by the agent do get documents for answering questions
    def get_java_assignment_summaries(self, strquery):
        return self.m_cllm._chain(strquery, self.m_csvdbsubmissions.query(strquery))
    # end get_java_assignment_summaries() 
# end CEvaluateSubmissions

#-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
# name: evaluate()
# desc: evaluates by using the test suite and rubic to evaluate the folder containing SUTs
# usage: python.exe .\cevaluatesubmission\cevaluatesubmission.py evaluate .\cevaluatesubmission\example.pdf .\cevaluatesubmission\contact.py .\cevaluatesubmission\contacttest.py .\cevaluatesubmission\rubic.pdf 
#-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
def evaluate(strassignmentpath, strsubmissionnumber, strrubicfilename, params=None):
    ces = CEvaluateSubmissions(params)
    if(ces.create(strassignmentpath, strsubmissionnumber, strrubicfilename)):
        num_grade = ces.doEvaluation()
        let_grade = get_letter_grade(int(num_grade))
        hjsongrades = readJSONFromFilename("C:/Users/klewi/Desktop/assignment_3/grades2.json")  # Get the human grade
        hsubmission_grade = hjsongrades[f"{ces.m_submissionnumber}"]
        total_hscore = get_numeric_grade(hsubmission_grade)
        total_hscore_per_grader = get_numeric_grade_per_grader(hsubmission_grade)
        letter_hscore_per_grader = get_letter_grade_per_grader(total_hscore_per_grader.values())
        print(total_hscore_per_grader)
        print(f"Machine Grade: {num_grade} - {let_grade}" )
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
    fire.Fire()
# end if