Read Me: ProtoSoupBrowser

This is the layout code for the cache browser used in NPDS.  Although you can invoke the browser from nHTTPd with the openBrowser() method, if you want to incorporate this into your own project, feel free.

You have to add both the Layout and the Resources file to your project in order to get it to work.

Change History:

27/10/2001	1.1 [Paul Guyot]
	Fixed bug when closing the browser with no entry left.
	 (in fact commented useless code which has the effect of generating an
	 exception in that particular case)
	 [Grant: 20011026-32227393$]

01/04/1999	1.0 [Matt Vaughn]
	Initial release.
	