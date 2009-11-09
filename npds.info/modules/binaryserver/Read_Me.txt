READ ME: Binary server

Change History:

20/05/2003	Installer 1.2 [Paul Guyot]
	The installer template now displays an error message for every entry that could not be saved
		to store.
	The installer template is now compressed on the Newton (I don't understand why it wasn't).

24/03/2003	1.11 [Paul Guyot]
	Fixed version number.

23/03/2003	1.101 & Installer 1.1 [Paul Guyot]
	Binaries are now served asynchronously and in chunks (it means you can serve large binaries).
	Fixed the about box bug (Grant, Wed, 19 Mar 2003 19:26:02 -0700)
	The installer template now copies the binary into a compressed VBO
		(allows binaries larger than 64 KB)

15/03/2003	1.1 [Paul Guyot]
	Updated for NPDS 2.1

25/10/2001	1.01 [Paul Guyot]
	Fixed a bug causing this server to not close the endpoint (this had plenty of bad effects)

01/07/2001	1.0 [Paul Guyot]
	Initial release
