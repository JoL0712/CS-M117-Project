Login credentials are set to:
username:CS117
password:whothehellknows


Each request must have 5 fields included:

 [USER/PASS/LOGOUT/OPTION/UPDATE] [Opt#/0] [Ver#/0] [(Int)Serial#] [Text/.]


About serial#:
client first assign a serial# in request: 
USER 0 0 CS117 [Initial Serial#]
Then for each next request, client and pc both calculate the next number with
a method only known by the program. Client must provide the correct serial#
to keep the logged-in state during each request. If failed, client must log in again.

Function used:
void nextSerial()
{
	long long result = ((long long) savedSerial * 1103515245 + 12345) % 65535;
	savedSerial = (int) result;
}



 Sample requests:(replace tab with space)


USER	0	0	48		CS117
PASS	0	0	(whatever next)	whothehellknows	
LOGOUT	0	0	(whatever next) .
OPTION	1(opt#)	1(ver#)	(whatever next) .
UPDATE	1(opt#)	1(ver#)	(whatever next) .

