Read Me: NPDS Tracker Client

NPDS Tracker Client Is a "Seriously Hacked Version" of the Thumb project released with the NIE 1.1 Developer Goodies. 

It can probably be done more elegantly than it's done here but I don't have any desire to rework the code.  If you want to, be my guest.  I'd be thrilled to have a Tracker Client that wasn't almost as big as the HTTP server.

Change History:

15/03/2003	2.1 [Paul Guyot]
	Updated for NPDS 2.1
	Updated default settings.

21/08/2001 2.035 [Paul Guyot]
	Added a pref to not report 4xx errors which Grant finds annoying.
	Fixed a bug in the way prefs is handled when upgrading (we still have to reset all the prefs, though)

13/08/2001 2.034 [Paul Guyot]
	Aesthetic changes.
	The client no longer asks the main module for the IP but grabs it itself.
	Removed unnecessary files from the Thumb sample code.
	Made some clean up in the FSM.
	The client is now verbose and displays alerts when problems occur (hence we'll know immediatly if the tracker is down).
	Added a checkbox to use the private IP field instead of the Newton's IP.
	The return code of the server is tested (and if it's not 20X, an error message is displayed).

30/06/2001 2.033 [Adam Tow]
	Added GetPublicIP method to the main view to work with nHTTPd 2.036

06/08/2000 2.032 [Paul Guyot]
	Fixed the bug of the interface for programmed reular REGUP.

05/08/2000 2.031 [Paul Guyot]
	Added an interface for programmed reular REGUP.
	(this feature will be removed with UP Continuous tests to be written later)

08/07/2000 2.03 [Paul Guyot]
	Added the REGDN command as suggested by Matt to use with the new tracker server.
	Added support for automatic REGDN and an interface for it.

14/06/2000 2.02 [Paul Guyot]
	Added support for a custom IP, so that the user can use NPDS thru an IP masquerading gateway.
		To change the IP, just put whatever you like in the Public IP field. To get the Newton IP,
		put <Ask nHTTPd> in the field (this option is offered in the popup, as well as the Newton
		IP if NPDS is connected).
	Faster compression is a useless flag, to take more space on the MP. I disabled it.

20/04/1999 2.01 [Matt Vaughn]
	Added support for auto-configuration packages to allow instant configuration for a given tracker.
	Data fields now have memory of past values.
	Added HTML 4.0/CSS support.



