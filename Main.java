package XML_Parse;

import org.jdom2.*;
import org.jdom2.output.XMLOutputter;

import java.util.List;
import java.util.UUID;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/* TO DO:
 * Fix tab spacing
 * Delete Attributes correctly after adding them as new elements
 * xmlns placing might be an issue? When ouputting xml, namespace goes first instead of last
 * Get Attributes to appear before other child elements
 */

public class Main {
	public static void main(String[] args) {
		String filename = new String("Function.gvi");
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
