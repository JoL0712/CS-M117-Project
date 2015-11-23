import CrossPlatform
import subprocess
import sys

args = sys.argv[1:]
args[:0] = [ CrossPlatform.shellExecutable("key-press") ]
subprocess.call(args, shell=True)