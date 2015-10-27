/* A simple server in the internet domain using TCP
   The port number is passed as an argument 
   This version runs forever, forking off a separate 
   process for each connection
*/
#include <stdio.h>
#include <sys/types.h>   // definitions of a number of data types used in socket.h and netinet/in.h
#include <sys/socket.h>  // definitions of structures needed for sockets, e.g. sockaddr
#include <netinet/in.h>  // constants and structures needed for internet domain addresses, e.g. sockaddr_in
#include <stdlib.h>
#include <sys/wait.h>	/* for the waitpid() system call */
#include <signal.h>	/* signal name macros, and the kill() prototype */
#include <errno.h>
#include <string.h>

const int BUFLEN = 1024;
const int NUMOPT = 16;

const char BAD_REQUEST_TEMPLATE = "Error: Bad Request.\n";
const char UPDATED_TEMPLATE = "Option %d updated to version %d.\n";
const char BAD_METHOD_TEMPLATE = "Method %s is not supported.\n";
const char NOT_UPDATED_TEMPLATE = "Option %d is not updated.\n";
const char USER_OK_TEMPLATE = "Password:\n";
const char USER_BAD_TEMPLATE = "Error: Bad User.\n";


const char username = "CS117";
const char password = "whothehellknows";
int auth = -1;

char optionList[NUMOPT][BUFLEN];
int optionValid[NUMOPT];
int optionVersion[NUMOPT];

void cleanCache()
{
	int i;
	for(i=0; i<NUMOPT; i++)
	{
		optionValid[i] = 0;
		optionVersion[i] = -1;
		memset(optionList[i],0,BUFLEN);
	}
}

void invalidateOptionList()
{
	int i;
	for(i=0; i<NUMOPT; i++)
		optionValid[i] = 0;

}

void error(char *msg)
{
	perror(msg);
	exit(1);
}

int run(int number)
{
	return 0;
}

int processRequest(char* buffer, int sockfd)
{
	char method[BUFLEN];
	int number;
	int version;
	char additional[BUFLEN];
	char response[BUFLEN]; //response message
	memset(response, 0, BUFLEN);

   	int n;
	n = sscanf(buffer, "%s %d %d",method, number, version);
	if (n == EOF || n < 3){
		write (sockfd, BAD_REQUEST_TEMPLATE, strlen(BAD_REQUEST_TEMPLATE));
		return -1;
	}

	// For debugging purposes
	printf("Here are Headers:%s %s %s\n",method, path, protocol);

	//Check the validity of request
	//method field: cannot process any method other than UPDATE, OPTION.
	if (strcasecmp(method, "UPDATE") == 0)
	{
		n = sscanf(buffer, "%s %d %d %s",method, number, version, additional);
		if (n == EOF || n < 4 || number >= NUMOPT){
			write (sockfd, BAD_REQUEST_TEMPLATE, strlen(BAD_REQUEST_TEMPLATE));
			return -1;
		}
		memset(optionList[number],0,BUFLEN);
		snprintf(optionList[number], BUFLEN-1, additional);
		optionValid = 1;
		optionVersion = version;
		snprintf(response, BUFLEN-1, UPDATED_TEMPLATE, number, version);
		write (sockfd, response, strlen(response));
		return 0;
	}
	else if(strcasecmp(method, "OPTION") == 0)
	{
		if (optionVersion[number] == version && optionValid[number] >= 0)
			return run(number);
		else
		{
			snprintf(response, BUFLEN-1, NOT_UPDATED_TEMPLATE, number);
			write (sockfd, response, strlen(response));
			return -1;
		}
	}
	else if (strcasecmp(method, "USER") == 0)
	{
		n = sscanf(buffer, "%s %d %d %s",method, number, version, additional);
		if (n == EOF || n < 4 || number >= NUMOPT){
			write (sockfd, BAD_REQUEST_TEMPLATE, strlen(BAD_REQUEST_TEMPLATE));
			return -1;
		}
		if (strcmp(additional, username) == 0)
		{
			auth = 0;
			write (sockfd, USER_OK_TEMPLATE, strlen(USER_OK_TEMPLATE));
			return 0;
		}
		else
		{
			auth = -1;
			write (sockfd, USER_BAD_TEMPLATE, strlen(USER_BAD_TEMPLATE));
			return -1;
		}
	} 
	else
	{
		snprintf (response, BUFLEN-1, BAD_METHOD_TEMPLATE, method);
		write (sockfd, response, strlen(response));
		return -1;
	}
}

int main(int argc, char *argv[])
{
	int sockfd, newsockfd, portno, pid;
	socklen_t clilen;
	struct sockaddr_in serv_addr, cli_addr;

	if (argc < 2) {
	 fprintf(stderr,"ERROR, no port provided\n");
	 exit(1);
	}
	sockfd = socket(AF_INET, SOCK_STREAM, 0);	//create socket
	if (sockfd < 0) 
	error("ERROR opening socket");
	memset((char *) &serv_addr, 0, sizeof(serv_addr));	//reset memory
	//fill in address info
	portno = atoi(argv[1]);
	port = portno;
	serv_addr.sin_family = AF_INET;
	serv_addr.sin_addr.s_addr = INADDR_ANY;
	serv_addr.sin_port = htons(portno);

	if (bind(sockfd, (struct sockaddr *) &serv_addr,
	      sizeof(serv_addr)) < 0) 
	error("ERROR on binding");

	cleanCache();

	while(1){
		listen(sockfd,5);	//5 simultaneous connection at most

		//accept connections
	 	newsockfd = accept(sockfd, (struct sockaddr *) &cli_addr, &clilen);
	 
		if (newsockfd < 0) 
			error("ERROR on accept");

		int n;
		char buffer[BUFLEN];

		memset(buffer, 0, BUFLEN);	//reset memory

		//read client's message
		n = recv(newsockfd,buffer,BUFLEN-1,0);
		if (n < 0)
			error("ERROR reading from socket");
		printf("Here is the message: \n%s\n",buffer);
		/*Null terminate the buffer so we can use string operations on it.*/
		buffer[n] = '\0'; 

		/*process request*/
		n = processRequest(buffer, newsockfd);
		/*process failed*/
		if (n == -1) 
			printf("Request was not successful.");

		printf("\n---------------------next request--------------------\n");
		close(newsockfd);//close connection 
	}
	 
	close(sockfd);
	return 0; 
}



