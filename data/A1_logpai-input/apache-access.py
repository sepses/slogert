#!/usr/bin/env python
import sys
sys.path.append('../')
from logparser import Drain

input_dir  = '../../output/'  # The input directory of log file
output_dir = '../../output/logpai/'  # The output directory of parsing results
log_file   = 'apache-access.log'  # The input log file name
log_format = '<Device> <IP> \(-\) - - \[<DateTime> <TimeZone>\] \"<Content>\"'  # apache log format

# Regular expression list for optional preprocessing (default: [])
regex      = [
    r'blk_(|-)[0-9]+' , # block id
    r'(/|)([0-9]+\.){3}[0-9]+(:[0-9]+|)(:|)', # IP
    r'(?<=[^A-Za-z0-9])(\-?\+?\d+)(?=[^A-Za-z0-9])|[0-9]+$', # Numbers
    r'(?<=()[\/:|=])([A-Za-z0-9._\/-]+)', # \w+[/:|=]\w+
    r'(?<=\[)\w+?(?=\])' # inside of a square bracket (e.g., username)
]
st         = 0.5  # Similarity threshold
depth      = 4  # Depth of all leaf nodes

parser = Drain.LogParser(log_format, indir=input_dir, outdir=output_dir,  depth=depth, st=st, rex=regex)
parser.parse(log_file)

