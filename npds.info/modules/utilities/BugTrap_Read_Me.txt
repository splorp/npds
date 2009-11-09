BugTrapª  --   Tactile Systems, Inc.
Written by: Rob Bruce (rbruce@tactile.com)

Version History
---------------
1.6 3/4/98 - Fixed a couple minor formatting problems. Enchanced the report output: More GetPkgRefInfo and Gestalt added to the report. Added user text, since people are forgetting to explain what happened to cause the crash.
1.5 2/8/98 - Fixed a problem with the object to String function. Changed patches to write and display to be instantaneous, to hopefully prevent lockups on freezing. Better searching of the stack for bugs in try... onexceptions...
1.4 2/6/98 - Added DebugHashToName to the package, and added a debug slot into the GetCurrentReceiver. Improves the frames usefulness. Changed GetCurrentReceiver to handle non-package throws better.
1.3 Updated readme.txt & Added version to the notes, Fixed a looping problem, added code to truncate long strings, binaries, arrays and frames. length > 50 for binaries and strings. length > lines for arrays, and frames
1.2 Fixed problem where symbols with | were shown as \| instead i|iter was i\|iter.
1.1 Fixed problem where strings were displayed without their ""'s around them.
1.0 Initial Public Release


Why BugTrap?
-----------
The idea of BugTrap is provide users a way of giving developers an accurate log of a crash occurring on their Newton. 

Rather than sending the developer a backup of their Newton, BugTrap should be a initial substitute for the developer hooking the users Newton up to NTK for debugging.

Our hope is that 90% of the problems out there can be fixed with the information this can provide the developer.

Power to the users!


How does it work?
----------------
It relies entirely on a publicly accessible Newton Developer Tool, released by Apple for free called NSDebug tools. <http://www.newton-inc.com/dev/>

By patching the following functions, it is able to create a new note with output from NSDebug tools. (functions.write, functions.display, functions.breakloop, and getroot().ActionNotify)

Since it does operate by patching methods in the system, it's possible that there might be a conflict with other packages which use those same functions.
Example: Exception Printer:DTS overrides the Getroot().ActionNotify and doesn't call the inherited method, so BugTrap will stop working if EP is installed after it.
Also, since it currently dumps the log to 'Notes', if the problem is with 'Notes' strange things might occur.


What's it cost?
--------------

Nothing, nada, zippo, FREE... 
If you really want to pay us for it, we will accept donations of $15 as a way of saying thanks.
303-841-1114

Don't blame us if something goes wrong. Let us know so we can fix it.
Get the latest version from <http://www.tactile.com/bugTrap.html>

Is it safe?
----------

We designed it to be simple and safe. It's possible that it might not work. It's possible your newton might lock.
It's possible that you might get a problem with Notes, since it's adding items to notes after a crash.
With the exception of adding a note, everything it does can be undone by resetting your Newton.
Backup your newton to be safe.


Getting Started!
---------------
1) Install BugTrap on your Newton. Currently, it's two packages in one. BugTrap and NSDebug are bound together, since NSDebug is required.

You should hear a 'Poof!' when BugTrap patches itself.

2) Reproduce the crash.

You should hear another 'Poof!' when the note is created.

3) Email the developer the note

There should be a line in the note like...
"Crashed in function from: BugTrap:Tactile"

BugTrap attempts to point the finger at the offending application. In this case you'd email us the note.
Note: It's possible one application is crashing because of the presence of another. In a situation like this, I'm sure both developers would like to see the note log.


Frequently Asked Questions
--------------------------
Q) After I reset, the prefs inside BugTrap are back to the defaults. What gives?
A) We're not saving anything extra in a soup in the newton, to avoid possible crash causing behavior.

Q) What does the setting 'Use VBO vs Heap' mean?
A) In low memory conditions, the function that builds up the text for the note will create a VBO to store the text in. This setting determines how low the memory should be before it creates a VBO.

Q) Why not always use VBO's? 
A) They really slows down the crash.

Q) What does the setting 'Current Frame Size' mean? 
A) BugTrap attempts to build a better GetCurrentReceiver frame for the developer. Normally, getcurrentreceiver returns something useless, and you have to go into the protos... BugTrap tries to do this automatically, but needs some sort of restraint so it doesn't make a huge frame. This setting determines how many slots it'll add to the frame before it stops.

Q) Why not set 'Current Frame Size' to something huge?
A) It's possible that you'll run out of memory.

Q) What's the slot "frame truncated: ..." doing in my get current receiver frame.
A) It indicates that BugTrap stopped gathering lines because the 'Frame Size' limit was hit.

Q) What does the setting 'Object Flattener Depth' mean? 
A) When turning a NewtonScript frame into a blob of text, some decision has to be made on how deep to go. If you want more info on some of the complicated objects, increase this number.

Q) Why not always have 'Object Flattener Depth' set to 3? The more the better right?
A) More is better, but you could also run out of memory.

Q) Why not always have 'Object Flattener Depth' set to 3? The more the better right?
A) More is better, but you could also run out of memory.

Q) What's "Crashing inside a 'Try'" all about?
A) If a developer thinks that something they're about to do might crash, they'll put the calls inside a 'Try.. Onexception' block. So, the newton throws, but doesn't report the error the user. Most of the times this works just fine. On occasion however, something might not be working, but it also might not be throwing an exception. This may be because some code has a try statement. In versions before 1.4 we only Trapped Crashes inside of Trys if they were on the same level as the crash. Now we can search the entire stack for 'Try' blocks. If you liked the way it worked before then change the setting to "Trapped on the same level as the crash".

Q) I like this BugTrap so much, I want to know when it's updated.
A) Watch our web page, or you can mail majordomo@tactile.com an email with subscribe products in the body.


Developer Questions
-------------------
Q) When I'm connected to the inspector no notes are created, what's up?
A) It doesn't create a note when connected to the inspector. 

Q) When I'm connected to the inspector tons of extra stuff is printed to the inspector.
A) BugTrap uses the write commands to collect the data. Sorry, remove BugTrap it if it bothers you.

Q) A user emailed me a note from a crash in my app, but I can't make heads or tails out of this gibberish, what gives?
A) Read up on NSDebug Tools, in particular.. DisHere(), QuickStackTrace() and GetCurrentReceiver();

Q) Why are the frames, arrays, strings, binaries truncated?
A) We need to keep the length of the notes down. Figured most people don't need to see the entire function to figure out what's wrong.

Q) "Current Receiver from level:" prints a frame which isn't in my app.
A) Right, I'm making a nicer temp frame, based on the result of GetCurrentReceiver(level) and printing that. It's made out of slots in the receiver, and the receivers's proto. I try to choose slots which will help you find the crashing view. I omit stuff which is in the ROM.

Q) I understand, "Current Receiver from level: X". But what's "Packages Receiver from level: X"?
A) If your function at level 4 crashes a rom function at level 1. We'll debug level 1, hoping it's more informative. However, we'll also print getCurrentReceiver(4) which we labeled "Packages Receiver from level: 4", so you can find the offending view. But we only print it, if it's different from the GetCurrentReceiver(1).

Q) You should add <something> to the note, I need it to know what's going on.
A) email suggestions to <rbruce@tactile.com>

Q) Why doesn't GetAllNamedVar(level) get printed every time?
A) Non-debug builds don't return anything. Send the user a DebugON version.

Q) Your object to string function doesn't seem to know how to handle objects of type <blah>. I get a blank space where the object should be.
A) "Wild, wacky stuff. I did not know that." email me a sample please.

Q) I got a note from a user which doesn't start with "Sorry a problem occured. (-?????)" I need that to figure out what went wrong.
A) Sorry, this error wasn't trapped by the system. If anyone knows a line in the inspector to get the exception number tell me.

Q) My app generates lots of BugTrap notes during normal operation. I'm getting annoyed by my users complaining.
A) Email me your AppName, and we'll add a list to this readme. We can also send you a version of this app which doesn't catch non surfacing bugs.

Q) BugTrap conflicts with....
A) email problems to <rbruce@tactile.com>

Q) Can BugTrap do anything NTK can't?
A) Not much, but we did find bugs in our powerOnScripts with it.


Dissecting the log...
------------------ 

I'm going to attempt to describe what's in a sample log... 

>	Sorry a problem has occurred 
>	(-48803)

The above lines will get captured if the 'Sorry a problem has occurred' Dialog pops.
If the error is an non-surfacing one, you won't get the error number. We're trying to fix this. 

>Crashed in function from: xWord BuddyII:Tactile

BugTrap iterates through the stacktrace hunting for a Non-ROM based function.
If it find one it assumes that the function it found is to blame and this line is printed.

>Packages Receiver from level: 1
>{prefsView: {#5},
>_Prefs: <_function: 1 arg),
>GetAppPreferences: <_function: 0 args>,
>addPuzzle: <_function: 1 arg),
>superSymbol: '|xWord BuddyII:Tactile|,
>viewSetupDoneScript: <_function: 0 args>,
>appObjectUnfiled: "Unfiled Puzzles",
>viewQuitScript: <_function: 0 args>,
>appAll: "All Puzzles",
>testFunc: <_function: 0 args>,
>GetRouteScripts: <_function: 1 arg),
>more: ...,
>}

The above lines won't always print out. It only prints if the call to GetCurrentReceiver(level) returns a different frame between the levels of the package's functions, and the function it's going to decompile.
The section 'level: 1' indicates that the GetCurrentReceiver was taken from level 1 (main.testFunc():9).

>(#64B).Retarget(0), 0:	FindVar dataCursor

This is the function which crashed, it's the result of a call to Where();

>Stack Trace:
>(#64B).Retarget(0):0
>main.testFunc():9
><function, 0 arg(s) #C420F3D>():6

This is the stackTrace. It's a result of QuickStackTrace().
In this case, level 1 is 'main.testFunc():9' and is the function which 'caused' the error. It's from our package.
However, the error occurs actually occurs in level 0 '(#64B).Retarget(0):0'. Since we can disassemble level 0, where the error occurred, we will.
To learn more about it read up on NSDebug Tools.

>Debugging at stack level: 0

The next few lines are all based on level = 0.

>Current Receiver from level: 0
>{viewBounds: {#4},
>masterSoupSlot: puzzleSoup,
>name: "Overview",
>Abstract: <_function: 2 args>,
>menuRightButtons: [#3],
>checkAll: <_function: 0 args>,
>UnCheckAll: <_function: 0 args>,
>debug: "overview",
>keyString: "",
>}

This is the result of the call to 'GetCurrentReceiver(0)'. The frame which is show isn't actually from the crash. 
BugTrap creates it, from the frame returned by 'GetCurrentReceiver', it is supposed contain slots which are more relevant towards determining which 'view' contains the bad code.


>    0: FindVar              dataCursor
>    1: Push                 'entry
>    2: Send                 0
>    3: SetVar               [ 4 ]
>    4: GetVar               [ 4 ]
>    5: Push                 'deleted
>    6: Equal                2
>    7: BranchIfNotNil       16
>   10: GetVar               [ 4 ]
>pc = 0

This is the result of a call to Dishere(). The program counter's position is displayed at the bottom of the listing.
This particular function was called with the wrong number of args, which explains why the program counter is before the function.
Read up on NSDebug tools Disasm output to make sense of this stuff.

>(#64B).Retarget(0), 0: FindVar dataCursor

This is reprinted by NSDebugTools, we didn't want it at the bottom so we moved it. ;)


Comments
--------

Get the latest version from our website. <http://www.tactile.com>
You can contact us at info@tactile.com or 303-841-1114

We'd like to make the program better, so email us your suggestions.
If we don't take them, sorry in advance. It is free so you get what you pay for. ;)
