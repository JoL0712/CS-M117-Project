#GO TO COMMAND LINE AND TYPE 'python setup.py build'
from cx_Freeze import setup, Executable
setup(name = "Remote Command", version = "1.0",
      description = "Remote Command server",
      executables = [Executable("server.py")])
