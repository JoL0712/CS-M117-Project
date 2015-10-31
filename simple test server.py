#run this script
#run android app
	#click 'Find Devices' from menu
	#go to 'Devices' page and click this device to connect
	#go to 'Commands' page and click on a command and the script will be printed out here

import socket
import time

UDP_IP = "192.168.1."
UDP_PORT = 2000
MESSAGE = "RemoteCommand"

sock = socket.socket(socket.AF_INET, # Internet
                     socket.SOCK_DGRAM) # UDP
sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
sock.setsockopt(socket.SOL_SOCKET, socket.SO_BROADCAST, 1)

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM) 
s.bind(('', 2000)) 
s.listen(1) 
s.settimeout(1)

client = None

while client == None:
	time.sleep(.5)
	print("Sending...");
	for j in range(256): #send to all local network ip address within subnet
		sock.sendto(MESSAGE.encode('utf-8'), (UDP_IP + str(j), UDP_PORT))
	try:
		client, address = s.accept()
		print("Connected to " + address);
	except:
		continue
		
while 1: 
	data = client.recv(1024) 
	print(data);
	client.send(data)

client.close()