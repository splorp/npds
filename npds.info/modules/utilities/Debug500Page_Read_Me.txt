READ ME: Debug 500 Page

This module patches NPDS's default 500 page by adding the name of the exception
that occurred. It might be improved in the future to further track down bugs in
NPDS code, for example by displaying the stack trace or whatever.

The interface is straightforward: install it on the same store than NPDS. Freeze
it or delete it whenever you no longer want to display the exception that
occurred.

Known bugs:
- if the package is on a card, you might get a "Please Insert This Card" alert
if you eject the card.

Change History:

26/11/2006	1.0 [Paul Guyot]
	Initial release.
