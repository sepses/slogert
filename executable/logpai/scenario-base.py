#!/usr/bin/env python
import os
# import sys
# sys.path.append('/ ')
from logparser import Drain

# Regular expression list for optional preprocessing (default: [])
regex      = [
    r'<?([a-zA-Z0-9.!#$%&\'*+\/?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*)>?' , # email
    r'(https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b[-a-zA-Z0-9()@:%_\+.~#?&\/\/=]*)', # http/s
    r'(/|)([0-9]+\.){3}[0-9]+(:[0-9]+|)(:|)', # IP
    r'(?<=()[\/:|=\<])([A-Za-z0-9._\/-]+(?=\>))', # \w+[/:|=]\w+
    r'(?<=()[\/:|=\(])([A-Za-z0-9._\/-]+(?=\)))', # \w+[/:|=]\w+
    r'(?<=\[)\w+?(?=\])', # inside of a square bracket (e.g., username)
    r'(?<=\<)\w+?(?=\>)', # inside of a square bracket (e.g., username)
    r'(?<=\()\w+?(?=\))', # inside of a square bracket (e.g., username)
    r'(?<=()[\/:|=])([A-Za-z0-9._\/-]+)', # \w+[/:|=]\w+
    r'(?<=()for user )([A-Za-z0-9._\/-]+)', # username specific for auth
    r'(?<=()login for )([A-Za-z0-9._\/-]+)', # username on login
    r'(?<=()LOGIN for )([A-Za-z0-9._\/-]+)', # username on login
    r'(?<=()sent to )([A-Za-z0-9._\/-]+)', # username on messages
    r'(?<=()from )([A-Za-z0-9._\/-]+)', # username / ip
    r'(?<=()pid )([0-9]+)', # pid
    r'(?<=()pid=)([0-9]+)', # pid
    r'blk_(|-)[0-9]+' , # block id
    r'(?<=[^A-Za-z0-9])(\-?\+?\d+)(?=[^A-Za-z0-9])|[0-9]+$', # Numbers
]
st         = 0.5  # Similarity threshold
depth      = 4  # Depth of all leaf nodes

input_dir  = '$input_dir$' # The input directory of log file
output_dir = '$output_dir$' # The output directory of parsing results
log_file   = '$log_file$' # The input log file name
log_format = '$log_format$' # Syslog log format

if not os.path.exists(output_dir):
    os.makedirs(output_dir)

parser = Drain.LogParser(log_format, indir=input_dir, outdir=output_dir,  depth=depth, st=st, rex=regex)
parser.parse(log_file)
