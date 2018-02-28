package XML_Parse;

import org.jdom2.*;
import java.util.List;
import java.util.Collection;
import java.util.UUID;
import java.util.HashMap;

public class Traverse {
	
	private static HashMap<String, String> idHash = new HashMap<String, String>();
		
	public static void reOrderAttributes(Element root) {
		List<Element> children = root.getChildren();
		Element VI = children.get(0);
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i).getName().equals("VirtualInstrument")) {
				VI = children.get(i);
				break;
			}
		}
		children = VI.getChildren();
		Element blockDiagram = children.get(0);
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
	
	private static String newTabbedLine(int numTabs) {
		String newTabbedLine = "\n";
		for(int i = 0; i < numTabs + 1; i ++) {
			newTabbedLine = newTabbedLine + "\t";
		}
		return newTabbedLine;
	}
	
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
	
	private static void checkID(List<Attribute> attr) {
		String attrName = attr.get(0).getName();
		String attrValue = attr.get(0).getValue();
		if(attrName.equals("Id")) {
			if(attrName.contains("max") || attrName.contains("min")) {
				return;
			}
			String GUID = checkHashMap(attrName);
			if(GUID != null) { // returns GUID if value is already in HashMap
				attr.get(0).setValue(GUID);
			} else {
				GUID = UUID.randomUUID().toString();
				idHash.put(GUID, attrValue);
				attr.get(0).setValue(GUID);
			}
		}
	}
	
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
