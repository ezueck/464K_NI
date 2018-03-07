package XML_Parse;

import org.jdom2.*;
import java.util.List;
import java.util.Collection;
import java.util.UUID;
import java.util.HashMap;

public class Traverse {
	
	private static HashMap<String, String> idHash = new HashMap<String, String>();
	
	/**
	 * Starting from root node of XML, traverse the file, reorder the attributes, and map the IDs to GUIDs.
	 * @param root:	The root element of the document
	 */
	public static void reOrderAttributes(Element root) {
		removeChecksumTimestamp(root);
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
			int index = 0;
			
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
								 || attrName.equals("RightRegister")) {
			if(attrValue.contains("max") || attrValue.contains("min") || attrValue.contains("Value")) {
				return;
			}
			String GUID = checkHashMap(attrValue);
			if(GUID != null) { // returns GUID if value is already in HashMap
				attr.get(0).setValue(GUID);
			} else {
				GUID = UUID.randomUUID().toString();
				idHash.put(GUID, attrValue);
				attr.get(0).setValue(GUID);
			}
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
					if(GUID != null) {
						build.insert(indexDelete, GUID);
						attrValue = build.toString();
						attr.get(0).setValue(attrValue);
					} else {
						GUID = UUID.randomUUID().toString();
						idHash.put(GUID, id);
						build.insert(indexDelete, GUID);
						attrValue = build.toString();
						attr.get(0).setValue(attrValue);
					}
				}		
				fromIndex = index + 1;
			}
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
