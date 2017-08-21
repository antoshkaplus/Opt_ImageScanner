# this script reads all the scores
# and prints sorted entries with corresponding
# total scores

import glob
import os

# list of pairs: filename, total_score
result = []

filenames = glob.glob("../scores/*.txt")
for f_name in filenames:
    with open(f_name) as f:
        total_score = 0.
        f.readline()
        for line in f:
            score = float(line.split(",")[1])
            total_score += score
        base = os.path.basename(f_name)
        base = base.split(".")[0]
        result.append((base, total_score))
result.sort(key=lambda x: x[1], reverse=True)
for name, score in result:
    print name, ":", score