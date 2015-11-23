import platform

def shellExecutable(scriptPath):
	ext = "sh"
	if platform.system() == 'Windows':
		ext = "bat"
	scriptPath += "." + ext
	if ext == "sh":
		scriptPath = "./" + scriptPath
	return scriptPath