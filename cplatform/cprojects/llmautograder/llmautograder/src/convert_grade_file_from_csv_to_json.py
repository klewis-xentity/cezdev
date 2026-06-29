# usage: python .\convert_grade_file_from_csv_to_json.py ../data/osfstorage-archive/grades.csv ../data/convert_grade_file_from_csv_to_json/grades.json

import argparse
import csv
import json
import statistics

# Helper function to convert letter grades to numeric grades
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

# Helper function to convert numeric grades to letter grades
def convert_numeric_to_grade(num_grade):
    if num_grade >= 90:
        return "A++"
    elif num_grade >= 80:
        return "A+"
    elif num_grade >= 75:
        return "A"
    elif num_grade >= 70:
        return "A-"
    elif num_grade >= 67:
        return "B+"
    elif num_grade >= 64:
        return "B"
    elif num_grade >= 60:
        return "B-"
    elif num_grade >= 57:
        return "C+"
    elif num_grade >= 54:
        return "C"
    elif num_grade >= 50:
        return "C-"
    elif num_grade >= 47:
        return "D+"
    elif num_grade >= 44:
        return "D"
    elif num_grade >= 40:
        return "D-"
    else:
        return "F"

# Function to compute the average of a list
def compute_average(grades):
    if not grades:
        return 0
    return sum(grades) / len(grades)

# Function to compute min, max, and median grades
def compute_grade_stats(grades):
    if not grades:
        return None, None, None
    min_grade = min(grades)
    max_grade = max(grades)
    med_grade = statistics.median(grades)
    return min_grade, max_grade, med_grade

# Function to convert CSV to JSON with added statistics
def csv_to_json(csv_file_path, json_file_path):
    data = {}

    with open(csv_file_path, mode='r') as csv_file:
        csv_reader = csv.DictReader(csv_file)
        
        for row in csv_reader:
            assignment = int(float(row['assignment_number']))
            skill = row['skill'].replace(" ", "_").lower()
            grade = row['grade']
            participant_id = row['participant_id']
            human_feedback = row['comments']
            # Convert letter grade to numeric grade
            num_grade = convert_grade_to_numeric(grade)
            
            if assignment not in data:
                data[assignment] = {}
            
            if skill not in data[assignment]:
                data[assignment][skill] = {
                    "let_grades": {},
                    "num_grades": {},
                    "human_feedback": {},
                    "feedback": {},
                    "reason_grades": {},
                    "avg_let_grade": "",
                    "avg_num_grade": 0,
                    "min_num_grade": 0,
                    "max_num_grade": 0,
                    "med_num_grade": 0,
                    "min_let_grade": "",
                    "max_let_grade": "",
                    "med_let_grade": "",
                    "dif_avg_med_grade": 0,
                    "dif_min_max_grade": 0
                }
            
            # skip rows with NAN grades
            if(grade == "NAN"):
                continue
            # end if
                        
            # Append the letter grade and numeric grade
            data[assignment][skill]["let_grades"][participant_id] = grade
            data[assignment][skill]["num_grades"][participant_id] = num_grade
            data[assignment][skill]["human_feedback"][participant_id] = human_feedback
            data[assignment][skill]["feedback"][participant_id] = human_feedback
            data[assignment][skill]["reason_grades"][participant_id] = human_feedback
            
            # Compute average numeric grade
            avg_num = compute_average(data[assignment][skill]["num_grades"].values())
            data[assignment][skill]["avg_num_grade"] = avg_num
            data[assignment][skill]["avg_let_grade"] = convert_numeric_to_grade(avg_num)
            
            # Compute min, max, and median for numeric grades
            min_num, max_num, med_num = compute_grade_stats(data[assignment][skill]["num_grades"].values())
            data[assignment][skill]["min_num_grade"] = min_num
            data[assignment][skill]["max_num_grade"] = max_num
            data[assignment][skill]["med_num_grade"] = med_num
            
            # Convert median, min, max numeric grades to letter grades
            data[assignment][skill]["min_let_grade"] = convert_numeric_to_grade(min_num)
            data[assignment][skill]["max_let_grade"] = convert_numeric_to_grade(max_num)
            data[assignment][skill]["med_let_grade"] = convert_numeric_to_grade(med_num)

            # Compute differences
            data[assignment][skill]["dif_avg_med_grade"] = abs(avg_num - med_num)
            data[assignment][skill]["dif_min_max_grade"] = abs(min_num - max_num)

    # Write the result to the JSON file
    with open(json_file_path, 'w') as json_file:
        json.dump(data, json_file, indent=4)

# Main function to parse command line arguments
def main():
    parser = argparse.ArgumentParser(description='Convert CSV grade data to JSON with added statistics.')
    parser.add_argument('csv_file', help='Path to the input CSV file')
    parser.add_argument('json_file', help='Path to the output JSON file')
    args = parser.parse_args()

    # Convert the CSV data to JSON with statistics
    csv_to_json(args.csv_file, args.json_file)

    # Output the path to the JSON file
    print(f"JSON file has been created at: {args.json_file}")

if __name__ == "__main__":
    main()
