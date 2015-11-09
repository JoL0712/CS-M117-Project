#run this script with Python3
#search for device from Android app
#connect to this server from Android app
#send commands over

import sys
import subprocess
import socket
import time

PORT = 2000

BUFLEN = 1024
NUMOPT = 16

BAD_REQUEST_TEMPLATE = "Error: Syntax Error. [USER/PASS/LOGOUT/OPTION/UPDATE] [Opt#/0] [Ver#/0] [(Random Int)Serial#] [Text/.]\n"
UPDATED_TEMPLATE = "Option {} updated to version {}.\n"
BAD_METHOD_TEMPLATE = "Method {} is not supported.\n"
NOT_UPDATED_TEMPLATE = "Option {} is not updated.\n"
USER_OK_TEMPLATE = "Password:\n"
USER_BAD_TEMPLATE = "Error: Bad Credential.\n"
PASSWORD_OK_TEMPLATE = "Logged in.\n"
LOGOUT_TEMPLATE = "Logged out.\n"
UNAUTHORIZED_TEMPLATE = "Please log in.\n"

password = "password"
auth = -1
savedSerial = 0

optionList = [None] * NUMOPT
optionValid = [None] * NUMOPT
optionVersion = [None] * NUMOPT

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
        return 0
    elif (method == "OPTION"):
        if (optionVersion[number] == version and optionValid[number] >= 0):
            return run(number)
        else:
            response = NOT_UPDATED_TEMPLATE.format(number)
            sockfd.send(response.encode('utf-8'))
            return 0
    elif (method == "PASS"):
        if (additional == password):
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
        data = newsockfd.recv(BUFLEN).decode()
    except:
        pass
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

def main():
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