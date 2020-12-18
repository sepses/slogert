#!/usr/bin/env python
import os
# import sys
# sys.path.append('/ ')
from logparser import Drain

# Regular expression list for optional preprocessing (default: [])
regex      = [
    r'blk_(|-)[0-9]+' , # block id
    r'(https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b[-a-zA-Z0-9()@:%_\+.~#?&\/\/=]*)', # http/s
    r'(/|)([0-9]+\.){3}[0-9]+(:[0-9]+|)(:|)', # IP
    r'(?<=[^A-Za-z0-9])(\-?\+?\d+)(?=[^A-Za-z0-9])|[0-9]+$', # Numbers
    r'(?<=()[\/:|=])([A-Za-z0-9._\/-]+)', # \w+[/:|=]\w+
    r'(?<=()[\/:|=\<])([A-Za-z0-9._\/-]+(?=\>))', # \w+[/:|=]\w+
    r'(?<=()[\/:|=\(])([A-Za-z0-9._\/-]+(?=\)))', # \w+[/:|=]\w+
    r'(?<=\[)\w+?(?=\])', # inside of a square bracket (e.g., username)
    r'(?<=\<)\w+?(?=\>)', # inside of a square bracket (e.g., username)
    r'(?<=\()\w+?(?=\))', # inside of a square bracket (e.g., username)
    r'(?<=()for user )([A-Za-z0-9._\/-]+)', # username specific for auth
]
st         = 0.5  # Similarity threshold
depth      = 4  # Depth of all leaf nodes

input_dir  = 'output/error.log/1-init' # The input directory of log file
output_dir = 'output/error.log/2-logpai' # The output directory of parsing results
log_file   = 'error.log.0' # The input log file name
log_format = '<Device> \[<dayOfWeek> <month> <day> <time> <year>\] \[<type>\] \[<pid> <pidNumber>\] <Content>' # Syslog log format

if not os.path.exists(output_dir):
    os.makedirs(output_dir)

parser = Drain.LogParser(log_format, indir=input_dir, outdir=output_dir,  depth=depth, st=st, rex=regex)
parser.parse(log_file)
