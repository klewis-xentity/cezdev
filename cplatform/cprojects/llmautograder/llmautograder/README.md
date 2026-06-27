# Grade Analysis Tool

This Python script, `analyze_grades.py`, is designed to analyze grading data from an autograder system. It offers functionalities to visualize grade distributions, as well as compute and display rank correlations between different grading metrics. This tool can be used to identify grading inconsistencies and validate the grading process.

## Description

The script uses the following primary functions:

- **Histograms of Grades**: Plots histograms for each grade metric to visualize the distribution of grades.
- **Rank Correlation Analysis**: Computes rank correlations (Spearman and Kendall's tau) between different grading metrics to analyze the consistency and reliability of the grading data.
- **Visualization**: Generates plots for both histograms and correlation matrices, saving them to specified file paths.

## Process Workflow Diagram

The following diagram illustrates the workflow of the analyze_grades.py script, detailing the sequence of operations from data input to output generation:

```text
+------------------+       +------------------+       +----------------------+
|                  |       |                  |       |                      |
|  Input CSV File  +------>+  Python Script   +------>+  Histogram Plots     |
| (grades.csv)     |       | (analyze_grades) |       |  & Correlation Plots |
|                  |       |                  |       |                      |
+------------------+       +---------+--------+       +-----------+----------+
                                    |                             |
                                    |                             |
                                    v                             v
                           +--------+---------+       +-----------+-----------+
                           |                  |       |                       |
                           |  Output PDFs for |       |  Output PDFs for      |
                           |  Histograms      |       |  Rank Correlations    |
                           |                  |       |                       |
                           +------------------+       +-----------------------+
```

## Dependencies

- Python 3.x
- pandas
- matplotlib
- scipy

## Installation

Ensure you have Python installed on your system. You can then install the required packages using pip:

```bash
pip install pandas matplotlib scipy
```

## Usage

To use the script, you need to provide a CSV file containing the grades and specify the prefix for output filenames of the histograms and correlation plots. Here is the general syntax to run the script:

```bash
python analyze_grades.py <grade_csv_file> <histogram_output_prefix> <correlation_output_prefix>
```

### Example

```bash
python analyze_grades.py ./data/grades.csv ./data/grades_distribution ./data/grades_rank_correlation
```

This command will read grades from `./data/grades.csv`, save histogram plots with filenames starting with `./data/grades_distribution`, and save rank correlation analysis plots with filenames starting with `./data/grades_rank_correlation`.

## Files

- `analyze_grades.py`: Main script file.
- `grades.csv`: Example CSV file containing the grades data (not included in this package).

## Contributing

Contributions to this project are welcome. Please fork the repository, make your changes, and submit a pull request.

## License

This project is open-sourced under the MIT License. See the LICENSE file for more details.
