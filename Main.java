package XML_Parse;

import org.jdom2.*;
import org.jdom2.output.XMLOutputter;

import java.util.List;
import java.util.UUID;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

/* TO DO:
 * Fix tab spacing
 * Get Attributes to appear before other child elements
 */

public class Main {
	
	public static HashMap<String, Integer> idHash = new HashMap<String, Integer>();
	
	public static void main(String[] args) {
		String filename = new String("Function.gvi");
		//idHash = new HashMap<String, Integer>();
		Document doc = Parser.parseXML(filename);
		Element root = doc.getRootElement();
		Traverse.reOrderAttributes(root);
		
		try {
			XMLOutputter XMLoutput = new XMLOutputter();
			XMLoutput.output(doc, new FileWriter("XML_output1.xml"));		
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
		
		/*
		for(int i = 0; i < 50; i++) {
			String randID = UUID.randomUUID().toString();
			System.out.println(randID);
		}
		*/
	}
}
