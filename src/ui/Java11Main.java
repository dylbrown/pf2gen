package ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

class Java11Main {
	public static void main(String[] args) {
		if(args.length == 0 || args[0].compareToIgnoreCase("console_log") != 0) {
			try {
				PrintStream stream = new PrintStream(new File("output.log"));
				System.setOut(stream);
				System.setErr(stream);
				System.out.println("----------------------");
				System.out.println(ZonedDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss z")));
				System.out.println("----------------------");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		Main.main(args);
	}
}
