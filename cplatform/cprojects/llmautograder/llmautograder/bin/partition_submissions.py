
import pandas as pd
import numpy as np

# Load dataset
df = pd.read_csv("C:\\Users\\oyole\\Desktop\\cezdev\\cplatform\\cprojects\\llmautograder\\llmautograder\\data\\grades.csv")

# Extract unique submission IDs
submission_ids = df["assignment_number"].dropna().unique()

# Shuffle reproducibly
rng = np.random.default_rng(seed=42)
rng.shuffle(submission_ids)

# Calculate split sizes
n = len(submission_ids)
n_train = int(n * 0.60)
n_val = int(n * 0.20)

# Partition IDs
set1 = submission_ids[:n_train]
set2 = submission_ids[n_train:n_train + n_val]
set3 = submission_ids[n_train + n_val:]

# Create corresponding datasets
df_set1 = df[df["assignment_number"].isin(set1)]
df_set2 = df[df["assignment_number"].isin(set2)]
df_set3 = df[df["assignment_number"].isin(set3)]

# Save datasets
df_set1.to_csv("train.csv", index=False)
df_set2.to_csv("validation.csv", index=False)
df_set3.to_csv("test.csv", index=False)

# Verify splits
print(f"Total unique submissions: {n}")
print(f"Train: {len(set1)} ({len(set1)/n:.1%})")
print(f"Validation: {len(set2)} ({len(set2)/n:.1%})")
print(f"Test: {len(set3)} ({len(set3)/n:.1%})")

# Verify no submission overlap
assert len(set(set1) & set(set2)) == 0
assert len(set(set1) & set(set3)) == 0
assert len(set(set2) & set(set3)) == 0
