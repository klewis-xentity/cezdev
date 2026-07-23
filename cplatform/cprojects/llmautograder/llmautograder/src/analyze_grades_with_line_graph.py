import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from scipy.interpolate import make_interp_spline
import argparse
import os

# Parse command-line argument
parser = argparse.ArgumentParser(description="Plot smoothed grades from a CSV file.")
parser.add_argument("csv_file", help="Path to the CSV file containing grades")
args = parser.parse_args()

# Check if file exists
if not os.path.isfile(args.csv_file):
    print(f"Error: File '{args.csv_file}' not found.")
    exit(1)

# Load data from CSV
df = pd.read_csv(args.csv_file)

# Validate required columns
required_columns = ["Submission ID", "Machine Grade", "Human Grade 1", "Human Grade 2", "Human Grade 3", "Human Grade 4"]
if not all(col in df.columns for col in required_columns):
    print("Error: CSV file is missing one or more required columns.")
    exit(1)

# Setup
# Sort by Submission ID so the x-axis is strictly increasing (required by the spline)
df = df.sort_values("Submission ID").reset_index(drop=True)
x = df["Submission ID"]

# Spline degree must be <= number_of_points - 1 (cubic needs at least 4 points)
num_points = len(df)
if num_points < 2:
    print("Error: Need at least 2 submissions to plot a line chart.")
    exit(1)
spline_k = min(3, num_points - 1)

x_smooth = np.linspace(x.min(), x.max(), 500)

# Define colors for human grades
color_map = {
    'Human Grade 1': '#FFA500',
    'Human Grade 2': '#FF8C00',
    'Human Grade 3': '#FF69B4',
    'Human Grade 4': '#BA55D3',
}

# Plotting
plt.figure(figsize=(14, 8))

# Machine Grade
spline_mg = make_interp_spline(x, df["Machine Grade"], k=spline_k)
plt.plot(x_smooth, spline_mg(x_smooth), label='Machine Grade (MG)', color='crimson', linewidth=3)

# Human Grades
for col in color_map:
    spline = make_interp_spline(x, df[col], k=spline_k)
    label = col.replace('Human ', 'HG')
    plt.plot(x_smooth, spline(x_smooth), label=label, linestyle='--', linewidth=2, color=color_map[col])

# Style
plt.title("Smoothed Alignment of Machine Grade with Human Grades", fontsize=18, fontweight='bold')
plt.xlabel("Submission ID", fontsize=14)
plt.ylabel("Grade", fontsize=14)
plt.xticks(fontsize=12)
plt.yticks(fontsize=12)
plt.grid(True, linestyle='--', alpha=0.6)
plt.legend(title="Grade Sources", title_fontsize=12, fontsize=11, loc='upper right')
plt.tight_layout()

# Save relative to the project root (parent of this script's src/ folder),
# so it works no matter which directory the script is launched from.
project_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
output_dir = os.path.join(project_dir, "data", "analyze_grades_with_line_graph")
os.makedirs(output_dir, exist_ok=True)
output_file = os.path.join(output_dir, "out.pdf")
plt.savefig(output_file, bbox_inches="tight")
print(f"Line chart saved to {output_file}")
plt.show()
