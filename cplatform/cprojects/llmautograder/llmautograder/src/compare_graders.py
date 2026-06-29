#------------------------------------------------------------------
# file: compare_graders.py
# desc: compares the machine computed grades of submission
#       with the human grades of the managerie dataset, including
#       letter-grade computation
# usage: python compare_graders.py main C:/Users/klewi/Desktop/cautograder/data/submission_ids/submission_ids_test.txt
#-------------------------------------------------------------------
import fire
import os
import csv
from difflib import SequenceMatcher
from c3dclassessdk.ccore.cutility.cutility import readJSONFromFilename, writeJSONToFilename, readTextFromFilename
from c3dclassessdk.ccore.cutility.cgrade import (
    compute_submission_numeric_grade,
    compute_submission_numeric_grade_per_grader,
    get_let_grade,
)
from tabulate import tabulate  # Use the tabulate library for table formatting
from collections import Counter

#------------------------------------------------
# name: is_within_grades()
# desc:
#------------------------------------------------
def is_within_grades2(mgrades, hgrades):
    return mgrades >= min(hgrades) and mgrades <= max(hgrades)
# end is_within_grades2()

#------------------------------------------------
# name: is_within_grades()
# desc: Checks if the rounded value of mgrades is 
#       within the range of rounded hgrades.
#------------------------------------------------
def is_within_grades(mgrades, hgrades):
    # Round the grades to the nearest whole number
    rounded_mgrades = round(mgrades)
    rounded_hgrades = [round(hgrade) for hgrade in hgrades]

    # Check if the rounded mgrades is within the 
    # range of rounded hgrades
    return rounded_mgrades >= min(rounded_hgrades) and rounded_mgrades <= max(rounded_hgrades)
# end is_within_grades()

def collect_human_feedback(hsubmissiondata):
    feedback = []
    for criteria in hsubmissiondata.values():
        for comment in criteria.get("human_feedback", criteria.get("feedback", {})).values():
            comment = str(comment).strip()
            if comment:
                feedback.append(comment)
            # end if
        # end for
    # end for
    return " ".join(feedback)
# end collect_human_feedback()

def collect_machine_feedback(strdatapath, sid):
    strsubmissionfilename = f"{strdatapath}/grade_submission/{sid}.json"
    if not os.path.exists(strsubmissionfilename):
        return ""
    # end if
    submission = readJSONFromFilename(strsubmissionfilename)
    feedback = []
    for comment in submission.get("machine_feedback", {}).values():
        comment = str(comment).strip()
        if comment:
            feedback.append(comment)
        # end if
    # end for
    return " ".join(feedback)
# end collect_machine_feedback()

def compute_feedback_similarity(human_feedback, machine_feedback):
    if not human_feedback or not machine_feedback:
        return 0.0
    # end if
    return SequenceMatcher(None, human_feedback.lower(), machine_feedback.lower()).ratio()
# end compute_feedback_similarity()

#----------------------------------------------------------------------
# name: main()
# desc: the main program
#----------------------------------------------------------------------
def main(strsubmissionidfilename):
    
    # Save the table (with letter grades) to a NEW CSV file
    stroutputpathfile = os.path.dirname(os.path.dirname(__file__))
    strdatapath = f"{stroutputpathfile}/data" 
    os.makedirs(f"{strdatapath}/compare_graders", exist_ok=True)
    strhmjsonfilename = f"{strdatapath}/convert_grade_file_from_csv_to_json/grades.json"
    straijsonfilename = f"{strdatapath}/compute_final_grade/m_grades.json"
    strsubmissionidfilename = f"{strdatapath}/submission_ids/{strsubmissionidfilename}"
    
    # Load JSON data and get grades
    hjsondata = readJSONFromFilename(strhmjsonfilename)  # human (managerie) grades
    mjsondata = readJSONFromFilename(straijsonfilename)  # machine (LLM) grades
    strsubmissionids = readTextFromFilename(strsubmissionidfilename)
    submissionids = list(map(str, strsubmissionids.split()))
    
    if not submissionids:
        print("No submission IDs found.")
        return

    # Determine number of graders from the first submission
    first_submission_id = submissionids[0]
    first_hgrades = compute_submission_numeric_grade_per_grader(hjsondata[first_submission_id])
    num_graders = len(first_hgrades)

    # Build CSV headers
    # We'll have:
    #   Submission ID
    #   Machine Grade, Machine Letter
    #   Human Grade 1, Human Grade 1 Letter, ...
    #   Within Human Grades?, Any D Grade?
    #headers = ["Submission ID", "MG", "ML"]
    headers = ["Submission ID", "Machine Grade"]
    for i in range(num_graders):
        headers.append(f"Human Grade {i+1}")
        ##headers.append(f"HGL {i+1}")
    headers += ["Within Human Grades?", "Feedback Similarity"]
    ##headers += ["Within Human Grades?", "Any D Grade?"]
    ##headers += ["Compiles?", "T-Similarity", "T-Percentage"]
    
    # We'll store rows for tabulate (for printing) as well as for CSV
    table = []
    feedback_table = []

    count_within_hgrades = 0
    count_d_grades = 0

    for sid in submissionids:
        sid = sid.strip()
        submissiondata = mjsondata[sid]
        submissionmetadata = submissiondata["program_correctness"]
        
        # Numeric grades
        mgrade_num = compute_submission_numeric_grade(mjsondata[sid])
        hgrades_num = compute_submission_numeric_grade_per_grader(hjsondata[sid])

        # Round numeric grades
        mgrade_num_rounded = round(mgrade_num)
        hgrades_num_rounded = [round(hg) for hg in hgrades_num]

        # Letter grades
        mgrade_letter = get_let_grade(mgrade_num_rounded)
        hgrades_letters = [get_let_grade(hg) for hg in hgrades_num_rounded]

        # Check if machine grade is within range of human grades
        bwithinhumangrades = is_within_grades(mgrade_num_rounded, hgrades_num_rounded)
        if bwithinhumangrades:
            count_within_hgrades += 1

        # Check if any human grader gave a D (40-49)
        banydgrade = any(40 <= h <= 49 for h in hgrades_num_rounded)
        if banydgrade:
            count_d_grades += 1
        # end if

        human_feedback = collect_human_feedback(hjsondata[sid])
        machine_feedback = collect_machine_feedback(strdatapath, sid)
        feedback_similarity = compute_feedback_similarity(human_feedback, machine_feedback)

        # Build a row with:
        # [SID, MNumeric, MLetter, HNumeric1, HLetter1, HNumeric2, HLetter2, ..., Within?, AnyD?]
        #row = [sid, mgrade_num_rounded, mgrade_letter]
        row = [sid, mgrade_num_rounded]
        for hnum, hlet in zip(hgrades_num_rounded, hgrades_letters):
            row.append(hnum)
            #row.append(hlet)
        row.append(bwithinhumangrades)
        row.append(round(feedback_similarity, 3))
        #row.append(banydgrade)
        
        #row.append(submissionmetadata["compilable"])
        #row.append(submissionmetadata["similarity_percentage"])
        #row.append(submissionmetadata["percentage_of_template_files"])
        
        """
        "metadata": {
        "compilable": 1.0,
        "complete": 0.0,
        "similarity_percentage": 1.0,
        "number_template_files": 10,
        "number_nontemplate_files": 0,
        "number_sumbitted_files": 10,
        "percentage_of_template_files": 1.0,
        "percentage_of_nontemplate_files": 0.0
        """
        
        table.append(row)
        feedback_table.append([
            sid,
            round(feedback_similarity, 3),
            machine_feedback,
            human_feedback
        ])

    # Print a nicely formatted table
    print(tabulate(table, headers=headers, tablefmt="grid"))

    # Print some summary stats
    total_submissions = len(submissionids)
    print(f"\nPercentage of submissions within human grades: "
          f"{count_within_hgrades / total_submissions * 100:.2f}%")
    print(f"Percentage of submissions with any D grade: "
          f"{count_d_grades / total_submissions * 100:.2f}%")       
    
    output_csv_filename = f"{strdatapath}/compare_graders/grades_evaluation_with_letters.csv"
    with open(output_csv_filename, mode="w", newline="", encoding="utf-8") as file:
        writer = csv.writer(file)
        writer.writerow(headers)
        writer.writerows(table)

    print(f"\nTable data (with letter grades) saved to {output_csv_filename}")

    feedback_csv_filename = f"{strdatapath}/compare_graders/feedback_comparison.csv"
    with open(feedback_csv_filename, mode="w", newline="", encoding="utf-8") as file:
        writer = csv.writer(file)
        writer.writerow(["Submission ID", "Feedback Similarity", "Machine Feedback", "Human Feedback"])
        writer.writerows(feedback_table)
    # end with

    print(f"Feedback comparison saved to {feedback_csv_filename}")


#----------------------------------------------
# name: __main__
# desc: entry point
#----------------------------------------------
if __name__ == "__main__":
    fire.Fire()
# end if
