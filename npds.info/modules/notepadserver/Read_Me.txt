READ ME: NotePad server

Change History:

11/01/2007 2.104 [Paul Guyot]
	Fixed a bug where the notes list would not include the latest element when
	presented in a reverse order.

02/12/2006 2.103 [Paul Guyot]
	No longer use VBO to handle notes list. The lists aren't large enough and
	  the result is converted to a regular string by the server anyway.
	Make large use of the new nHTTPd API for SSI (2.107 or higher):
	  - the new <notes> tag with options replaces the old note list tags.
	  - the SSIs that relied on functions are only evaluated when nHTTPd feels
	    the need to evaluate them (with the previous APIs, the modules had to
	    figure out which SSI would need to be evaluated and therefore the test
	    was done twice, once by the module and once by the server before the
	    replacement).
	Added a new <notescount> tag with options that displays the count of notes.
	If nHTTPd 2.107 or higher isn't installed, the SSIs will display:
      "Please install nHTTPd 2.107 or higher"

30/11/2006 2.102 [Paul Guyot]
	Used VBO to handle notes list (NOTES_*, POST_* and EVERY_* SSIs) and to
	  render checklists and outlines.
	These lists are now built using a soup cursor instead of a building an
	  in-ram array. (These two changes should fix the out of memory problem
	  Grant was experiencing)

20/06/2003	2.101 [Paul Guyot]
	Now serves ink text embedded into notes.
	Fixed the about box bug (Grant, Wed, 19 Mar 2003 19:26:02 -0700)
	Fixed a bug about Notes with no name.

15/03/2003	2.1 [Paul Guyot]
	Updated for NPDS 2.1

28/10/2001 2.045 [Paul Guyot]
	Fixed a bug which lead to have no HTML formatting for notes (introduced with 2.044).
		[Josh: 20011027-10:12:45]
	Transition from 2.044: you need to reboot and reconfigure (as usual, it's a problem in nHTTPd
		prefs management) and empty the cache.

26/10/2001 2.044 [Paul Guyot]
	Notes ending with .css, .js, .htm and .html no longer have their endlines translated to <BR>.

25/10/2001 2.043 [Paul Guyot]
	Various cleanups
	Added more verbose outputs when kDebugOn is TRUE
	Any note with a title ending with .css or .js or .htm or .html won't be translated to HTML.
		However, I think that the MIME type sent is text/html. Bah, this is just a temporary change
		before NPDS 3.0 alpha 1.

30/04/2001 2.042 [Paul Guyot]
	Fixed the search result bug (the result links had an additional unwanted .nsd)

05/08/2000 2.041 [Paul Guyot]
	Fixed the aesthetic problem with NetHopper when using the post note form.
	Fixed the index bug. Now NoteServ doesn't use the title index. It may do in the future
		use it if every store has this index.

07/07/2000 2.04 [Paul Guyot]
	Added the fix and enhancement in 2.021 (see below)
	Added a second naming convention. /html/notestitle. The title shouldn't end with .nsd
		%xx are translated.
	There are now two folders, one for bulletin board (posts), one for public space. They can be the
		same, though. Adding interface for more than 1 generic public space is possible.
	Added author in posted notes' list
	Some optimization of the code when it is related to data queries.
	Some additional optimization.

14/06/2000 2.03 [Paul Guyot]
	(based on 2.02)
	HTML code is said to be faster when lowercase. Some more changes to be more XML friendly.
		(Would be useful if we implement a WAP server.)
	Added the e-mail field in the whiteboard form, so that posters would leave more often their
		address.
	A function in the functions file seemed unused. I commented it out.
	I also set the Use Compression flag. (Hey, I don't have gigs on my Newton).

24/04/1999 2.021 [Matt Vaughn]
	When you were serving under NPDS and went to edit your Notes, the changes you
		make were reversed the next time the page was served. This was due to a faulty
		strategy of mine in dealing with the NewtonOS Soup system. In this release, this
		problem has been completely fixed.
	There are times when it is good to have the NoteServ cache turned off. This
		version of Noteserv allows you to specify cache on or off. 
	One of these is when you are low on Storage space and have a fast Newton. You
		probably don't need it then although it'd speed things up about 15%.
	Another case is when you are tweaking the layout or text of a note and you are
		reloading it multiple times in your browser.  With the cache on, the minimum
		time before a change will be recognized and sent to the browser is 1 minute.
		This can get frustrating if you're changing/reloading rapipdly in succession.
		In this case, flush the cache, then turn off cacheing for the duration of your
		editing session.  When finished, make sure to turn the cache back on if you
		want caching to occur.

20/04/1999 2.02 [Matt Vaughn]
	May have fixed the reluctance of NoteServ to let you change the index.html file
		while the server is active.
	Fixed a bug where carriage return/line feeds (CR/LF) in the text of a note would
		shut down the HTTP connection
	Publicly announced the admin-post method of adding notes by administrator.
	Added HTML 4.0/CSS support




