READ ME: NPDS Watcher

Cf the global NPDS Read Me for how to use this since there is no manual yet.

Change History:

29/03/2003	1.1a1 [Paul Guyot]
	Reorganized the code, it seems to be compatible with latest nHTTPd.
	The three checking routines can be enabled and disabled separately now.
	The check for alerts routine now explicitly looks for protoTCPServer alerts.
	Fixed a problem where the server might not be restarted if the internet setting
		is to disconnect immediatly.
	Improved the way the alert is handled (the watcher will be triggered even if it's
		not the frontmost view and it will close it once the server will be restarted).

10/09/2002	1.0a14 [Paul Guyot]
	Fixed the -48803 bug introduced with 1.0a13.

07/09/2002	1.0a13 [Paul Guyot]
	The watcher now waits 5 seconds if NIE isn't active before launching the server
		(it actually tries 15 times and display an alert if NIE isn't there
		after these attempts)

23/03/2002	1.0a12 [Paul Guyot]
	nHTTPd is properly maximized before being launched.

20/03/2002	1.0a11 [Paul Guyot]
	Server is now restarted (instead of rebooted)
	nHTTPd is minimized when started by the watcher
	NS Task check is now set to every 10 minutes instead of the Watcher period * 4.

03/10/2001	1.0a10 [Paul Guyot]
	Fixed the -48809 exception bug which sometimes occurred when opening the
		server application.
	Declared the global functions GetRegisteredSound, InetGrabLink and InetReleaseLink so
		NTK doesn't complain.

05/09/2001	1.0a9 [Paul Guyot]
	Creation of the Change History file.
	Fixed the bug experienced by Paul Filmer (411, 440 & 441 ):
		Now, if the endpoint's disposal function fails, we gonna restart.
	Fixed the bug experienced by Grant Hutchinson (53):
		Now, if the endpoint's bind fails, we gonna restart.
	The checking interval pickers now shows the actual strings instead
		of the number of seconds when the prefs are open.
	Fixed the bug experienced by Alex Humffray (4/9/01 7:30, 5/9/01 2:51)
		The state of the button and the status line isn't updated if they're
		not visible (it will be updated when they'll show up).
	