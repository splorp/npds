README for NPDSTracker 0.1
==========================

Requirements:
~~~~~~~~~~~~~

- Sun Java 1.1 or better
	Java 1.3 or higher is HIGHLY RECOMMENDED due to DNS caching bugs in 
	prior Java versions.  If you have to use Java 1.2 there is a 
	workaround: add the parameter "-Dsun.net.inetaddr.ttl=0" to the 
	Java command-line.

- Connection to the internet
	NPDSTracker has an official TCP port assigned by the IANA: 3680.
	Many trackers also listen on port 80 or 8080.

- Text editor



File List:
~~~~~~~~~~

readme.txt			you're reading it now
npdstracker.java	tracker source code
npdstracker.ini		settings file read at startup
template.html		tracker page template
npdscmd.txt			initial tracker commands.  Tracker will write 
					currently tracked servers to this file on shutdown.
startnpds.sh		example startup script for UN*X like OSes.



Setup:
~~~~~~

- Assuming you already have the Java SDK setup properly, open a command line
and compile the server:

javac npdstracker.java

- Edit npdstracker.ini and change any settings you see fit.  Pay attention to
the log settings - NPDSTracker logs are rather verbose and can become quite
large over time.  You can turn logging off in the INI file once you are sure
your server is configured properly.  Also add any trackers which you want to
share records with.

- Start the server at the command line:

java npdstracker
or, for Java 1.2.x:
java -Dsun.net.inetaddr.ttl=0 npdstracker

- Test connecting to the server.  Fire up a web browser and point it to

http://<server IP/hostname>:3680/

Configure and NPDS server to point to the tracker and make sure it can 
register.  Etc, etc...

Need support?  Join the NPDS mailing list: npds@ml.free.fr
