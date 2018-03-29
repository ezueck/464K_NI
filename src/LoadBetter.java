package XML_Parse;

import org.jdom2.*;
import org.jdom2.output.XMLOutputter;

import java.util.List;
import java.util.Scanner;
import java.util.UUID;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

public class LoadBetter {
	
	//GUID-ID mapping, guid = key, id = key
	private static HashMap<String, String> idMap = new HashMap<String, String>(); 
	private static Element justForInstantiation;
	private static int highestID;
	
	public static void main(String[] args) {
		
	    // Scanner for user input
	    Scanner user = new Scanner( System.in ); 
	    String  inputFileName, outputFileName;

	    // prepare the input file
	    System.out.print("Input .xml File Name: ");
	    inputFileName = user.nextLine().trim();     

	    // prepare the output file
	    System.out.print("Output .gvi File Name: ");
	    outputFileName = user.nextLine().trim();
	    
	    Document doc = Parser.parseXML(inputFileName);
		Element root = doc.getRootElement();
		justForInstantiation = root;

		//load hash between GUID and IDs.
		//revert structure to labview compatible
		//change GUID to IDs in xml
		createHash();
		BackToOriginal(root);
		
		
		//create xml file using the labview compatible structure 
		try {
			XMLOutputter XMLoutput = new XMLOutputter();
			XMLoutput.output(doc, new FileWriter(outputFileName));		
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
	}
	
	//change xml back to original structure for labview NXG
	public static void BackToOriginal(Element root){
		
		List<Element> children = root.getChildren();
		Element VI = justForInstantiation;
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i).getName().equals("VirtualInstrument")) {
				VI = children.get(i);
				break;
			}
		}

		children = VI.getChildren();
		Element blockDiagram = justForInstantiation;
		
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i).getName().equals("BlockDiagram")) {
				blockDiagram = children.get(i);
				break;
			}
		}
		////we now have the block diagram element (blockDiagram), so we can begin reverting to original form
		revertTerminals(blockDiagram);
		revertAttributes(blockDiagram);
		getHighestID(root);
		hashChange(blockDiagram);
	
	}
	
	// Revert terminals so that all info is back onto 1 line, instead of split up into multiple lines.
	public static void revertTerminals(Element node) {
		for(Element each : node.getChildren()) {
			revertTerminals(each);
		}
		
		if(node.getName().equals("Terminals")) {
			StringBuilder revertTerminalsBuilder = new StringBuilder();
			int numTerminals = node.getContentSize() / 2;
			int commaCount = 0;
			while(!node.getContent().isEmpty()) {
				if(node.getContent(0).getCType() == Content.CType.Element) {
					revertTerminalsBuilder.append(node.getContent(0).getValue());
					commaCount++;
					if(commaCount < numTerminals) {
						revertTerminalsBuilder.append(", ");
					}
				}
				node.removeContent(0);
			}
			String revertTerminals = revertTerminalsBuilder.toString();
			node.addContent(revertTerminals);
		}
	}
	
	//recursively take a node and move the "Attributes" element into the node's attributes while also deleting that "Attributes" element
	public static void revertAttributes(Element node){
		
		List<Element> block = node.getChildren();
		List<Element> children = node.getChildren();//list of child elements of current node
		Element attr = justForInstantiation; //attrbute element once found
		Element temp = justForInstantiation;//temp variable to help with random stuff
		Element parent = justForInstantiation; //parent of attribute element
		boolean attrFound = false;
		int attrnum = 0;
		
		//recursive call - check current node for elements, and recursively call those elements if they arent attributes or null. guarantees element coverage
		for(int i=0; i<block.size();i++){
			temp = block.get(i);
			if(temp.getContent()!=null && temp.getName()!="Attributes")
				revertAttributes(temp);		
			}
		
		//check elements of current node. if attributes is found then set flag
		for (int i = 0; i<block.size();i++){
			if(block.get(i).getName().equals("Attributes") ){
				attr = block.get(i); //get attribute element
				attrFound = true;
				attrnum = i;
				break;
			}
		}
		
		//attributes element found. this method moves the data from the element into the attributes section of the node while deleting the attributes element
		if(attrFound){
			children = attr.getChildren();
			parent = attr.getParentElement();
			for(int i = 0; i<children.size(); i++){
				temp = children.get(i);
				parent.setAttribute(temp.getName(), temp.getText());
			}
			parent.removeContent(attr);
			parent.removeContent(attrnum);
			
			if(parent.getContent().size() == 1) {  // If we're left with a content size of 1, then it should be an empty element.
				parent.removeContent(attrnum);	   // There's a text element leftover from the "save better" function.
			}
		}		
	}
	
	//scan table for all guid-id pairs
	public static void createHash(){
		try {
			idMap = Metadata.readMap("GUID_map.txt");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	//change GUIDs to labview IDs
	public static void hashChange(Element node){
		
		List<Element> children = node.getChildren();
		for(Element each : node.getChildren()) {
			hashChange(each);
		}
		
		// get list of attributes. Start reverting IDs back to LabVIEW-friendly IDs.
		List<Attribute> attributes = node.getAttributes();
		
		for(Attribute each : attributes) {
			String attrName = each.getName();
			String attrValue = each.getValue();
			if(attrName.equals("Id") || attrName.equals("AttachedTo") || attrName.equals("DiagramId") || attrName.equals("RightRegister")
									 || attrName.equals("Label")) {
				int labviewID = getIdValue(each);
				if(labviewID > 0) {
					String idString = Integer.toString(labviewID);
					each.setValue(idString);
				} else if(labviewID == -1) {
					String idString = idMap.get(attrValue);
					each.setValue(idString);
				}
			} else if(attrName.equals("Joints")) {
				int fromIndex = 0;
				for(int i = 0; i < attrValue.length(); i++) {
					int index = attrValue.indexOf("N(", fromIndex);
					if(index == -1) {
						break;
					} else {
						String id = new String();
						for(int j = index + 2; j < attrValue.length(); j++){
							if(attrValue.charAt(j) == ':') {
								break;
							}
							id = id + attrValue.charAt(j);
						}
						String idString = idMap.get(id);
						int indexDelete = index + 2;
						StringBuilder build = new StringBuilder(attrValue);
						while(build.charAt(indexDelete) != ':') {
							build.deleteCharAt(indexDelete);
						}
						if(idString != null) {
							build.insert(indexDelete, idString);
							attrValue = build.toString();
							each.setValue(attrValue);
						} else {
							System.out.println("error in finding valid ID for wire joint");
						}
					}		
					fromIndex = index + 1;
				}
			} else if(attrName.equals("Terminals")) {
				StringBuilder revertTerminalsBuild = new StringBuilder(); // used to build new Terminals attribute with GUIDs
				String[] tokens = attrValue.split(", ");
				
				for(int i = 0; i < tokens.length; i++) {
					String[] lookForEquals = tokens[i].split("="); // need to check both sides of "="
					for(int j = 0; j < lookForEquals.length; j++) {
						String GUID = lookForEquals[j];
						String stringID = idMap.get(GUID);
						if(stringID != null) {
							lookForEquals[j] = stringID;  // change GUID to ID, and use the StringBuilder to implement it back into XML.
						}
					}
					if(lookForEquals.length == 2) {
						revertTerminalsBuild.append(lookForEquals[0] + "=" + lookForEquals[1]);
					} else if(lookForEquals.length == 1) {
						revertTerminalsBuild.append(lookForEquals[0]);
					}
					if(i < tokens.length - 1) {
						revertTerminalsBuild.append(", ");
					}
				}
				String revertTerminals = revertTerminalsBuild.toString();
				each.setValue(revertTerminals); // replace old attribute, now including the GUIDs
			}
		}
	}
	
	//find highest id so we know when to start incrementing for new elements
	public static void getHighestID(Element node){
		
		List<Element> children = node.getChildren(); // list of children of current node
		for(Element each : node.getChildren()) {
			getHighestID(each);
		}
		
		// get list of attributes, check for an ID
		List<Attribute> attributes = node.getAttributes();
		
		for(Attribute each : attributes) {
			if(each.getName().equals("Id")) {
				int idValue = getIdValue(each);  // get value of the ID
				if(idValue != -1) {	// if idValue is -1, then it's a non-numeric ID.
					if(idValue > highestID) {
						highestID = idValue;
					}
				}
			}
		}
	}
	
	/**
	 * Given an ID attribute, return the value of the ID as an int.
	 * If the ID is determined to be a GUID, and it's not in the HashMap already, then add it to the HashMap.
	 * @param attribute the ID to be parsed/looked up
	 * @return the value of the ID, or -1 if the ID is non-numeric.
	 */
	public static int getIdValue(Attribute attribute) {
		try {
			int idValue = Integer.parseInt(attribute.getValue()); // try parsing the ID value as an integer
			return idValue;
		}
		catch(NumberFormatException NFE) {  // if exception is thrown, then it is likely a GUID. Check map.
			String ID = idMap.get(attribute.getValue());
			if(ID != null) { // the ID exists in the map already
				try {
					int idValue = Integer.parseInt(ID);
					return idValue;
				}
				catch(NumberFormatException NFE2) { // if a second exception is thrown, then it's a non-numeric ID that already exists in the map.
					return -1;
				}
			} else {
				if(attribute.getValue().length() == 36) { // 36 characters is the length of our GUIDs.
					highestID = highestID + 1;			  // If we find an ID that is 36 characters long, and not in the HashMap,
					idMap.put(attribute.getValue(), Integer.toString(highestID));  // then assume it's a new GUID from a newly added element. Add to HashMap.
					return highestID;
				}
				return -2; // non-numeric ID, and we don't want it in the map.
			}
		}
	}
	


}//end of file


