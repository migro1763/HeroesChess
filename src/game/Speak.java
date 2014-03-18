package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Speak {
	
	public static int ask(String question, String[] options) {
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
		while(true) {
			try {
				System.out.println(question);
				for (int i = 0; i < options.length; i++)
					System.out.print(i + ":" +options[i]+" ");
				System.out.print("\nYour choice: ");
				String returnValue =  inputReader.readLine();
				
				// check validity
				int choice = Integer.parseInt(returnValue);
				if((choice >= 0 && choice < options.length) || choice == -2) {
					return choice;
				} else {
					System.out.println("Your selection is out of range. Please try again.");
				}
			} catch (Exception e) {
				System.out.println("Your choice has been invalid, please try again:" + e);
			}
		}
	}

	public static String ask(String question) {
		System.out.println();
		//read user input
		BufferedReader inputReader = new BufferedReader(new InputStreamReader(System.in));
		try {
			System.out.print(question);
			String returnValue =  inputReader.readLine();
			
			// return null instead of empty string
			if (returnValue != null && returnValue.trim().length() == 0) {
				returnValue = null;
			}
			return returnValue;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	public static void say() {
		System.out.println();
	}
	
	public static void say(String sentence) {
		System.out.print(sentence);
	}
	
	public static void say(String sentence, boolean line) {
		say(sentence += "\n");
	}		
}
