"""Analyze the grades from the autograder.

    Example:
    python analyze_grades.py \
        grades_evaluation.csv grades_distribution grades_rank_correlation
    
    Usage: 
    python analyze_grades.py ./data/grades.csv ./data/analyze_grades/grades_distribution ./data/analyze_grades/grades_rank_correlation 
"""
import argparse

import pandas as pd
import matplotlib
import matplotlib.pyplot as plt
from matplotlib import ticker
from scipy.stats import kendalltau, spearmanr



COLUMNS = (
    "Machine Grade",
    "Human Grade 1",
    "Human Grade 2",
    "Human Grade 3",
    "Human Grade 4",
    "Within Human Grades",
)

BINS = (0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100)


def parse_cmdline():
    parser = argparse.ArgumentParser()
    parser.add_argument("grade_csv_file", help="CSV data file containing the grades")
    parser.add_argument(
        "hist_plot_fn_prefix", help="output filename prefix for the histogram plots"
    )
    parser.add_argument(
        "correlation_plot_fn_prefix",
        help="output filename for rank correlation analysis plots",
    )
    return parser.parse_args()


def load_grades(grade_csv_file):
    return pd.read_csv(grade_csv_file)


def compute_rank_correlation(grades, method):
    row_names = COLUMNS[0:-1]
    col_names = COLUMNS[0:-1]
    coefs = [[None for _ in col_names] for _ in row_names]
    for row, c_x in enumerate(COLUMNS[0:-1]):
        for col, c_y in enumerate(COLUMNS[0:-1]):
            if method == "spearmanr":
                rho, p = spearmanr(grades[c_x], grades[c_y])
            elif method == "kendalltau":
                rho, p = kendalltau(grades[c_x], grades[c_y])
            else:
                raise ValueError(f"Unknown method {method}")
            coefs[row][col] = (rho, p)
    print(row_names, col_names)
    print(coefs)
    return row_names, col_names, coefs


def save_plot(plot_fn):
    plt.tight_layout()
    plt.savefig(plot_fn, bbox_inches="tight")
    print(f"saved figure to {plot_fn}")


def plot_matrix(row_names, col_names, coefs, plot_fn):
    fig, ax = plt.subplots()
    matrix = [[c[0] for c in row] for row in coefs]
    cax = ax.matshow(matrix, interpolation="nearest", cmap="gray_r")
    for i in range(len(row_names)):
        for j in range(len(col_names)):
            ax.text(
                j,
                i,
                f"r={coefs[i][j][0]:.2f}\np={coefs[i][j][1]:.2f}",
                ha="center",
                va="center",
                color="k",
            )
    fig.colorbar(cax)
    ticks_loc = [i for i in range(len(col_names))]
    ax.xaxis.set_major_locator(ticker.FixedLocator(ticks_loc))
    ax.set_xticklabels(col_names, rotation=30)
    ticks_loc = [i for i in range(len(col_names))]
    ax.yaxis.set_major_locator(ticker.FixedLocator(ticks_loc))
    ax.set_yticklabels(row_names)
    save_plot(plot_fn)


def plot_histograms(grades, plot_fn_prefix):
    for c in COLUMNS[0:-1]:
        _, ax = plt.subplots(clear=True)
        ax.hist(grades[c], bins=BINS, alpha=0.5, color="gray", edgecolor="black")
        ax.set_title(c)
        ax.set_xlabel("Grade")
        ax.set_ylabel("Frequency")
        plot_fn = f"{plot_fn_prefix}_{c}.pdf".replace(" ", "_")
        save_plot(plot_fn)


def plot_correlations(grades, plot_fn_prefix):
    for method in ["spearmanr", "kendalltau"]:
        fig_fn = f"{plot_fn_prefix}_{method}.pdf"
        row_names, col_names, coefs = compute_rank_correlation(grades, method)
        plot_matrix(row_names, col_names, coefs, fig_fn)


def main():
    args = parse_cmdline()
    grades = load_grades(args.grade_csv_file)
    plot_histograms(grades, args.hist_plot_fn_prefix)
    plot_correlations(grades, args.correlation_plot_fn_prefix)
    plt.show()


if __name__ == "__main__":
    main()