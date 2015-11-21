#run this script with Python3
#search for device from Android app
#connect to this server from Android app
#send commands over

try:
    from Tkinter import *
except ImportError:
    from tkinter import *
import sys
import subprocess
import socket
import time

PORT = 2000

BUFLEN = 1024
NUMOPT = 16

#[keyword] [message]
#OUTPUT [message to output to user]

BAD_REQUEST_TEMPLATE = "OUTPUT Error: Syntax Error. [USER/PASS/LOGOUT/OPTION/UPDATE] [Opt#/0] [Ver#/0] [(Random Int)Serial#] [Text/.]\n"
UPDATED_TEMPLATE = "UPDATED {} {}\n" #option version
NOT_UPDATED_TEMPLATE = "UPDATE_OPT {}\n" #option
BAD_METHOD_TEMPLATE = "OUTPUT Method {} is not supported\n"
USER_OK_TEMPLATE = "INPUT password\n"
USER_BAD_TEMPLATE = "PW_BAD Error: Bad Credential\n"
PASSWORD_OK_TEMPLATE = "PW_OK Logged in\n"
LOGOUT_TEMPLATE = "RESULT logout\n"
UNAUTHORIZED_TEMPLATE = "OUTPUT Please log in\n"

passwordHash = ""
auth = -1
savedSerial = 0

optionList = [None] * NUMOPT
optionValid = [None] * NUMOPT
optionVersion = [None] * NUMOPT

SETTINGS_FILE = "settings.dat"
        
def nextSerial():
    global savedSerial
    result = (savedSerial * 13469 + 2671) % 65535
    savedSerial = result

def cleanCache():
    global optionList
    global optionValid
    global optionVersion
    for i in range(NUMOPT):
        optionValid[i] = 0
        optionVersion[i] = -1
        optionList[i] = None
    
def invalidateOptionList():
    for i in range(NUMOPT):
        optionValid[i] = 0

def error(msg):
    print(msg)
    sys.exit(1)	

def run(number):
    #creates a new subprocess without blocking
    subprocess.Popen(optionList[number], shell=True)
    #print(optionList[number])
    return 0

def processRequest(data, sockfd):
    global optionList
    global optionValid
    global optionVersion
    global auth
    global savedSerial
    method = None
    number = 0
    version = 0
    additional = None
    serial = 0
    response = None #response message
    
    try:
        method, number, version, serial, additional = data.split(' ', 4)
    except:
        sockfd.send(BAD_REQUEST_TEMPLATE.encode('utf-8'))
        return -1

    serial = int(serial)
    version = int(version)
    number = int(number)

    # Check if the client is the last user.
    if (auth > -1 and serial != savedSerial):
        auth = -1
        return -1

    if (method == "UPDATE"):
        optionList[number] = additional
        optionValid[number] = 1
        optionVersion[number] = version
        response = UPDATED_TEMPLATE.format(number, version)
        sockfd.send(response.encode('utf-8'))
        return run(number)
    elif (method == "OPTION"):
        if (optionVersion[number] == version and optionValid[number] >= 0):
            return run(number)
        else:
            response = NOT_UPDATED_TEMPLATE.format(number)
            sockfd.send(response.encode('utf-8'))
            return 0
    elif (method == "PASS"):
        if (additional == passwordHash):
            auth = 1
            savedSerial = serial
            sockfd.send(PASSWORD_OK_TEMPLATE.encode('utf-8'))
            return 0
        else:
            auth = -1
            sockfd.send(USER_BAD_TEMPLATE.encode('utf-8'))
            return -1
    elif (method == "LOGOUT"):
        auth = -1
        sockfd.send(LOGOUT_TEMPLATE.encode('utf-8'))
        return 0
    else:
        response = BAD_METHOD_TEMPLATE.format(method)
        sockfd.send(response.encode('utf-8'))
        return -1
    
def do(newsockfd):
    data = None
    #read client's message
    try:
        data = newsockfd.recv(BUFLEN).decode('utf-8')
    except:
        pass
    else:
        if (auth == -1):
            print("Attempting to log in...")
        else:
            print("Here is the message: \n%s\n" % data)
        #process request
        n = processRequest(data, newsockfd)
        #process failed
        if (n == -1):
            print("Request was not successful.")
        print("---------------------next request--------------------")
        nextSerial()
        if (auth >= 0):
            print("Next Serial Number should be %d.\n-------------------" % savedSerial)

def get_ip_address():
    s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    s.connect(("8.8.8.8", 80))
    return s.getsockname()[0]  

def discover(ip_subnet, udp_sock):
    print("Broadcasting...");
    for j in range(256): #send to all local network ip address within subnet
        udp_sock.sendto("RemoteCommand".encode('utf-8'), (ip_subnet + str(j), PORT))

class mainWindow(Frame):
    def __init__(self,master):
        Frame.__init__(self, master, width=300, height=50)
        self.master=master
        self.master.width = 150
        self.l=Label(master,text="Create a Password")
        self.l.pack()
        self.e=Entry(master)
        self.e.pack()
        self.b=Button(master,text='Ok',command=self.cleanup)
        self.b.pack()
        self.pack()
    def pack(self, *args, **kwargs):
        Frame.pack(self, *args, **kwargs)
        self.pack_propagate(False)
    def cleanup(self):
        passwordFile = open(SETTINGS_FILE, 'wb')
        chars = list(self.e.get())
        total = 133
        for c in chars:
            total *= ord(c)
            total += ord(c)
            total %= 128
            hexData = hex(total)[2:].zfill(2)
            passwordFile.write(hexData.encode('utf-8'))
        passwordFile.close()
        self.master.destroy()
        
def main():
    global passwordHash
    try:
        passwordFile = open(SETTINGS_FILE, 'rb')
        passwordFile.close()
    except: 
        root=Tk()
        root.wm_title("Password")
        m=mainWindow(root)
        root.mainloop()

    try:
        passwordFile = open(SETTINGS_FILE, 'rb')
    except:
        sys.exit(1)
    else:
        passwordHash = passwordFile.read().decode('utf-8')
        passwordFile.close()

        sockfd = None
        newsockfd = None
        ip_subnet = get_ip_address().rsplit('.', 1)[0] + '.'
        
        try:
            udp_sock = socket.socket(socket.AF_INET, # Internet
                                 socket.SOCK_DGRAM) # UDP
            udp_sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
            udp_sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)
            sockfd = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        except:
            error("ERROR opening socket")
        try:
            sockfd.bind(('', PORT))
        except:
            error("ERROR on binding")

        cli_addr = None

        cleanCache()

        sockfd.settimeout(1)
        sockfd.listen(1) 

        while(True):
            discover(ip_subnet, udp_sock)
            try:
                #accept connections
                newsockfd, cli_addr = sockfd.accept()
            except:
                continue
            else:
                print("Attempting to establish connection with " + str(cli_addr))
                invalidateOptionList()
                newsockfd.settimeout(60)
                do(newsockfd)
                while (auth >= 0):
                    do(newsockfd)
                newsockfd.close()
                print("Closing connection")
                
        sockfd.close()
    return 0 

main()
