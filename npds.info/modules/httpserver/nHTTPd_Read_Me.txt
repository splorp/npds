READ ME: nHTTPd

Change History:

02/12/2006 2.107 [Paul Guyot]
	New API for SSIs that allow modules to define lazy evaluated SSIs and SSIs
		with parameters, passed as HTML attributes. NoteServ 2.103 makes use of
		this feature. The SSIs of the server itself have also been reworked to
		be based on lazy evaluation.
	The scripts can also take parameters. The parameters are passed as HTML
		attributes and are available in the script in the param.attributes
		frame.

23/04/2004	2.106 [Paul Guyot]
	Fixed the bug that prevented one to post notes with &, URLs and similar stuff.

07/02/2004	2.105 [Paul Guyot]
	Fixed a bug (or maybe more) related to rotation.
	Fixed the width so one can fully read 6 digits hit counts (bug #46 submitted by Grant)

16/06/2003	2.104 [Paul Guyot]
	Changed the NPDS URL.
	Reworked the finite state machine of the server.
	Fixed bugs in SSI parsing.
	The parameter of the SSIs is not the request.
		Backward compatibility in NPDS 3 is not guaranteed.
	Fixed other bugs I don't recall about.

24/03/2003	2.103 [Paul Guyot]
	Fixed a bug in logaccess method [Paul F. (Mon, 24 Mar 2003 15:40:41 -0500)]
	Fixed a bug in mGetHostAddressWithPortStr method (used to generate 400/404/500 pages)

24/03/2003	2.102 [Paul Guyot]
	Fixed two problems in the server probably causing it to stop serving.
		(Apparently, there are still issues)
	Fixed a bug when the server would crash if an IP wouldn't be provided in the Tracker's
		Public IP field [Halvor (Thu, 20 Mar 2003 23:27:36 +0100 (CET)) &
			David (Fri, 21 Mar 2003 09:39:08 +1000)]
	Now replaces every HTTP_ CGI vars (with the content of headers) from SSI outputs. Note: these
		variables aren't replaced if the header isn't present.
	Added REQUEST_URI replacement in the SSI outputs.
	Added X-Forwarded-For header to the log soup, if present.
	SERVER_NAME is now properly replaced with what it should be.

18/03/2003	2.101 [Paul Guyot]
	Fixed robots.txt.
	Something else I already forgot.
	:LogToTextFile(aStore) is now limited to 32 KB
	added :LogToWorks(aStore) which isn't limited (although it can be slow if the log is big).
	Fixed the folder popup that occurs with admin-post.html.
	Made -36006's on disconnect completely silent.

15/03/2003	2.1 [Paul Guyot]
	Fixed issues with current_http_request global variable and other remainings
	of thread unsafety (basically, I fixed the problem I introduced with
	version 2.03)
	Fixed a silent problem in the server when disconnecting.
	The log now works. Log is in a new format and exported as text in CLF format.
	Fixed HTTP date: it no longer depends on the locale.
	Improved request parsing.
	Probably some other changes I forgot about.

26/10/2002	2.046 [Paul Guyot]
	Improved EvaluateScript so it tells more about the exceptions.

16/09/2002	2.045 [Paul Guyot]
	Fixed three or four bugs in the new TCP Server.
	Added a maximum size for the pool of endpoints.
	
15/09/2002	2.044 [Paul Guyot]
	New TCP Server.
	
23/03/2002	2.043 [Paul Guyot]
	Fixed the ListenCB -60049 bug.
	We no longer can be star'd twice.
	
27/10/2001	2.042 [Paul Guyot]
	Included ProtoSoupBrowser 1.1

26/10/2001	2.041 [Paul Guyot]
	Set the version to 2.041 (I forgot it for 2.040)
	Mozilla 5 is considered as a modern browser.

25/10/2001	2.040 [Paul Guyot]
	Fixed the SetRemove problem (thanks Sean) with a workaround. I think this is why the server
		sometimes refused to close every endpoint
	Added plenty of verbose lines and updated the previous ones.

21/08/2001	2.039 [Paul Guyot]
	Fixed a bug introduced with 2.036 with the title not being set if port is 80 (thanks Grant).
	Fixed another aesthetic bug in this function (:x suffix was only added with a public IP) 
		and revised it entirely.

15/08/2001	2.038 [Paul Guyot]
	Fixed the ReorientToScreen method: the server is properly behaving on reorientation,
		and is not shown if it was minimized before reorientation.
		Known limitation: when maximized, you see for a brief moment the stop button and the
		close box. I think it's not worth trying to fix it with more hacks in this code (if
		ever this is possible), we really need to change things and have the server able to
		serve pages even when the application is closed.
	Cleaned the viewQuitScript and the viewSetupFormScript.
	Removed some unused code here and there.
	Changed the status indicator messages. Now, possible values are:
		IDLE: not serving
		INIT: initiating the internet link
		RDY: ready to serve pages, didn't get any request yet
		SERV: last request was successfully completed
		400: last request was completed with a 400 error (Bad request)
		404: last request was completed with a 404 error (Resource not found)
		500: last request was completed with a 500 error (Internal error)
		RBT: last request was a robot (search engine) and I deflected it

04/07/2001	2.037 [Paul Guyot]
	Fixed the StartServing method.

29/06/2001	2.036 [Adam Tow]
	Changed the UI of the nHTTPd application. Buttons and controls more cleanly laid out
	Updated the IP Address field (vIPField) to display the current public IP address of the
		device instead of the LAN address. Make a call to the TraqMe client app to retrieve the
		public IP address (GetPublicIP). 

27/05/2001	2.035 [Paul Guyot]
	Fixed two bugs in the TCP/IP server.
	Better alerts in the TCP/IP server to track two other known bugs.

31/08/2000	2.034 [Paul Guyot]
	Fixed a little bug in TCP Server error handling.
	Better prefs support. The prefs are no longer reset.
	The counter is no longer reset from this version (this is the last reset).
	New HitCounterSince slot in the prefs, which data is shown with the <COUNTER-SINCE> SSI tag.
	Minor aesthetic changes.
	
28/08/2000	2.033 [Paul Guyot]
	Fixed the all ports open bug.
	Added the backdrop icon.
	Minor other changes.

20/08/2000	2.032 [Paul Guyot]
	Improved log with errors (status codes) and user agent.
		(I should re-code the whole logging one day because of multiple connections)
	404 show ERR on the serving Now static text.
	iCab is recognized as a modern browser.

09/07/2000	2.031 [Paul Guyot]
	Fixed the -16013 bug
	
08/07/2000	2.03 [Paul Guyot]
	Small changes to allow the start and stop button to show up properly.
	Changes to support multiple connections
	Improved address parsing without the third slash bug.
	The server now sends StopScript to anyone who'll understand it.

20/04/1999	2.02 [Matt Vaughn]
	Fixed screwy logging behavior.
	Limit on size of script-generated text removed. Now limited only by free RAM.
	Fixed browser detection algorithm to be actually useful.
	JavaScript is only inserted if it's needed.
	The <SERVER_NAME> tag now returns server IP address to scripts.
	Fixed screen rotation bug.
	Brought HTTP header transmission syntax into RFC20068 compliance.
	Various oversights in error-trapping addressed.
	Revised HTML engine uses HTML 4.0 and CSS.
	Added support for most CGI environment variables in scripting architecture.
	Cleaned up API and added more direct access.
	Added 'Apply' function to NPDS Config to allow changing of settings without shutting down the server.
