package XML_Parse;

import java.util.Scanner;
import org.jdom2.*;
import org.jdom2.output.XMLOutputter;

import java.util.List;
import java.util.UUID;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/* TO DO:
 * Fix tab spacing
 * Get Attributes to appear before other child elements
 */

public class Main {
		
	public static void main(String[] args) {
		
	    // Scanner for user input
	    Scanner user = new Scanner( System.in ); 
	    String  inputFileName, outputFileName;

	    // prepare the input file
	    System.out.print("Input .gvi File Name: ");
	    inputFileName = user.nextLine().trim();     

	    // prepare the output file
	    System.out.print("Output .xml File Name: ");
	    outputFileName = user.nextLine().trim();
		
		Document doc = Parser.parseXML(inputFileName);
		Element root = doc.getRootElement();
		Traverse.reOrderAttributes(root);
		
		try {
			XMLOutputter XMLoutput = new XMLOutputter();
			XMLoutput.output(doc, new FileWriter(outputFileName));
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
	}
}
