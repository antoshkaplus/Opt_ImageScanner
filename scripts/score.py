import sys
import os
import subprocess as sp
from multiprocessing import cpu_count
from multiprocessing import Pool
from optparse import OptionParser


SCORE_START = "Score = "
TEST_COUNT = 100
VERSION = None

CPU_COUNT = cpu_count()
if CPU_COUNT > 2: CPU_COUNT-= 2

parser = OptionParser(usage="usage: %prog --version=%executable [--test-count=%count] [--cpu=%cpu_count]")
parser.add_option("--test-count", dest="test_count")
parser.add_option("--version", dest="version")
parser.add_option("--cpu", dest="cpu_count")

(options, args) = parser.parse_args()

if options.test_count:
    TEST_COUNT = int(options.test_count)

if options.version:
    VERSION = options.version
else:
    parser.error("version not specified")

if options.cpu_count:
    CPU_COUNT = int(options.cpu_count)

def worker(i):
    print "start seed: " + str(i)
    command = ["java", "-jar", "ImageScannerVis.jar", "-exec", "../bin/" + VERSION + " -d", "-novis", "-seed", str(i)]
    s = sp.check_output(command)
    lines = s.split("\n")
    for ln in lines:
        if ln.startswith(SCORE_START):
            return ln[len(SCORE_START):]

pool = Pool(CPU_COUNT)
result = pool.map(worker, (i for i in range(TEST_COUNT)))
with open("../scores/" + VERSION + ".txt", "w") as out:
    out.write(str(len(result)) + "\n")
    for index, score in enumerate(result):
        out.write(str(index) + "," + score + "\n")