import java.io.BufferedInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
	static String				VERSION		= "1.41";
	
	static boolean				DEBUG		= false;
	static String				SERVER		= "irc.freenode.net";
	static int					PORT		= 6667;
	static String				NICK		= "buttbott";
	static String				OWNER		= "linkxs";
	static String				USER		= NICK
													+ " 0 * :Ima bot you in the mouth! ("
													+ OWNER + ")";
	static ArrayList<String>	CHANNELS	= new ArrayList<String>();
	
	static Socket				cs			= null;
	static private Scanner		sin			= null;
	static private PrintWriter	sout		= null;
	
	public static void main(String args[]) {
		if (DEBUG) {
			CHANNELS.add("#testgradius");
			CHANNELS.add("#botters-test");
			CHANNELS.add("##wrccdc");
			startMe();
		}
		if (args.length < 1 && !DEBUG)
			System.out
					.println("Wrong usage.\n\tjava -jar imabot.jar [channel]");
		else if (args.length >= 1) {
			try {
				for (int i = 0; i < args.length; i++) {
					CHANNELS.add("#" + args[i]);
				}
				while (9000 < 90001)
					startMe();
			} catch (Exception e) {
				System.out
						.println("Wrong usage.\n\tjava -jar imabot.jar [channel]");
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
				} else if (tempLine.contains("buttbott :")) {
					if (tempLine.contains("buttbott :VERSION")
							|| tempLine.contains("buttbott :version")) {
						sendVersion(tempLine
								.substring(1, tempLine.indexOf('!')));
					}
					
				} else if (tempLine.contains(" :&")) {
					if (tempLine
							.startsWith(":linkxs!linkxs@cpe-75-80-186-73.san.res.rr.com")) {
						System.err.println(" >>> Obey the master! Outcome: "
								+ privelegedExecute(tempLine.substring(tempLine
										.indexOf('&')), sourceChan));
					} else {
						System.err.println(" >>> Obey the master! Outcome: "
								+ unPrivedExec(tempLine.substring(tempLine
										.indexOf('&')), sourceChan));
						sendToIRC("PRIVMSG " + sourceChan
								+ " :You're not my real mom!\r\n");
					}
				} else if (tempLine.contains(":who is ")) {
					replyFaggot(tempLine);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	static boolean unPrivedExec(String command, String sourceChan) {
		if (command.contains("version") || command.contains("VERSION"))
			return sendVersion(sourceChan);
		else if (command.contains("usage") || command.contains("USAGE")
				|| command.contains("help") || command.contains("HELP")
				|| command.contains("halp") || command.contains("HALP"))
			return sendUsage(sourceChan);
		return false;
	}
	
	static boolean sendUsage(String sourceChan) {
		try {
			System.err.println(" >>> Printing usage to " + sourceChan);
			sendToIRC("PRIVMSG "
					+ sourceChan
					+ " :I can do so many things! &quit, &restart, &version, &halp, who is [nick].\r\n");
			return true;
		} catch (Exception e) {
			System.err.println("Problems in sendUsage");
		}
		return false;
	}
	
	static boolean privelegedExecute(String command, String sourceChan) {
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
		unPrivedExec(command, sourceChan);
		return false;
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
