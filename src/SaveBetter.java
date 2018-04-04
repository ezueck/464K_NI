package XML_Parse;

import java.util.Scanner;
import org.jdom2.*;
import org.jdom2.output.XMLOutputter;

import java.util.List;
import java.util.UUID;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class SaveBetter {
	
	private static HashMap<String, String> idHash = new HashMap<String, String>();

		
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
		reOrderAttributes(root);
		
		try {
			XMLOutputter XMLoutput = new XMLOutputter();
			XMLoutput.output(doc, new FileWriter(outputFileName));
		}
		catch(IOException ioe){
			System.out.println(ioe);
		}
		
		user.close();
	}
		
	/**
	 * Starting from root node of XML, traverse the file, reorder the attributes, and map the IDs to GUIDs.
	 * @param root:	The root element of the document
	 */
	public static void reOrderAttributes(Element root) {
		removeChecksumTimestamp(root);
		
		try {
			idHash = Metadata.readMap("GUID_map.txt");
		}
		catch(FileNotFoundException fnfe) {
			
		}
		List<Element> children = root.getChildren();
		Element VI = children.get(0);
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i).getName().equals("VirtualInstrument")) {
				VI = children.get(i);
				break;
			}
		}
		children = VI.getChildren();
		Element blockDiagram = children.get(0); // new Element() isn't allowed, so have to assign it to something. children.get(0) as a dummy element.
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i).getName().equals("BlockDiagram")) {
				blockDiagram = children.get(i);
				break;
			}
		}
		int numTabs = getNumTabs(blockDiagram.getContent(0));
		traverseAndChange(blockDiagram, numTabs);
		changeTerminalsAndWires(blockDiagram);
		Metadata.storeMap("GUID_map.txt", idHash);
	}
	
	/**
	 * Simply changes the Checksum to 32 0s in order to avoid possible merge conflicts with the Checksum
	 * @param e: element that contains Checksum as an attribute (should be SourceFile element)
	 */
	private static void removeChecksumTimestamp(Element e) {
		Attribute checksum = e.getAttribute("Checksum");
		checksum.setValue("00000000000000000000000000000000");
		Attribute timestamp = e.getAttribute("Timestamp");
		timestamp.setValue("000000000000000");
	}
	
	/**
	 * Given a string, find the number of tabs in the string
	 * @param str: Input string
	 * @return The number of tab ("\t") characters
	 */
	private static int getNumTabs(Content str) {
		String findString = "\t";
		int lastIndex = 0;
		int numTabs = 0;
		while(lastIndex != -1) {
			lastIndex = str.getValue().indexOf(findString, lastIndex);
			if(lastIndex != -1) {
				numTabs++;
				lastIndex += findString.length();
			}
		}
		return numTabs;
	}
	
	/**
	 * Creates a new tabbed line for placing a new element. 
	 * @param numTabs: The number of tabs of the previous line
	 * @return New line string with appropriate number of tabs
	 */
	private static String newTabbedLine(int numTabs) {
		String newTabbedLine = "\n";
		for(int i = 0; i < numTabs + 1; i ++) {
			newTabbedLine = newTabbedLine + "\t";
		}
		return newTabbedLine;
	}
	
	/**
	 * Gets the number of tabs from an element's parent. An element's child should always have 1 more tab than the parent
	 * @param element: the child element, used to find the number of tabs from the parent
	 * @return The number of tabs that the parent element contains
	 */
	private static int getParentTabs(Element element) {
		Element parent = element.getParentElement();
		Element parentOfParent = parent.getParentElement();
		boolean foundParent = false;
		int index = 0;
		while(!foundParent) {
			for(int i = 0; i < parentOfParent.getContentSize(); i++) {
				if(parentOfParent.getContent(i).equals(parent)) {
					foundParent = true;
					index = i - 1;
					break;
				}
			}
			if(!foundParent) { // this should never happen; avoids infinite loop
				return 0;
			}
		}
		int numTabs = getNumTabs(parentOfParent.getContent(index));		
		return numTabs;
	}
	
	/**
	 * Helper function for reOrderAttributes. Actually does the traversal (DFS) and reorders/changes IDs
	 * @param root: Root element of the document
	 * @param numTabs: Number of tabs of the previous line, to keep track
	 */
	private static void traverseAndChange(Element root, int numTabs) {
		int numTabs1 = numTabs;
		for(Element each : root.getChildren()) {
			numTabs1 = getNumTabs(root.getContent(0));
			traverseAndChange(each, numTabs1);
		}
		numTabs1 = numTabs;
		boolean noAttributes = false;
		if(root.getAttributes().size() == 0) {
			noAttributes = true;
		}
		
		if(!noAttributes) {
			
			// Add a tabbed newline for the new children
			int numTabsTest = getParentTabs(root);
			root.addContent(newTabbedLine(numTabsTest + 1));
			
			List<Attribute> attr = root.getAttributes();
			Element newAttr = new Element("Attributes");
			root.addContent(newAttr);
			numTabs1 = getNumTabs(root.getContent(0));
			newAttr.addContent(newTabbedLine(numTabs1));
			newAttr.setNamespace(root.getNamespace());
			int attrSize = attr.size();
			for(int i = 0; i < attrSize; i++) {
				Element subAttr = new Element(attr.get(0).getName());
				newAttr.addContent(subAttr);
				checkID(attr);
				subAttr.addContent(attr.get(0).getValue());
				subAttr.setNamespace(root.getNamespace());
				root.removeAttribute(attr.get(0)); // when uncommenting this, must change all attr.get(i)s to attr.get(0)
				if(i != (attrSize - 1)) {
					newAttr.addContent(newTabbedLine(numTabs1));
				}
			}
			newAttr.addContent(newTabbedLine(numTabs1 - 1));
			root.addContent(newTabbedLine(numTabs1 - 2));
			
			// move parent element's attributes to be right below the parent, instead of after all children.
			newAttr = newAttr.getParentElement();
			List<Content> moveToTop = newAttr.cloneContent();
			while(moveToTop.size() > 3) {
				moveToTop.remove(0);
			}
			for(int i = 0; i < 3; i++) {
				newAttr.removeContent(newAttr.getContentSize() - 1);
			}
			if(newAttr.getContentSize() > 0) {
				moveToTop.remove(moveToTop.size() - 1);
			}
			newAttr.addContent(0, moveToTop);		
		}		
	}
	
	/**
	 * Checks the HashMap for a GUID<-->ID mapping. If there is no mapping, generate a new GUID and create the mapping.
	 * Also changes the ID in the XML to its corresponding GUID.
	 * @param attr: List of attributes that may or may not contain an ID that needs to be changed
	 */
	private static void checkID(List<Attribute> attr) {
		String attrName = attr.get(0).getName();
		String attrValue = attr.get(0).getValue();
		if(attrName.equals("Id") || attrName.equals("AttachedTo") || attrName.equals("DiagramId")
								 || attrName.equals("RightRegister") || attrName.equals("Label")) {
			if(attrValue.contains("max") || attrValue.contains("min") || attrValue.contains("Value")) {
				return;
			}
			String GUID = checkHashMap(attrValue);
			if(GUID == null) { // returns GUID if value is already in HashMap
				GUID = UUID.randomUUID().toString();
				idHash.put(GUID, attrValue);
			}
			attr.get(0).setValue(GUID);
		}
		if(attrName.equals("Joints")) {
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
					String GUID = checkHashMap(id);
					int indexDelete = index + 2;
					StringBuilder build = new StringBuilder(attrValue);
					while(build.charAt(indexDelete) != ':') {
						build.deleteCharAt(indexDelete);
					}
					if(GUID == null) {
						GUID = UUID.randomUUID().toString();
						idHash.put(GUID, id);
					}
						build.insert(indexDelete, GUID);
						attrValue = build.toString();
						attr.get(0).setValue(attrValue);
				}		
				fromIndex = index + 1;
			}
		}
		if(attrName.equals("Terminals")) {
			StringBuilder newTerminalsBuild = new StringBuilder(); // used to build new Terminals attribute with GUIDs
			String[] tokens = attrValue.split(", ");
			
			for(int i = 0; i < tokens.length; i++) {
				String[] lookForEquals = tokens[i].split("="); // need to check both sides of "="
				for(int j = 0; j < lookForEquals.length; j++) {
					String stringID = lookForEquals[j];
					boolean validID = false;
					try {
						Integer.parseInt(stringID);
						validID = true;
					}
					catch(NumberFormatException e) {						
					}
					if(validID) {
						String GUID = checkHashMap(stringID);
						if(GUID == null) {
							GUID = UUID.randomUUID().toString();
							idHash.put(GUID, stringID);
						}
						lookForEquals[j] = GUID;  // change ID to GUID, and use the StringBuilder to implement it back into XML.
					}
				}
				if(lookForEquals.length == 2) {
					newTerminalsBuild.append(lookForEquals[0] + "=" + lookForEquals[1]);
				} else if(lookForEquals.length == 1) {
					newTerminalsBuild.append(lookForEquals[0]);
				}
				if(i < tokens.length - 1) {
					newTerminalsBuild.append(", ");
				}
			}
			String newTerminals = newTerminalsBuild.toString();
			attr.get(0).setValue(newTerminals); // replace old attribute, now including the GUIDs
		}
	}
	
	/**
	 * Called after the rest of the xml has been restructured.
	 * Changes Terminals to be Git-compatible; separating information onto separate lines to prevent merge conflicts.
	 * @param node: The Terminals to restructure
	 */
	private static void changeTerminalsAndWires(Element node) {
		for(Element each : node.getChildren()) {
			changeTerminalsAndWires(each);
		}
		
		if(node.getName().equals("Terminals")) {
			int numTerminalTabs = getParentTabs(node) + 2;
			String newLine = newTabbedLine(numTerminalTabs - 1); // -1 because newTabbedLine adds an extra tab? don't want to mess with it
			
			String terminalValue = node.getValue();
			String[] tokens = terminalValue.split(", ");
			node.removeContent(0);
			for(int i = 0; i < tokens.length; i++) {
				node.addContent(newLine);
				Element Terminal = new Element("Terminal" + i);
				Terminal.addContent(tokens[i]);
				Terminal.setNamespace(node.getNamespace());
				node.addContent(Terminal);
			}
			newLine = newTabbedLine(numTerminalTabs - 2);
			node.addContent(newLine);
		} else if(node.getName().equals("Joints")) {
			int numJointsTabs = getParentTabs(node) + 2;
			String newLine = newTabbedLine(numJointsTabs - 1);
			
			String jointsValue = node.getValue();
			String[] tokens = jointsValue.split("\\)");
			node.removeContent(0);
			for(int i = 0; i < tokens.length; i++) {
				node.addContent(newLine);
				Element Joint = new Element("Joint" + i);
				Joint.addContent(tokens[i]);
				Joint.setNamespace(node.getNamespace());
				node.addContent(Joint);
			}
			newLine = newTabbedLine(numJointsTabs - 2);
			node.addContent(newLine);
		}
	}
	
	/**
	 * Helper function for checkID. Checks the HashMap for a valid GUID<-->ID mapping
	 * @param value: The ID to be checked
	 * @return Returns the respective GUID if the mapping exists, and null if there is no mapping.
	 */
	private static String checkHashMap(String value) {
		for(HashMap.Entry<String, String> entry : idHash.entrySet()) {
			String ID = entry.getValue();
			if(ID.equals(value)) {
				return entry.getKey();
			}
		}
		return null;
	}
}
