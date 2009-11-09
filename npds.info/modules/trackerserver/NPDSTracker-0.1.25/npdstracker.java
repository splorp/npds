// NPDSTracker for Java 1.2
// written by Victor Rehorst <chuma@chuma.org> and Paul Guyot <pguyot@kallisys.net>
// many thanks to Matt Vaughn <LightyearMedia@mac.com> for NPDS
// last modified 2001 June 6th

// CHANGELOG (version numbers are defined as major.minor.build)
// 0.1.25 [PG] Fixed the java.lang.IllegalArgumentException: timeout value is negative bug (well, I think)
// 0.1.24 [PG] Fixed the reload 0 bug.
//		- Fixed the lock bug.
//		- Set the timeout to 20 secs (was 10 secs) so my Newton is no longer considered as down ;)
//		- Added last validation template element.
// 0.1.23 [VR] No actual code changes, but to workaround a bug in JDK < 1.1
//		you must use Java 1.2 or better and set the sun.net.inetaddr.ttl
//		property to 0 on the command line, like this:
//		'java -Dsun.net.inetaddr.ttl=0 npdstracker'
// 0.1.22 [PG] Implemented the template stuff 
//		- Re-organized the ProcessQuery method
//		- There is now a single class with embedded sub classes (so we'll have a single .java binary)
//		- Improved answer to the GET request (with many headers now)
// 0.1.21 [PG] No longer uses the bugged URL interface to check if Newton servers are up. Instead, I use a Socket.
//		- the REGUP command tokenizer now accepts any standard token (and no longer only spaces which wasn't protocol-compliant)
//		- added several syntax checking with an appropriate status message.
// 0.1.20 [VR] Retrieving SHARE records from other trackers now works!!
// 0.1.19 [VR] Will now read optionsfile and cmdfile from default location
//		- fixed up options file parsing
//		- HTML now easily customizable (see npdstracker.ini, header.html, footer.html)
// 0.1.18 [VR] Minor admin console fixes
//		- implemented command-line arguments for logfile, cmdfile, optionsfile
//		- now can specify files to log to, read options from, or read initial commands from
//		- finally implemented LOGS command in admin console.
//		- code and syntax cleanups.
//        [PG] Use SimpleDateFormat in the ReturnRFCTime function. (this also fixes the GMT bug).
//		- Fixed a little HTML bug.
// 0.1.17 [VR] VTEST command added to the admin console
//		- SHARE command is now actually sent (forgot to flush PrintWriter)
//		- changed return code of SHARE command if sharing is disabled
// 0.1.16 [PG] The server now only sleeps in the validator loop.
// 0.1.15 [PG] Now the server always uses CRLF except for the log
//		- fixed a little bug in the validation date.
// 0.1.14 [PG] Now supports multiple connections
// 0.1.13 [VR] trying to fix intermittent bug in server validation code
// 0.1.12 [VR] more bugs in server validation fixed
// 0.1.11 [VR] added more features to GET code
//		-fixed up RFC times somewhat
//		-fixed bug in validation code with Connection Refused socket exceptions
// 0.1.10 [VR] fixed bug with exception passing and server socket code
//		-rewrote GET code to return a nice HTML table (HTML 4.01 compliant)
// 0.1.9  [VR] admin console is now complete save for the LOGS command
//		-now retrieves, updates, and properly handles records from other servers via
//		the SHARE command
// 0.0.8  [VR] added SHARE, ABOUT, VERIFY, and STATS command to admin console
//		-fixed the problem with the verification code repeating many times per minute
//		-added a REGUP counter for the STATS admin command
// 0.0.7  [VR] now accepts commands regardless of case
//		-checks for an existing host entry before processing a REGUP
//		-started implementing the ADMIN command for live configuration of the server
// 0.0.6  [VR] first implementation of SHARE command
//		-cleaned up some return codes
//		-fixed some status handling and checking
// 0.0.5  [VR] server validation actually works!  woohoo!
//		-implemented a socket timeout so that the loop doesn't get stuck
//		-removed the QHTML extended command
// 0.0.4  [VR] implemented Return Codes
// 0.0.3  [VR] Fixed GET method - now returns DTD HTML 2.0 compliant pages
//		-Rewrote internal storage of records - is now good and extensible
//		-Now we store time server last checked, and server status
//		-Implemented QueryMethod function for finding a record when we do a REGDN -
//		could also use this method for an possible SRCH command later
// 0.0.2  [VR] Added QHTML method for returning HTMLized results
//		-Added GET method support for returning a basic web page to web browsers

// GOALS for 0.1
// -be fully v1 protocol compliant
// -ability to save state when server is shutdown / restore state

import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

public class npdstracker extends Thread
{
	//////////////////////////////////////////////////////////////////////////////
	//	VARIABLES AND CONSTANTS
	//////////////////////////////////////////////////////////////////////////////

	// ======= Constants ======= //

	// Version Information
	public static final String serverdesc = "NPDSTracker for Java";
	public static final int majorversion = 0;
	public static final int minorversion = 1;
	public static final int build = 25;
	public static final int protocolversion = 1;
	public static final String versionStr = majorversion + "." + minorversion + "." + build;
	public static final String kServerStr = "Victor Rehorst's NPDS Tracker " + versionStr;
	public static final String kUserAgentStr = "Mozilla/5.0 (compatible; " + kServerStr + "; Java)";

	// 200, 400, 403 are HTTP codes - 202 is the "special" NPDS code
	public static final int HTTP_OK = 200;
	public static final int NPDS_OK = 202;
	public static final int HTTP_ERR = 400;
	public static final int HTTP_FORBID = 403;

	public static final int kValidateTimeUnit = 60000;	// a minute in milliseconds
	public static final int kRefreshTimeUnit = 1000;	// a second in milliseconds
	public static final int kTimeout = 20000;			// 20 seconds.
	public static final String defaultOptionsFile = "npdstracker.ini";
	public static final String defaultCmdFile = "npdscmd.txt";

	// Messages
	public static final String kRTFMStr = " Please check out the protocol before telnetting to the server (http://eddie.cis.uoguelph.ca/~victor/npds/";
	public static final String kAlreadyRegisteredStr = " host already exists in list";
	public static final String kNotRegisteredStr = " host is unknown";
	public static final String kWeirdPortStr = " Weird port (not an integer)";
	public static final String kWeirdPortValueStr = " Weird port (not within 1-65535)";
	public static final String kUnsupportedVersionStr = " This version of the protocol is not supported by this server";
	

	// ======= Variables ======= //

	// Server variables: time between validations, number of attempts to make before server is 
	// dropped, whether or not to share with other Trackers, hit counter, admin password, logfile location
	// all of these can be set at runtime using the -o switch
	public static int validateTime = 20;
	public static int validateTries = 3;
	public static boolean shareEnabled = true;
	public static String adminpasswd = "qwerty";
	public static int kPort = 2110;
	public static String logfile = "";
	public static String templateFile = "";

	// Runtime variables: these are the data structures and other values used by the server while it is running
	// I put all Newton info in a single Vector because I have a semaphore for accessing it and it is easier like this.
	public static Vector mHostInfoVector = new Vector();
	// Identically, I put all server info in a single Vector.
	public static Vector mSharingInfoVector = new Vector();
	public static int hitcounter = 0;
	public static int regcounter = 0;
	// Non 0 if a validation is in progress, 0 otherwise.
	public static int mValidationInProgress = 0;
	// Time the last validation ended (in RFC format):
	public static String mLastValidation = "never";
	
	// Validator and Server
	// (Victor uses a lot of public static variables, he surely has a good reason for that)
	public static TValidator mValidator;
	public static ServerSocket mServer;

	private static DateFormat mRFCGMTFormatter;
	static
	{
		mRFCGMTFormatter = new SimpleDateFormat("EEE',' d-MMM-yyyy HH:mm:ss 'GMT'", Locale.US);
		mRFCGMTFormatter.setTimeZone(TimeZone.getTimeZone("Africa/Casablanca"));
	}

	//////////////////////////////////////////////////////////////////////////////
	//	EMBEDDED CLASSES
	//////////////////////////////////////////////////////////////////////////////
	
	// ============================================================	//
	// * TQueryException: a class to handle exceptions when parsing the query *
	// ============================================================	//

	public static class TQueryException extends Exception
	{
	    public TQueryException(String s)
	    {
			super(s);
	    }
	}
	
	// ============================================================	//
	// * TValidator: a class to validate the Newton servers *
	// ============================================================	//

	public static class TValidator extends Thread
	{
		public Date	mLastCheck;
		public Date	mNextCheck;

		public void run ()
		{
			// Initialization of variables to know when to check the registered Newtons.
			Date serverstarted = new Date();
			mLastCheck = serverstarted;
			mNextCheck = serverstarted;
			mNextCheck.setTime(serverstarted.getTime() + (npdstracker.validateTime * npdstracker.kValidateTimeUnit));

			// I'm looping forever. (I will be killed by the application as it quits).
			
			try {
				while (true)
				{
					try 
					{
						Date now = new Date();
						
						if (now.after(mNextCheck))
						{
							// get the current date
							mLastCheck = new Date();
							mNextCheck.setTime(mLastCheck.getTime() + (npdstracker.validateTime * npdstracker.kValidateTimeUnit));
							npdstracker.validateServers();
						}
						
						now = new Date();	// Updates the now value.
						
						// Let's sleep until next check if we have to.
						
						long timeout = mNextCheck.getTime() - now.getTime();
						if (timeout > 0)
							sleep( timeout );
					} catch (InterruptedException e) {
						// Ignore any interrupt.
					}
				}	// while (true)
			} catch (Exception e)
			{
				// Oops, some exception occured.
				npdstracker.logMessage("TValidator: Exception " + e + " occurred");
			}
		}
	}

	// ============================================================	//
	// * TConnection: a class to handle a connection the Newton servers *
	// ============================================================	//

	public static class TConnection extends Thread
	{
		// Variables
		private Socket mSocket;
		
		// Constructor
		TConnection ( Socket inSocket )
		{
			mSocket = inSocket;
		}
		
		// Thread entry point
		public void run ()
		{
			try {
				mSocket.setSoTimeout( npdstracker.kTimeout );
			
				BufferedReader in = null;
				PrintWriter out = null;
				String result;
				try
				{
					in = new BufferedReader( new InputStreamReader( mSocket.getInputStream() ) );
					out = new PrintWriter(new OutputStreamWriter( mSocket.getOutputStream() ) );
					npdstracker.logMessage("Handling connection from " + mSocket.getInetAddress().toString() );
					result = in.readLine();
					// pass the command to the query processor
					if (result != null)
					{
						result.toUpperCase();
						npdstracker.ProcessQuery( result, in, out, mSocket );
					}
				}
				catch (Exception e) 
				{
					System.err.println( "aah! some exception!" );
					System.err.println( e );
					
					// Easier than calling System.getProperty("line.separator")
				}
			
				npdstracker.logMessage("Closing connection from " + mSocket.getInetAddress().toString());
				mSocket.close();
				System.gc();
			} catch (Exception e)
			{
				// Ignored.
			}
		}
	}

	// ============================================================	//
	// * THostInfo *
	// ============================================================	//
	// A class to handle a data about Newton servers
	// I need such a class because of semaphores (locks).

	public static class THostInfo
	{
		// The Newton's hostname (including IP)
		public String mName;	// The string as shown in the logs and in the table.
		public String mHost;	// The host name only (used to check the server)
		public int mPort;	// The port only (default is 80)
		// Unique ID of the Newton (NOT CURRENTLY USED)
		public String mHash;
		// Plaintext description of the Newton
		public String mDesc;
		// Time this Newton was last validates (string in RFC format)
		public String mLastValidation;
		// Current status of this Newton: 0 is up, any other number is the number of unsuccessful 
		// attempts made to validate, -1 is a SHAREd record
		public int mStatus;
		
		// Unused yet. Because if a tracker dies, I may need to warn the Newton and tell it that
		// personally, I am up. (sounds cool, doesn't it?)
		public TServerInfo mServer;
	}

	// ============================================================	//
	// * TServerInfo *
	// ============================================================	//
	// A class to handle a data about other tracker servers
	// I need such a class because of semaphores (locks).
	public static class TServerInfo
	{
		// List of external trackers to get data from
		public String mHost;
		public String mPort;
	}

	//////////////////////////////////////////////////////////////////////////////
	//	UTILITY FUNCTIONS
	//////////////////////////////////////////////////////////////////////////////
	
	// ====================================================================	//
	// * void logMessage( String ) [static]
	// ====================================================================	//
	// Sends a message to the System.out or the logfile, if specified
		
	public static void logMessage(String message)
	{
		Date theDate = new Date();
		if (logfile.equals(""))
			System.out.println(theDate.toString() + "-> " + message);
		else
		{
			try {
				FileWriter outlogfile = new FileWriter(logfile, true);
				outlogfile.write(theDate.toString() + "-> " + message + "\r\n");
				outlogfile.flush();
				outlogfile.close();
			} catch (IOException e) {System.out.println(theDate.toString() + "-> FATAL - can't write to logfile: " + logfile);}
		}
	}

	// ====================================================================	//
	// * int QueryRecord( String ) [static, private]
	// ====================================================================	//
	// Given a name, returns the host index or -1 if it cannot be found.

	private static int QueryRecord (String host)
	{
		// Looks for a host with a given name.
		// Searches the mHostInfoVector list and returns an index or -1
		int index_i;
		
		synchronized (mHostInfoVector)
		{
			for (index_i = 0; index_i < mHostInfoVector.size(); index_i++)
			{
				THostInfo theInfo = (THostInfo) mHostInfoVector.elementAt(index_i);
				if (host.equals(theInfo.mName))
					return index_i;
			}
		}
		return -1;
	}

	// ====================================================================	//
	// * void ReturnCode( int, String, PrintWriter ) [static, private]
	// ====================================================================	//
	// Given a code and a message, outputs a NPDS/TP status code line.

	private static void ReturnCode(int codetype, String message, PrintWriter out)
	{
		out.print(codetype);
		out.flush();
		if ((codetype == HTTP_OK) || (codetype == NPDS_OK))
		{
			out.print(" OK" + message + "\r\n");
			out.flush();
		}
		else if (codetype == (HTTP_ERR))
		{
			out.print(" Bad Request" + message + "\r\n");
			out.flush();
		}
		else
		{
			out.print(" Undefined status" + message + "\r\n");
			out.flush();
		}
	}

	// ====================================================================	//
	// * String ReturnRFCTime( Date ) [static, private]
	// ====================================================================	//
	// Given a date, returns a RFC #1123 string.
	// (cf http://www.faqs.org/rfc/rfc1123.txt)

	private static String ReturnRFCTime(Date thisdate)
	{
		synchronized (mRFCGMTFormatter)
		{
	            return mRFCGMTFormatter.format(thisdate);
        	}

	}

	// ====================================================================	//
	// * String ReturnAbout( PrintWriter ) [static, private]
	// ====================================================================	//
	// Outputs to PrintWriter the about message. (the format is defined in the NPDS/TP protocol)

	private static void ReturnAbout(PrintWriter out)
	{
		logMessage("Processing ABOUT command");
		out.print("protocol: " + protocolversion + "\r\n");
		out.print("period: " + validateTime + "\r\n");
		out.print("tries: " + validateTries + "\r\n");
		out.print("share: " + shareEnabled + "\r\n");
		out.print("about: " + serverdesc + " version " + versionStr);
		out.print(" running on Java " + System.getProperty("java.version") + " by " + System.getProperty("java.vendor") + " ");
		out.print(System.getProperty("os.name") + " " + System.getProperty("os.arch") + " " + System.getProperty("os.version") + "\r\n");
		out.flush();
	}

	// ====================================================================	//
	// * void printUsage( void ) [static, private]
	// ====================================================================	//
	// Outputs to PrintWriter the about message. (the format is defined in the NPDS/TP protocol)

	private static void printUsage()
	{
		System.out.println("Java NPDS Server Tracker version " + versionStr);
		System.out.println("Usage: java npdstracker [-h] [-c cmdfile] [-o optionsfile]");
		System.out.println("       -h : print this help");
		System.out.println("       -c cmdfile : specify commands to run at start (defaults to none)");
		System.out.println("       -o optionsfile : specify options file (defaults from compile time)");
	}

	// ====================================================================	//
	// * void ParseOptionsFile( String ) [static, private]
	// ====================================================================	//
	// Reads the server options from the INI file and parses the appropriately.

	private static void ParseOptionsFile(String optionsfile) throws FileNotFoundException, IOException
	{
		BufferedReader optionsreader = new BufferedReader (new FileReader(optionsfile));
		String tempoption = "";
		int linenumber = 1;
		tempoption = optionsreader.readLine();
		while (tempoption != null)
		{
			StringTokenizer st = new StringTokenizer(tempoption, " ");
			String garbage = "";
			try {
				garbage = st.nextToken();
			} catch (NoSuchElementException e) {}
			try {
			if (tempoption.startsWith("kPort"))
			{
				garbage = st.nextToken();
				if (!(garbage.equals("=")))
					logMessage("error reading optionsfile, line " + linenumber);
				else
					kPort = Integer.parseInt(st.nextToken());
			}
			else if (tempoption.startsWith("adminpasswd"))
			{
				garbage = st.nextToken();
				if (!(garbage.equals("=")))
					logMessage("error reading optionsfile, line " + linenumber);
				else
					adminpasswd = st.nextToken();
			}
			else if (tempoption.startsWith("validateTime"))
			{
				garbage = st.nextToken();
				if (!(garbage.equals("=")))
					logMessage("error reading optionsfile, line " + linenumber);
				else
					validateTime = Integer.parseInt(st.nextToken());
			}
			else if (tempoption.startsWith("validateTries"))
			{
				garbage = st.nextToken();
				if (!(garbage.equals("=")))
					logMessage("error reading optionsfile, line " + linenumber);
				else
					validateTries = Integer.parseInt(st.nextToken());
			}
			else if (tempoption.startsWith("shareEnabled"))
			{
				garbage = st.nextToken();
				if (!(garbage.equals("=")))
					logMessage("error reading optionsfile, line " + linenumber);
				else
					shareEnabled = Boolean.getBoolean(st.nextToken());
			}
			else if (tempoption.startsWith("shareServer"))
			{
				garbage = st.nextToken();
				if (!(garbage.equals("=")))
					logMessage("error reading optionsfile, line " + linenumber);
				else
				{
					TServerInfo theServerInfo = new TServerInfo();
					theServerInfo.mHost = st.nextToken();
					theServerInfo.mPort = st.nextToken();
					mSharingInfoVector.addElement(theServerInfo);
				}
			}
			else if (tempoption.startsWith("logfile"))
			{
				garbage = st.nextToken();
				if (!(garbage.equals("=")))
					logMessage("error reading optionsfile, line " + linenumber);
				else
					logfile = st.nextToken();
			}
			else if (tempoption.startsWith("pageTemplate"))
			{
				garbage = st.nextToken();
				if (!(garbage.equals("=")))
					logMessage("error reading optionsfile, line " + linenumber);
				else
					templateFile = st.nextToken();
			}
			else if (tempoption.startsWith("#") || garbage.equals(""))
			{
				// do nothing
			}
			} catch (NoSuchElementException e) { System.err.print("error in options parsing " + optionsfile + " line " + linenumber + "\r\n");}
			tempoption = optionsreader.readLine();
			linenumber++;
		}
	}

	// ====================================================================	//
	// String StrReplace( String, String, String ) [static, public]
	// ====================================================================	//
	// Replaces every occurences of a string by another string in a string.
	
	public static String StrReplace( String inString, String inPattern, String inReplacement )
	{
		String result = inString;
		int fromIndex = 0;
		while (true)
		{
			fromIndex = result.indexOf( inPattern, fromIndex );

			if (fromIndex == -1)
				break;
						
			int newIndex = fromIndex + inPattern.length();
			result = result.substring( 0, fromIndex ) + inReplacement + result.substring( newIndex );
			fromIndex = newIndex;
		}
		
		return result;
	}

	//////////////////////////////////////////////////////////////////////////////
	//	MAIN FUNCTIONS
	//////////////////////////////////////////////////////////////////////////////

	// ====================================================================	//
	// * void main( String[] ) [static]
	// ====================================================================	//
	// Main entry point.

	public static void main(String[] args)
	{
		String tempcmdfile = defaultCmdFile, tempoptionsfile = defaultOptionsFile;
		// parse command line options here
		// -l [logfile] -file to write logs to, will be created if it doesn't exists, otherwise appended to
		// -c [cmdfile] -file to read initial commands from (REGUPs)
		// -o [optionsfile] -file to read other options from
		for (int i = 0; i < args.length; i++)
		{
			if (args[i].equals("-c"))
			{
				tempcmdfile = args[i+1];
				i++;
			}
			else if (args[i].equals("-o"))
			{
				tempoptionsfile = args[i+1];
				i++;
			}
			else
			{
				System.out.println("invalid arguments");
				printUsage();
				System.exit(0);
			}
		}
		// start the tracker server
		npdstracker dathinghere = new npdstracker(tempcmdfile, tempoptionsfile);
		dathinghere.run();
	}

	// ====================================================================	//
	// * npdstracker() [private]
	// ====================================================================	//
	// Constructor
	
	private npdstracker(String tempcmdfile, String tempoptionsfile)
	{
		String tempcmd = "";
		try
		{
			if (!(tempoptionsfile.equals("")))
				ParseOptionsFile(tempoptionsfile);

			if (!(tempcmdfile.equals("")))
			{
				BufferedReader cmdreader = new BufferedReader(new FileReader(tempcmdfile));
				PrintWriter cmdwriter = new PrintWriter(new FileWriter(FileDescriptor.out));
				tempcmd = cmdreader.readLine();
				while (tempcmd != null)
				{
					ProcessQuery(tempcmd, null, cmdwriter, null);
					tempcmd = cmdreader.readLine();
				}
			}

			// Let's create the validation thread.
			mValidator = new TValidator();
			mValidator.start();
					
			mServer = new ServerSocket( kPort );

			// ServerSocket timeout: I'll wait forever until a connection arrives.
			// mServer.setSoTimeout( 0 ); // (this is default)
		} catch(Exception e)
		{
			logMessage("npdstracker: Exception " + e + " occurred");
		}

		// Hello message
		logMessage(serverdesc + " version " + versionStr + " started");
	}
	
	// ====================================================================	//
	// * void run () [private]
	// ====================================================================	//
	// Thread entry point.
	public void run ()
	{
		// I'm looping waiting for connections.
		while (true)
		{
			try 
			{
				// Wait until a connection arrives.
				Socket s = mServer.accept();
				
				// Let a connection object handle the socket
				TConnection thisConnection = new TConnection( s );
				thisConnection.start();

			} catch (Exception e) {;}
		}
	}

	// ====================================================================	//
	// void ProcessQuery( String, BufferedReader, PrintWriter, Socket ) [static]
	// ====================================================================	//
	// Function to handle the queries. (I need to have it public).

	public static void ProcessQuery(String query, BufferedReader in, PrintWriter out, Socket s) throws SocketException, IOException
	{
		// I get the query command.
		// I now use exceptions for a cleaner way to procede (at least, I think so)
		
		try {	// Global try.
			// tokenize me, baby
			StringTokenizer st = new StringTokenizer( query );
				// We now accept any standard delimiter, hence HT
				// (the protocol says we should)
				// We also accept the other delimiters, but anyway, there shouldn't be any other delimiter
				// in the host name or the REGUP command, so we don't mind.
			String theCommand = st.nextToken().toUpperCase();

			// I do handle several commands.
			
			if (theCommand.equals("ABOUT"))
			{
				ReturnCode(HTTP_OK, "", out);
				ReturnAbout(out);
			} else if (theCommand.equals("REGUP"))
			{
				logMessage("Processing REGUP command");

				// throw out the first token
				String hname = st.nextToken();
				String hdesc = query.substring(hname.length() + 7);	// "REGUP hname ". Note: this isn't really protocol 1.1 compliant.

				if (hname.equals("NPDS/TP"))
				{
					logMessage("v2 command recieved - not yet supported");
					throw new TQueryException ( kUnsupportedVersionStr );
				}
				else if ((QueryRecord(hname) == -1))
				{
					// Host is not in our list. Let's add it.
					
					THostInfo theInfo = new THostInfo();
					theInfo.mName = hname;
					// I process the host name (without the port) and the port if ever there is a colmun in the host.
					StringTokenizer host_st = new StringTokenizer(hname, ":");
					theInfo.mHost = host_st.nextToken();
					boolean portIsCorrect = true;
					if (host_st.hasMoreTokens())
					{
						try {
							theInfo.mPort = Integer.parseInt(host_st.nextToken());
						} catch (NumberFormatException theException)
						{
							logMessage("Server \"" + hname + " " + hdesc + "\" wasn't inserted into the list because its port isn't correct (not an integer)");
							throw new TQueryException ( kWeirdPortStr );
						}
						
						if ((theInfo.mPort < 1) || (theInfo.mPort > 65535))
						{
							logMessage("Server \"" + hname + " " + hdesc + "\" wasn't inserted into the list because its port isn't correct (not within 1-65535)");
							throw new TQueryException ( kWeirdPortValueStr );
						}
					} else {
						theInfo.mPort = 80;
					}
					
					theInfo.mDesc = hdesc;
					Date tempDate = new Date();
					theInfo.mLastValidation = ReturnRFCTime(tempDate);
					theInfo.mStatus = 0;

					// Synchronized is not required here because the addElement method is synchronized.
					mHostInfoVector.addElement( theInfo );
					
					ReturnCode(HTTP_OK, "", out);

					logMessage("Inserted \"" + hname + " " + hdesc + "\" into the list");
					logMessage(mHostInfoVector.size() + " hosts now in the list");
					regcounter++;
				}
				else
				{
					// host is already in our list, 
					logMessage("Did not insert \"" + hname + "\" into list - host is already registered");
					throw new TQueryException ( kAlreadyRegisteredStr );
				} // if ((QueryRecord(hname) == -1)) ... else 
			}
			else if (theCommand.equals("REGDN"))
			{
				logMessage("Processing REGDN command");

				String host = null;
				
				// throw out the first token
				host = st.nextToken();
					
				// Check there is no token left (there shouldn't be).
				if (st.hasMoreTokens())
				{
					logMessage("Bad syntax: the tokenizer found more than two elements");
					throw new TQueryException ( kRTFMStr );
				}
					
				int tempindex;
				
				// I need the tempindex, nobody should change the list before I do remove this element
				// But, because I don't want to lock the other connections, I'd better do only removal
				// in the synchronized statement
				synchronized (mHostInfoVector)
				{
					tempindex = QueryRecord(host);
					if (tempindex != -1)
					{
						mHostInfoVector.removeElementAt(tempindex);
					}
				}
				if (tempindex == -1)
				{
					logMessage("REGDN failed - host not found in list");
					throw new TQueryException ( kNotRegisteredStr );
				}
				else
				{
					ReturnCode(HTTP_OK, "", out);
					logMessage("Removed \"" + host + "\" from the list");
					logMessage(mHostInfoVector.size() + " hosts now in the list");
				} // if (tempindex == -1)
			}
			else if (theCommand.equals("QUERY"))
			{
				logMessage("Processing QUERY command");
				ReturnCode(NPDS_OK, "", out);
				// print out all of the Newtons registered
				synchronized (mHostInfoVector)
				{
					for (int index_i = 0; index_i < mHostInfoVector.size(); index_i++)
					{
						THostInfo theInfo = (THostInfo) mHostInfoVector.elementAt(index_i);

						out.print(theInfo.mName + " " + theInfo.mDesc + " " + theInfo.mLastValidation + " " + theInfo.mStatus + "\r\n");
						out.flush();
					}
				}
			}
			else if (theCommand.equals("SHARE"))
			{
				// return list of entries in SHARE format
				logMessage("Processing SHARE command");
				if (shareEnabled == true)
				{
					ReturnCode(HTTP_OK, "", out);
					synchronized (mHostInfoVector)
					{
						for (int index_i = 0; index_i < mHostInfoVector.size(); index_i++)
						{
							THostInfo theInfo = (THostInfo) mHostInfoVector.elementAt(index_i);
							if (!(theInfo.mStatus == -1))
							{
								out.print("address: " + theInfo.mName + "\tlast-verified: "
									+ theInfo.mLastValidation + "\t");
								String statstring;
								if (theInfo.mStatus == 0)
									statstring = "UP";
								else
									statstring = "DOWN";
								out.print("status: " + statstring + "\tdescription: " + theInfo.mDesc + "\r\n");
								out.flush();
							}
						}
					}
				}
				else
				{
					ReturnCode(HTTP_FORBID, " server not sharing records", out);
					out.print("no-entries" + "\r\n");
					out.flush();
				}
			}

			else if (theCommand.equals("GET"))
			{
				String HTTPDocStr = st.nextToken();
				
				// return a WEB PAGE
				logMessage("Processing GET command");
				out.print("HTTP/1.0 ");
				ReturnCode(HTTP_OK, "", out);
				hitcounter++;

				// I prepare the result strings.
				// Find out what my address is. I use the host header if present.
				String urlStr = s.getLocalAddress().getHostName();
				String requestLine = in.readLine();
				while ((requestLine != null) && (requestLine.length() > 0) )
				{
					StringTokenizer headerTk = new StringTokenizer( requestLine, ": \t" );
					if (headerTk.hasMoreTokens() == false)
					{
						break;
					}
					String theHeader = headerTk.nextToken().toLowerCase();
					if (theHeader.equals("host"))
					{
						// Cool, I have a host header.
						urlStr = "http://" + headerTk.nextToken();
						break;
					}
					
					requestLine = in.readLine();
				}
				
				if (kPort != 80)
					urlStr += ":" + kPort;
				
				urlStr += HTTPDocStr;

				String tableStr = "<table border=\"1\" width=\"100%\" summary=\"List of online Newtons\">\r\n<tr><th width=\"10%\">Active</th><th width=\"75%\">Server Name</th><th>Last Verified</th></tr>\r\n";
					
				int index_i;
				synchronized (mHostInfoVector)
				{
					for (index_i = 0; index_i < mHostInfoVector.size(); index_i++)
					{
						tableStr += "<tr valign=\"top\"><td><p align=\"center\"><b>";
						THostInfo theInfo = (THostInfo) mHostInfoVector.elementAt(index_i);
						switch ( theInfo.mStatus )
						{
							case -1:
								tableStr += "UP-sharing";
								break;
							
							case 0:
								tableStr += "UP";
								break;
								
							default:
								tableStr += "DOWN";
								break;
						}
		
						tableStr += "</b></p></td><td><a href=\"http://" + theInfo.mName
							+ "\">" + theInfo.mDesc + "</a></td>";
						tableStr += "<td>" + theInfo.mLastValidation + "</td></tr>\r\n";
					}
				} // synchronized (mHostInfoVector)
				if (index_i == 0)
				{
					tableStr += "<tr valign=\"top\"><td colspan=\"3\"><p align=\"center\"><i>No Newtons have registered with this Tracker.</i></p></td></tr>\r\n";
				}
				tableStr += "</table>\r\n";	

				String validateTimeStr = Integer.toString( validateTime );
				String hitCounterStr = Integer.toString( hitcounter );
				// Refresh is validate time / 2
				long refreshCount = (npdstracker.validateTime * npdstracker.kValidateTimeUnit ) / (kRefreshTimeUnit * 2);
				
				String metaRefreshStr = "<meta http-equiv=\"refresh\" content=\"" + refreshCount + "; url=" + HTTPDocStr + "\">\r\n";

				out.print( "Refresh: " + refreshCount + "; url=" + HTTPDocStr + "\r\n" );
				out.print( "Server: " + kServerStr + "\r\n" );

				Date now = new Date();

				out.print( "Date: " + ReturnRFCTime( now ) + "\r\n" );
				out.print( "Last-Modified: " + ReturnRFCTime( mValidator.mLastCheck ) + "\r\n" );
				out.print( "Content-type: text/html\r\n\r\n" );

				String lastValidationStr;
				if (mValidationInProgress != 0)
				{
					lastValidationStr = "Validation is in progress.";
				} else {
					lastValidationStr = "Last validation: " + mLastValidation + ".";
				}

				BufferedReader template = new BufferedReader (new FileReader(templateFile));
				String templateLine = template.readLine();
				while (templateLine != null)
				{
					// I replace the following sgml tags. (note: this isn't pure sgml as my tags can be inside other tags arguments)
					// <servers/>			-> the table of the servers
					// <validate-time/>		-> the time (in minutes) between validations
					// <hit-counter/>		-> the number of hits since last restart
					// <url/>				-> the url of this server (used reading the host header, useful for w3 syntax check button)
					// <meta-refresh/>		-> meta-HTTP equiv refresh line (remark: the refresh line is sent in the HTTP headers)
					// <http-doc/>			-> What comes after the GET (usually "/")
					// <version/>			-> Returns the version (e.g. 0.1.2221)
					// <last-validation/>	-> Last validation: <foo> or "Validation is in progress."
					
					templateLine = StrReplace( templateLine, "<servers/>", tableStr );
					templateLine = StrReplace( templateLine, "<validate-time/>", validateTimeStr );
					templateLine = StrReplace( templateLine, "<hit-counter/>", hitCounterStr );
					templateLine = StrReplace( templateLine, "<url/>", urlStr );
					templateLine = StrReplace( templateLine, "<meta-refresh/>", metaRefreshStr );
					templateLine = StrReplace( templateLine, "<http-doc/>", HTTPDocStr );
					templateLine = StrReplace( templateLine, "<version/>", versionStr );
					templateLine = StrReplace( templateLine, "<last-validation/>", lastValidationStr );

					out.print(templateLine + "\r\n");
					templateLine = template.readLine();
				}

				out.print( "\r\n\r\n" );
				out.flush();
			}
			else if (query.startsWith("ADMIN"))
			{
				logMessage("processing ADMIN command");
				try {
					String password = st.nextToken();
					if (password.equals(adminpasswd))
						adminConsole(in, out, s);
					else
						throw new TQueryException( " Incorrect admin password" );
				} catch (NoSuchElementException e)
				{
					throw new TQueryException( "" );
				}
			}
			else
				throw new TQueryException("");			
		} catch (NoSuchElementException theException)
		{
			logMessage("Bad syntax");
			ReturnCode( HTTP_ERR, kRTFMStr, out );
		} catch (TQueryException theQueryException) {
			ReturnCode( HTTP_ERR, theQueryException.getMessage(), out );
		}
	}

	// ====================================================================	//
	// void adminConsole( BufferedReader, PrintWriter, Socket ) [static, private]
	// ====================================================================	//
	// Function to handle administrator console commands.
	
	private static void adminConsole(BufferedReader in, PrintWriter out, Socket s) throws SocketException, IOException
	{
		// funky cool admin console
		s.setSoTimeout(0);
		out.print("Welcome to the NPDSTracker admin console!  Type HELP for help.\r\n");
		out.flush();
		String commandline = "";
		while (!(commandline.startsWith("Q")) || !(commandline.startsWith("q")))
		{
			out.print("> ");
			out.flush();
			commandline = in.readLine();
			commandline.toUpperCase();
			if (commandline.equals("HELP"))
			{
				out.print("Valid commands are:\r\n");
				out.print("ABOUT     show the server's current settings.\r\n");
				out.print("HALT      stop the server (asks for confirmation)\r\n");
				out.print("LOGS      dump the server's logs\r\n");
				out.print("SHARE     change the server's share settings\r\n");
				out.print("SLIST     view/modify the list of servers to get SHARE records from\r\n");
				out.print("VTEST     trigger a server validation now\r\n");
				out.print("STATS     show the server's statistics\r\n");
				out.print("VERIFY    change the server's NPDS verification settings\r\n");
				out.print("QUIT      quit the admin console and close the connection\r\n");
				out.flush();
			}
			else if (commandline.equals("QUIT"))
			{
				out.print("Bye!\r\n");
				out.flush();
				break;
			}
			else if (commandline.equals("ABOUT"))
				ReturnAbout(out);

			else if (commandline.equals("HALT"))
			{
				out.print("Are you sure? (y/n)\r\n");
				out.flush();
				String confirm = in.readLine();
				if (confirm.startsWith("y") || confirm.startsWith("Y"))
				{
					logMessage("server shutting down via admin console");
					System.gc();
					System.exit(0);
				}
			}
			else if (commandline.equals("LOGS"))
			{
				if (logfile.equals(""))
				{
					out.print("Sorry, logs can only be read remotely if they are being written to a file (they aren't).\r\n");
					out.flush();
				}
				else
				{
					out.print("start your terminal's capture feature, then hit enter.\r\n");
					out.flush();
					String foo = in.readLine();
					BufferedReader dumplogs = new BufferedReader (new FileReader(logfile));
					foo = dumplogs.readLine();
					while (foo != null)
					{
						out.print(foo + "\r\n");
						out.flush();
						foo = dumplogs.readLine();
					}
				}
			}
			else if (commandline.equals("SHARE"))
			{
				if (shareEnabled)
				{
					out.print("Sharing is currently enabled (TRUE)\r\n");
					out.flush();
				}
				else
				{
					out.print("Sharing is currently disabled (FALSE)\r\n");
					out.flush();
				}
				out.print("Set sharing (TRUE/FALSE): ");
				out.flush();
				String confirm = in.readLine();
				if (confirm.startsWith("T") || confirm.startsWith("t"))
				{
					shareEnabled = true;
					out.print("Sharing is enabled\r\n");
					out.flush();
				}
				else if (confirm.startsWith("F") || confirm.startsWith("f"))
				{
					shareEnabled = false;
					out.print("Sharing is disabled\r\n");
					out.flush();
				}
				else
				{
					out.print("Sharing setting unchanged\r\n");
					out.flush();
				}
			}
			else if (commandline.equals("VTEST"))
			{
				npdstracker.validateServers();
				out.print("server validation test started\r\n");
				out.flush();
			}
			else if (commandline.equals("STATS"))
			{
				out.print("Pages served: " + hitcounter + "\r\n");
				out.print("REGUP commands processed: " + regcounter + "\r\n");
				out.print("NPDS servers currently registered: " + mHostInfoVector.size() + "\r\n");
				out.flush();
			}
			else if (commandline.equals("VERIFY"))
			{
				out.print("Server verifies every " + validateTime + " minutes\r\n");
				out.print("Verification is attempted " + validateTries + " times\r\n");
				out.print("Edit settings? ");
				out.flush();
				String confirm = in.readLine();
				confirm.toUpperCase();
				if (confirm.startsWith("Y") || confirm.startsWith("y"))
				{
					out.print("Enter delay between verification attempts: ");
					out.flush();
					validateTime = Integer.parseInt(in.readLine());
					out.print("Enter number of verification attempts: ");
					out.flush();
					validateTries = Integer.parseInt(in.readLine());
				}
			}
			else if (commandline.equals("SLIST"))
			{
				out.print("NPDSTrackers to get SHARE records from:\r\n");
				out.flush();
				synchronized (mSharingInfoVector)
				{
					for (int foo = 0; foo < mSharingInfoVector.size(); foo++)
					{
						TServerInfo theServerInfo = (TServerInfo) mSharingInfoVector.elementAt(foo);
						out.print(foo + ": " + theServerInfo.mHost + ":" + theServerInfo.mPort + "\r\n");
						out.flush();
					}
				}

				out.print("Add or Delete record?: ");
				out.flush();
				String confirm = in.readLine();
				if (confirm.startsWith("A") || confirm.startsWith("a"))
				{
					
					TServerInfo theServerInfo = new TServerInfo();
					
					out.print("Enter host: ");
					out.flush();
					theServerInfo.mHost = in.readLine();

					out.print("Enter port: ");
					out.flush();
					theServerInfo.mPort = in.readLine();
					
					mSharingInfoVector.addElement(theServerInfo);
				}
				else if (confirm.startsWith("D") || confirm.startsWith("d"))
				{
					out.print("Enter number to delete: ");
					out.flush();
					String deletenumber = in.readLine();
					mSharingInfoVector.removeElementAt(Integer.parseInt(deletenumber));
				}
			}
			else
			{
				out.print("Invalid command - type HELP for command reference\r\n");
				out.flush();
			}
		}
	}

	// ====================================================================	//
	// void validateServers( void ) [static, private]
	// ====================================================================	//
	// Checks the registered Newtons.
	
	public static void validateServers() throws SocketException
	{
		logMessage("Starting validation of records");
		
		mValidationInProgress += 1;
		
		Vector theHosts = new Vector( mHostInfoVector );
		
		for (int foo = 0; foo < mHostInfoVector.size(); foo++)
		{
			THostInfo theInfo = (THostInfo) theHosts.elementAt(foo);
			System.gc();
			// try to retrieve /traq/confirm.ns
			String checkResult = null;
			InputStream in = null;
			OutputStream out = null;
			// don't validate SHAREd records
			if (!(theInfo.mStatus == -1))
			{
				try
				{
					logMessage("checking " + theInfo.mName);
					Socket checkSocket = new Socket( theInfo.mHost, theInfo.mPort );
					checkSocket.setSoTimeout( kTimeout );
						
					BufferedReader inshare = new BufferedReader(new InputStreamReader(checkSocket.getInputStream()));
					PrintWriter outshare = new PrintWriter(new OutputStreamWriter(checkSocket.getOutputStream()));
						
					// Here is what I say:
					// GET /traq/confirm.ns HTTP/1.0
					// Host: <the host of the Newton server - used by some firewalls>
					// User-Agent: <kUserAgentStr>
					// Accept: text/x-npds				// this is what the client says and it should confuse any Apache webserver.
														// Remind me to add this to the protocol.
						
					outshare.print("GET /traq/confirm.ns HTTP/1.0\r\n");
					outshare.print("Host: " + theInfo.mHost + "\r\n");
					outshare.print("User-Agent: " + kUserAgentStr + "\r\n");
					outshare.print("Accept: text/x-npds\r\n");
					outshare.print("\r\n");
					outshare.flush();
					logMessage("Waiting for reply");

					char[] buffer = new char[512];
					int bytes_read = 0;
					while((bytes_read = inshare.read(buffer)) != -1);

					checkResult = new String(buffer);

					checkSocket.close();
						
					// The result should look like this:
					// HTTP_Version	202	foo
					// plenty of headers
					// CRLF CRLF
					// npds-status: SERVER_ALIVE_WELL

					// This last element isn't in the protocol. So I accept anybody without it.
					// I only check the 202.
					// (maybe one day, I'll check the content-type and the npds-status)
					// In fact, I'm pretty laxist (please don't repeat that) and I allow test pages served by some webserver).
					// I look for a 202 anywhere in the first 512 bytes.

					// if there was a zero-length result recieved, we assume the server is down
					if (checkResult.startsWith("\u0000") || (checkResult.length() == 0))
					{
						logMessage(theInfo.mName + " is down (timeout / host not found)");
						theInfo.mStatus += 1;
					}
					else if (checkResult.indexOf("202") > -1)
					{
						// the server is good: update its time and status
						logMessage(theInfo.mName + " is up");
						
						int index_j;

						synchronized ( mHostInfoVector )
						{
							for (index_j = 0; index_j < mHostInfoVector.size(); index_j++)
							{
								THostInfo theOriginalInfo = (THostInfo) mHostInfoVector.elementAt(index_j);
								if (theInfo.mName.equals(theOriginalInfo.mName))
								{
									theOriginalInfo.mLastValidation = ReturnRFCTime(new Date());
									theOriginalInfo.mStatus = 0;
									break;
								}
							}
						}
					}
					else
					{
						// the server is down / has magically changed into Apache
						logMessage(theInfo.mName + " is down (bad reply)");
						
						int index_j;
						
						synchronized ( mHostInfoVector )
						{
							for (index_j = 0; index_j < mHostInfoVector.size(); index_j++)
							{
								THostInfo theOriginalInfo = (THostInfo) mHostInfoVector.elementAt(index_j);
								if (theInfo.mName.equals(theOriginalInfo.mName))
								{
									theOriginalInfo.mStatus += 1;
									break;
								}
							}
						}
					}
				}
				catch (Exception e) 
				{
					// if there was an exception, we assume the server is down
					logMessage(theInfo.mName + " is down (timeout/connection refused/other exception) " + e);
					
					int index_j;
					
					synchronized ( mHostInfoVector )
					{
						for (index_j = 0; index_j < mHostInfoVector.size(); index_j++)
						{
							THostInfo theOriginalInfo = (THostInfo) mHostInfoVector.elementAt(index_j);
							if (theInfo.mName.equals(theOriginalInfo.mName))
							{
								theOriginalInfo.mStatus += 1;
								break;
							}
						}
					}
				}
			}
		} // for (int foo = 0; foo < theHosts.size(); foo++)
		
		// check for servers which we haven't been able to reach in a while and toast them
		synchronized (mHostInfoVector)
		{
			// for (int foo = 0; foo < mHostInfoVector.size(); foo++)
			// This should lead to a problem. I'd better start from the end.
			int theLastIndex = mHostInfoVector.size() - 1;
			for (int foo = theLastIndex; foo >= 0; foo--)
			{
				THostInfo theInfo = (THostInfo) mHostInfoVector.elementAt(foo);
				if ((theInfo.mStatus > validateTries)
					|| (theInfo.mStatus == -1))
				// Remove both down Newtons and shared Newton (as shared Newton will be got later)
				// (notice: this isn't a good idea later, we should first check that the other tracker servers
				// are still running, but as nobody does share, it isn't really a problem)
				{
					logMessage(theInfo.mName + " removed - too many failed connections");
					mHostInfoVector.removeElementAt( foo );
				}
			} // for (int foo = theLastIndex; foo >= 0; foo--)
		} // synchronized (mHostInfoVector)

		// retrieve the latest info from other trackers
		synchronized( mSharingInfoVector )
		{
			for (int foo = 0; foo < mSharingInfoVector.size(); foo++)
			{
				TServerInfo theServerInfo = (TServerInfo) mSharingInfoVector.elementAt(foo);
				logMessage("Making SHARE connection with " + theServerInfo.mHost);
				try
				{
					Socket s = new Socket(theServerInfo.mHost, Integer.parseInt(theServerInfo.mPort));
					logMessage("setting up input and output streams");
					BufferedReader inshare = new BufferedReader(new InputStreamReader(s.getInputStream()));
					PrintWriter outshare = new PrintWriter(new OutputStreamWriter(s.getOutputStream()));
					logMessage("Sending SHARE command");
					outshare.print("SHARE\r\n");
					outshare.flush();
					logMessage("Waiting for reply");

					String templine = "", returncode = "";
					templine = inshare.readLine();
					while (templine != null)
					{
						returncode += templine + "\n";
						templine = inshare.readLine();
					}

					logMessage("recieved: " + returncode);
					if (returncode.startsWith("200 OK"))
					{
						logMessage("Return code is good - parsing records");

						StringTokenizer lines = new StringTokenizer(returncode, "\n");
						while (lines.hasMoreTokens() == true)
						{
							StringTokenizer tabs = new StringTokenizer(lines.nextToken(), "\t");
							String currenttoken = tabs.nextToken();
							if (currenttoken.startsWith("no-entries"))
							{
								logMessage("remote server has no records");
								break;
							}
							else if (currenttoken.startsWith("200 OK"))
							{
								// go to the next token
							}
							else
							{
								// take the share record, break up its tokens and add it to the list
								String addresspair = new String(currenttoken);
								String timepair = new String(tabs.nextToken());
								String statuspair = new String(tabs.nextToken());
								String descpair = new String(tabs.nextToken());

								logMessage("SHARE: " + addresspair + " " + timepair + " " + statuspair + " " + descpair);

								THostInfo theNewInfo = new THostInfo();
								
								theNewInfo.mName = addresspair.substring(9);
								theNewInfo.mLastValidation = timepair.substring(15);
								theNewInfo.mStatus = -1;
								theNewInfo.mDesc = descpair.substring(13);
								
								mHostInfoVector.addElement( theNewInfo );
							}
						}
					}
					else
						logMessage("Return code is bad - not getting any records from this server");

					s.close();
				} catch (IOException e) {;}
			} // for (int foo = 0; foo < mSharingInfoVector.size(); foo++)
		} // synchronized( mSharingInfoVector )
		
		mLastValidation = ReturnRFCTime(new Date());
		mValidationInProgress -= 1;
		
		logMessage("ending validation of records");
	}
}

// ================================================	//
// "*Beware of these column-writing CEOs.*"			//
//		- Jean-Louis GassŽe column, 24 May 2000		//
// ================================================	//
