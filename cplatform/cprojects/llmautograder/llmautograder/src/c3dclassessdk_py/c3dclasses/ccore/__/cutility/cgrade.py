import tabulate

#-----------------------------------------------------------
# name: compute_grade_step_difference() 
# desc: returns the grade step difference
#-----------------------------------------------------------
def compute_grade_step_difference(grades):
    # Define the letter-to-number mapping
    grade_mapping = {'A': 4, 'B': 3, 'C': 2, 'D': 1, 'F': 0}
    
    # Convert the letter grades to their numeric equivalents
    numeric_grades = [grade_mapping[grade] for grade in grades]
    
    # Calculate the step difference between the highest and lowest grades
    step_difference = max(numeric_grades) - min(numeric_grades)
    
    return step_difference
# end grade_step_difference()

#---------------------------------------------------------------
# name: agreement_percentage()
# desc: computes an aggrement percentage 
#---------------------------------------------------------------
def agreement_percentage(target_grade, grades):
    # Count the occurrences of the target grade in the list
    count = grades.count(target_grade)
    
    # Calculate the percentage of agreement
    percentage = (count / len(grades)) * 100
    
    return percentage
# end agreement_percentage()

#---------------------------------------------------------------
# name: group_agreement() 
# desc: determines the group agreement percentage
#---------------------------------------------------------------
def group_agreement(grades):
    # Count the occurrences of each grade
    grade_counts = Counter(grades)
    
    # Find the frequency of the most common grade
    most_common_count = max(grade_counts.values())
    
    # Calculate the percentage of agreement
    percentage = (most_common_count / len(grades)) * 100
    
    return percentage
# end group_agreement()

#----------------------------------------------------------
# name: get_grade_step_diff()
# desc: returns the step difffernce in the grades
#----------------------------------------------------------
def group_hjsondata_by_grade_step_diff(hjsondata):
    hjsondata_by_step_diff = {}
    weight = 0.25
    for num, submission in hjsondata.items():
        submission_num_grade = [0,0,0,0]
        for criteria in submission.values():
            i = 0
            
            # skip groups of grades greater than 4
            if(len(criteria['num_grades']) > 4):
                continue
            # end if
            
            # check if it contains any NAN values    
            for num_grade in criteria['num_grades']:  
                submission_num_grade[i] += weight * num_grade
                i += 1
            # end for
        # end for
        submission_let_grade = [get_letter_grade(numgrade) for numgrade in submission_num_grade]
        step_diff = compute_grade_step_difference(submission_let_grade)
        if(step_diff not in hjsondata_by_step_diff):
            hjsondata_by_step_diff[step_diff] = {}
        #if(num not in hjsondata_by_step_diff[step_diff]):
        hjsondata_by_step_diff[step_diff][num]=submission
    # end for    
    return hjsondata_by_step_diff
# end group_hjsondata_by_grade_step_diff()
        
#----------------------------------------------------------
# name: get_letter_grade()
# desc: returns a letter grade for a numeric grade
# scale:
#       80-100 - A+, A++
#       70-79  - A
#       60-69  - B
#       50-59  - C
#       40-49  - D
#       0-39   - F
#----------------------------------------------------------
def get_letter_grade(num_grade):
    if 80 <= num_grade <= 100:
        return 'A'
    elif 70 <= num_grade < 80:
        return 'B'
    elif 60 <= num_grade < 70:
        return 'C'
    elif 50 <= num_grade < 60:
        return 'D'
    else:
        return 'F'
# end get_letter_grade()

#------------------------------------------------------------------------
# name: get_numeric_grades()
# desc: returns the numberic grade of all the submission passed in
#------------------------------------------------------------------------
def get_numeric_grades(jsonsubmissionsdata, attribute="avg_num_grade"):
    grades = {}
    for num, submission in jsonsubmissionsdata.items():
        grades[num] = get_numeric_grade(submission, attribute)
    # end for
    return grades
# end get_numeric_grades()

#------------------------------------------------------------------------
# name: get_numeric_grade() 
# desc: returns the numeric grade of a single submission
#------------------------------------------------------------------------
def get_numeric_grade(submission, attribute="avg_num_grade"):
    weight = 0.25
    return sum(criteria[attribute] * weight for criteria in submission.values())
# end get_numeric_grade()


#------------------------------------------------------------------------
# name: get_numeric_grade() 
# desc: returns the numeric grade of a single submission
#------------------------------------------------------------------------
def get_numeric_grade_per_grader(submission):
    weight = 0.25
    graders_total_grade = {}
    for criteria_name, criteria in submission.items():
        for grader, grade in criteria["num_grades"].items():
            if(grader not in graders_total_grade):
                graders_total_grade[grader] = 0
            # end if
            graders_total_grade[grader] += weight * grade
        # end for
    # end for
    return graders_total_grade
# end get_numeric_grade()

def get_letter_grade_per_grader(num_grades):
    return [get_letter_grade(num_grade) for num_grade in num_grades]
# end get_numeric_grade_per_grader()


#--------------------------------------------------------------------------------------------
# name: print_table_of_mgrade_to_hgrade_step_groups()
# desc: prints a table comparing the percentage of mgrades match with hgrade step groups, 
#       with one table per step group.
#--------------------------------------------------------------------------------------------
def print_table_of_mgrade_to_hgrade_step_groups(hjsondata_by_steps, mjsondata):
    # Loop through each step group to create separate tables
    for step, hsubmissions in hjsondata_by_steps.items():
        table_data = []  # Store rows of data to print for each step group
        
        # Loop through machine submissions
        for mnum, msubmission in mjsondata.items():
            m_num_grade = get_numeric_grade(msubmission)
            m_let_grade = get_letter_grade(m_num_grade)
            
            if mnum in hsubmissions:
                # Calculate human grades and agreement
                submission_num_grades = compute_submission_numeric_grade_per_grader(hsubmissions[mnum])
                submission_let_grades = [get_letter_grade(grade) for grade in submission_num_grades]
                
                # Calculate machine agreement and group agreement
                machine_agreement = agreement_percentage(m_let_grade, submission_let_grades)
                group_agreement_percentage = group_agreement(submission_let_grades)
                
                # Add row data to the table
                table_data.append([
                    mnum,
                    m_let_grade,
                    submission_let_grades,
                    f"{machine_agreement:.2f}%",
                    f"{group_agreement_percentage:.2f}%"
                ])
        
        # Print the table for this step group if there is data
        if table_data:
            headers = ["Submission #", "Machine Grade", "Human Grades", "Machine Agreement %", "Group Agreement %"]
            print(f"\nTable for Step {step}:")
            print(tabulate(table_data, headers=headers, tablefmt="grid"))

# end print_table_of_mgrade_to_hgrade_step_groups

#--------------------------------------------------------------------------------------------
# name: print_table_of_mgrade_to_hgrade_step_groups()
# desc: prints a table comparing the percentage of mgrades match with hgrade step groups
#--------------------------------------------------------------------------------------------
def print_table_of_mgrade_to_hgrade_step_groups2(hjsondata_by_steps, mjsondata):
    table_data = []  # Store rows of data to print
    
    # Loop through machine submissions
    for mnum, msubmission in mjsondata.items():
        m_num_grade = get_numeric_grade(msubmission)
        m_let_grade = get_letter_grade(m_num_grade)
        
        # Loop through human step submissions
        for step, hsubmissions in hjsondata_by_steps.items():
            if mnum in hsubmissions:
                
                # Calculate human grades and agreement
                submission_num_grades = compute_submission_numeric_grade_per_grader(hsubmissions[mnum])
                submission_let_grades = [get_letter_grade(grade) for grade in submission_num_grades]                
                machine_agreement = agreement_percentage(m_let_grade, submission_let_grades)
                group_agreement_percentage = group_agreement(submission_let_grades)
                
                # Add row data to the table
                table_data.append([
                    mnum,
                    step,
                    m_let_grade,
                    submission_let_grades,
                    f"{machine_agreement:.2f}%",
                    f"{group_agreement_percentage:.2f}%"
                ])
    
    # Define headers for the table
    headers = ["Submission #", "Step", "Machine Grade", "Human Grades", "Machine Agreement %", "Group Agreement %"]
    
    # Print the table using tabulate
    print(tabulate(table_data, headers=headers, tablefmt="grid"))

# end print_table_of_mgrade_to_hgrade_step_groups

#--------------------------------------------------------------------------------------------
# name: print_table_of_mgrade_to_hmedgrade()
# desc: prints a table showing the percentage mgrades match hmedgrades (number, letter grade) 
#--------------------------------------------------------------------------------------------
def print_table_of_mgrade_to_hmedgrade(hjsondata, mgrades_within_hgrades):
    # Get the data
    hmedgrades = get_numeric_grades(hjsondata, "med_num_grade")

    # Initialize counters and table data
    count_agree = 0
    table_data = []
    
    for key in mgrades_within_hgrades.keys():
        mgrade = mgrades_within_hgrades[key]
        hmedgrade = hmedgrades.get(key, "N/A")

        # Skip if there's no corresponding human grade
        if hmedgrade == "N/A":
            continue
        
        # Convert numerical grades to letter grades
        m_letter = get_letter_grade(mgrade)
        hmed_letter = get_letter_grade(hmedgrade)

        # Check for agreement
        if m_letter == hmed_letter:
            count_agree += 1
        
        # Append to table data
        table_data.append([
            key,
            mgrade,
            m_letter,
            hmedgrade,
            hmed_letter,
            "Yes" if m_letter == hmed_letter else "No"
        ])
    # end for
    
    # Calculate the percentage of agreement
    total_valid_grades = len(table_data)
    percentage_agreement = (count_agree / total_valid_grades) * 100 if total_valid_grades > 0 else 0
    
    # Print the table
    headers = ["ID", "M Grade", "M Letter Grade", "H Median Grade", "H Letter Grade", "Match"]
    print(tabulate(table_data, headers=headers, tablefmt="grid"))
    
    # Print the percentage of agreement
    print(f"\nPercentage of matching letter grades: {percentage_agreement:.2f}%")
# end print_table_of_mgrade_to_hmedgrade()

#-------------------------------------------------------------------------------
# name: print_table_of_mgrades_within_hgrades()
# desc: prints a table showing the percentage of mgrades within the hgrades range
#-------------------------------------------------------------------------------
def print_table_of_mgrades_within_hgrades(hjsondata, mjsondata):
    # Get the data
    mgrades = get_numeric_grades(mjsondata)
    hgrades = get_numeric_grades(hjsondata)
    hmingrades = get_numeric_grades(hjsondata, "min_num_grade")
    hmaxgrades = get_numeric_grades(hjsondata, "max_num_grade")

    # Compute the table
    table_data = []
    total_machine_grades = len(mgrades)  # Total number of machine grades
    count_of_machine_grades_within_range = 0
    mgrades_within_hgrades = {}
    for num in mgrades.keys():
        table_data.append([
            num,
            hmingrades.get(num, "N/A"),
            mgrades[num],
            hgrades.get(num, "N/A"),
            hmaxgrades.get(num, "N/A")
        ])
        # Check if the machine grade is within the human grade range
        if hmingrades.get(num) is not None and hmaxgrades.get(num) is not None:
            if hmingrades[num] <= mgrades[num] <= hmaxgrades[num]:
                count_of_machine_grades_within_range += 1
                mgrades_within_hgrades[num] = mgrades[num]
        # end if
    # end for

    # Print the table
    headers = ["ID", "H Min Grade", "M Grade", "H Grade", "H Max Grade"]
    print(tabulate(table_data, headers=headers, tablefmt="grid"))

    # Calculate and print the percentage of machine grades within human range
    if total_machine_grades > 0:
        percentage_within_range = (count_of_machine_grades_within_range / total_machine_grades) * 100
        print(f"\nPercentage of machine grades within human grades' range: {percentage_within_range:.2f}%")
    else:
        print("\nNo machine grades available to calculate percentage.")
    # end else
    return mgrades_within_hgrades
# end print_table_of_mgrades_within_hgrades()

#---------------------------------------------------------------
# name: compute_submission_grade_per_grader() 
# desc: returns the computed 
#---------------------------------------------------------------
def compute_submission_numeric_grade_per_grader(submission):
    weight = 0.25
    submission_num_grade = [0,0,0,0]
    for criteria in submission.values():
        # skip groups of grades greater than 4
        if(len(criteria['num_grades']) > 4):
           continue
        # end if
        
        # check if it contains any NAN values    
        i = 0
        for num_grade in criteria['num_grades']:  
            submission_num_grade[i] += (weight * num_grade)
            i += 1
        # end for
    # end for
    return submission_num_grade
# end compute_submission_grade_per_grader()


#----------------------------------------------------------
# name: get_letter_grade()
# desc: returns a letter grade for a numeric grade
# scale:
#       80-100 - A+, A++
#       70-79  - A
#       60-69  - B
#       50-59  - C
#       40-49  - D
#       0-39   - F
#----------------------------------------------------------
def get_let_grade(num_grade):
    if 70 <= num_grade <= 100:
        return 'A'
    elif 60 <= num_grade < 70:
        return 'B'
    elif 50 <= num_grade < 60:
        return 'C'
    elif 40 <= num_grade < 50:
        return 'D'
    elif 0 <= num_grade < 40:
        return 'F'
    else:
        raise ValueError("Grade must be between 0 and 100 inclusive.")
# end get_letter_grade

#----------------------------------------------------------
# name: get_num_grade()
# desc: returns a random numeric grade for a letter grade
# scale:
#       A     - 80-100
#       B     - 70-79
#       C     - 60-69
#       D     - 50-59
#       F     - 0-49
#----------------------------------------------------------
def get_num_grade(letter_grade):
    if letter_grade == 'A':
        return 80  #random.randint(80, 100)
    elif letter_grade == 'B':
        return 70 #random.randint(70, 79)
    elif letter_grade == 'C':
        return 60 #random.randint(60, 69)
    elif letter_grade == 'D':
        return 50 #random.randint(50, 59)
    else:
        return 0 #random.randint(0, 49)
# end get_num_grade()




# Convert detailed grade to numeric equivalent
def convert_grade_to_numeric(grade):
    if grade == "A++":
        return 95
    elif grade == "A+":
        return 85
    elif grade == "A":
        return 77
    elif grade == "A-":
        return 72
    elif grade == "B+":
        return 68
    elif grade == "B":
        return 65
    elif grade == "B-":
        return 61
    elif grade == "C+":
        return 58
    elif grade == "C":
        return 55
    elif grade == "C-":
        return 51
    elif grade == "D+":
        return 48
    elif grade == "D":
        return 45
    elif grade == "D-":
        return 42
    else:
        return 30  # F grade (0-39)



# Helper function to convert letter grades to simplified grades
def convert_to_simplified_grade(grade):
    # Convert detailed grade to numeric equivalent
    def convert_grade_to_numeric(grade):
        if grade == "A++":
            return 95
        elif grade == "A+":
            return 85
        elif grade == "A":
            return 77
        elif grade == "A-":
            return 72
        elif grade == "B+":
            return 68
        elif grade == "B":
            return 65
        elif grade == "B-":
            return 61
        elif grade == "C+":
            return 58
        elif grade == "C":
            return 55
        elif grade == "C-":
            return 51
        elif grade == "D+":
            return 48
        elif grade == "D":
            return 45
        elif grade == "D-":
            return 42
        else:
            return 30  # F grade (0-39)
    
    # Map numeric grade to simplified grade
    numeric_grade = convert_grade_to_numeric(grade)
    if numeric_grade >= 77:
        return "A"
    elif numeric_grade >= 65:
        return "B"
    elif numeric_grade >= 51:
        return "C"
    elif numeric_grade >= 42:
        return "D"
    else:
        return "F"

#---------------------------------------------------------------
# name: compute_submission_grade_per_grader() 
# desc: returns the computed 
#---------------------------------------------------------------
def compute_submission_numeric_grade_per_grader(submission):
    weight = 0.25
    submission_num_grade = [0,0,0,0]
    for criteria in submission.values():
        # skip groups of grades greater than 4
        if(len(criteria['num_grades']) > 4):
           continue
        # end if
        
        # check if it contains any NAN values    
        i = 0
        for num_grade in criteria['num_grades'].values():  
            submission_num_grade[i] += (weight * num_grade)
            i += 1
        # end for
    # end for
    return submission_num_grade
# end compute_submission_grade_per_grader()


def compute_submission_numeric_grade(submission):
    weight=0.25
    num_grade = 0.0
    for criteria in submission.values():
        num_grade += weight * criteria["avg_num_grade"]
    # end for
    return num_grade
# end compute_submission_numeric_grade()

#----------------------------------------------------------
# name: get_letter_grade()
# desc: returns a letter grade for a numeric grade
# scale:
#       80-100 - A+, A++
#       70-79  - A
#       60-69  - B
#       50-59  - C
#       40-49  - D
#       0-39   - F
#----------------------------------------------------------
def get_letter_grade(num_grade):
    if 70 <= num_grade <= 100:
        return 'A'
    elif 60 <= num_grade < 70:
        return 'B'
    elif 50 <= num_grade < 60:
        return 'C'
    elif 40 <= num_grade < 50:
        return 'D'
    elif 0 <= num_grade < 40:
        return 'F'
    else:
        raise ValueError("Grade must be between 0 and 100 inclusive.")
# end get_letter_grade