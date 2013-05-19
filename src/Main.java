import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
	static String				VERSION		= "0.01";
	
	static boolean				DEBUG		= false;
	static String				SERVER		= "irc.freenode.net";
	static int					PORT		= 6667;
	static String				NICK		= "infobott";
	static String				OWNER		= "linkxs";
	static String				USER		= NICK
													+ " 0 * :I'm a nice informative bot! ("
													+ OWNER + ")";
	static ArrayList<String>	CHANNELS	= new ArrayList<String>();
	
	static String				FILE		= "infofile.txt";
	
	static Socket				cs			= null;
	static private Scanner		sin			= null;
	static private PrintWriter	sout		= null;
	
	public static void main(String args[]) {
		if (DEBUG) {
			CHANNELS.add("#linkxs");
			CHANNELS.add("#botters-test");
			CHANNELS.add("##wrccdc");
			startMe();
		}
		if (args.length < 1 && !DEBUG)
			System.out.println("Wrong usage.\n\tjava -jar *.jar [channel]");
		else if (args.length >= 1) {
			try {
				for (int i = 0; i < args.length; i++) {
					CHANNELS.add("#" + args[i]);
				}
				while (9000 < 90001)
					startMe();
			} catch (Exception e) {
				System.out.println("Wrong usage.\n\tjava -jar *.jar [channel]");
			}
		}
	}
	
	static boolean sendToIRC(String s) {
		try {
			// System.err.println(s);
			sout.print(s);
			sout.flush();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	static boolean startMe() {
		try {
			cs = new Socket(SERVER, PORT);
			sout = new PrintWriter(cs.getOutputStream());
			sin = new Scanner(new BufferedInputStream(cs.getInputStream()));
			
			sendToIRC("NICK " + NICK + "\r\n");
			sendToIRC("USER " + USER + "\r\n");
			sendToIRC("PRIVMSG nickserv :identify whataFUCKINGfatOassOWodForthisBot\r\n");
			for (String s : CHANNELS) {
				sendToIRC("JOIN " + s + "\r\n");
			}
			sout.flush();
			while (true) {
				String tempLine = sin.nextLine();
				String sourceChan = "linkxs";
				String sourceNick = tempLine.substring(tempLine.indexOf(":"),
						tempLine.indexOf("!"));
				try {
					sourceChan = tempLine.substring(
							tempLine.indexOf("PRIVMSG ") + 8,
							tempLine.indexOf(' ', tempLine.indexOf(" :")));
				} catch (Exception e) {
				}
				System.out.println(tempLine);
				if (tempLine.contains("PING")) {
					tempLine.replace("PING", "PONG");
					sendToIRC(tempLine + "\r\n");
				} else if (tempLine.contains("infobott :")) {
					if (tempLine.contains("infobott :VERSION")
							|| tempLine.contains("infobott :version")) {
						sendVersion(tempLine
								.substring(1, tempLine.indexOf('!')));
					}
				} else if (tempLine.contains(" :&")) {
					if (tempLine
							.startsWith(":linkxs!linkxs@cpe-75-80-186-73.san.res.rr.com")) {
						System.err.println(" >>> Obey the master! Outcome: "
								+ privelegedExecute(tempLine, tempLine
										.substring(tempLine.indexOf('&')),
										sourceChan, sourceNick));
					} else {
						System.err.println(" >>> Not the master! Outcome: "
								+ unPrivedExec(tempLine, tempLine
										.substring(tempLine.indexOf('&')),
										sourceChan, sourceNick));
						sendToIRC("PRIVMSG " + sourceChan
								+ " :You're not my real mom!\r\n");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	static boolean unPrivedExec(String tempLine, String command,
			String sourceChan, String sourceNick) {
		if (command.contains("version") || command.contains("VERSION"))
			return sendVersion(sourceChan);
		else if (command.contains("info show all"))
			return listAllInfo(sourceNick);
		else if (command.contains("info show "))
			return listInfo(tempLine.substring(
					tempLine.indexOf("info show ") + 10,
					tempLine.indexOf(' ', tempLine.indexOf("info show ") + 10)));
		else
			return sendUsage(sourceChan);
	}
	
	static boolean listInfo(String desciptor) {
		// TODO Auto-generated method stub
		return false;
	}
	
	static boolean sendUsage(String sourceChan) {
		try {
			System.err.println(" >>> Printing usage to " + sourceChan);
			sendToIRC("PRIVMSG "
					+ sourceChan
					+ " :I can do so many things! &quit, &restart, &version, &halp, &info show all, &info show [descriptor], &info add [descriptor] - [link].\r\n");
			return true;
		} catch (Exception e) {
			System.err.println("Problems in sendUsage");
		}
		return false;
	}
	
	static boolean privelegedExecute(String tempLine, String command,
			String sourceChan, String sourceNick) {
		System.err.println(" >>> Command is " + command);
		if (command.contains("quit")) {
			try {
				System.err.println(" >>> Quitting");
				sendToIRC("PRIVMSG " + sourceChan + " :bye!\r\n");
			} catch (Exception e) {
				System.err.println("Bad substring in &quit");
			}
			System.exit(0);
			return true;
		} else if (command.contains("restart")) {
			System.err.println(" >>> Restarting..");
			try {
				cs.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			startMe();
			return true;
		}
		unPrivedExec(tempLine, command, sourceChan, sourceNick);
		return false;
	}
	
	static boolean listAllInfo(String sourceNick) {
		try {
			System.err.println(" >>> Listing all known info");
			sendToIRC("PRIVMSG " + sourceNick
					+ " :Here's a list of everything I have: \r\n");
			for (String s : openFile()) {
				sendToIRC("PRIVMSG " + sourceNick + " :" + s);
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	static ArrayList<String> openFile() {
		ArrayList<String> al = new ArrayList<String>();
		try {
			Scanner sc = new Scanner(new FileInputStream(FILE));
			while (sc.hasNextLine()) {
				String tempLine = sc.nextLine();
				al.add(tempLine.substring(0, tempLine.indexOf(";;;")));
			}
		} catch (FileNotFoundException e) {
			try {
				(new File(FILE)).createNewFile();
			} catch (Exception e2) {
				e2.printStackTrace();
			}
			return openFile();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return al;
	}
	
	static boolean sendVersion(String sourceChan) {
		try {
			System.err.println(" >>> Reporting version");
			sendToIRC("PRIVMSG " + sourceChan + " :I am versionless v"
					+ VERSION + ", with debug on: " + DEBUG + "\r\n");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	static boolean replyFaggot(String tempLine) {
		System.err.print(" >>> Faggot message ");
		try {
			String sourceChan = tempLine.substring(
					tempLine.indexOf("PRIVMSG ") + 8,
					tempLine.indexOf(' ', tempLine.indexOf("PRIVMSG ") + 10));
			String tnick = "";
			if (tempLine.indexOf('?', tempLine.indexOf("who is ")) > 0)
				tnick = tempLine.substring(tempLine.indexOf("who is ") + 7,
						tempLine.indexOf('?', tempLine.indexOf("who is ")));
			else if (tempLine.indexOf('!', tempLine.indexOf("who is ")) > 0)
				tnick = tempLine.substring(tempLine.indexOf("who is ") + 7,
						tempLine.indexOf('!', tempLine.indexOf("who is ")));
			else if (tempLine.indexOf(' ', tempLine.indexOf("who is ") + 8) > 0)
				tnick = tempLine.substring(tempLine.indexOf("who is ") + 7,
						tempLine.indexOf(' ', tempLine.indexOf("who is ") + 8));
			else
				tnick = tempLine.substring(tempLine.indexOf("who is ") + 7);
			
			System.err.print("to tnick: " + tnick + "\n");
			if (tnick.contains(NICK))
				sendToIRC("PRIVMSG " + sourceChan + " :" + "I am a faggot.\r\n");
			else if (tnick.equals(OWNER))
				sendToIRC("PRIVMSG " + sourceChan + " :"
						+ "According to my tests, " + OWNER
						+ " isn't a faggot.\r\n");
			else
				sendToIRC("PRIVMSG " + sourceChan + " :" + tnick
						+ " is a faggot.\r\n");
			return true;
		} catch (Exception e) {
			System.err.println("Bad substring in replyFaggot");
		}
		return false;
	}
}
