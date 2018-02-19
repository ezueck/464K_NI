package XML_Parse;

import org.jdom2.*;
import java.util.List;
import java.util.Collection;
import java.util.UUID;
import java.util.HashMap;

public class Traverse {
	
	HashMap<String, Integer> idHash = new HashMap<String, Integer>();
	
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
	
	// postorder traversal of tree
	private static void traverseAndChange(Element root, int numTabs) {
		int numTabs1 = numTabs;
		for(Element each : root.getChildren()) {
			numTabs1 = getNumTabs(root.getContent(0));
			traverseAndChange(each, numTabs1);
		}
		int noChildren = 0;
		if(root.getAttributes().size() == 0) {
			noChildren = 1;
		}
		
		if(noChildren == 0) {
			
			// Add a tabbed newline for the new children
			root.addContent(newTabbedLine(numTabs1));
			
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
				subAttr.addContent(attr.get(0).getValue());
				subAttr.setNamespace(root.getNamespace());
				root.removeAttribute(attr.get(0)); // when uncommenting this, must change all attr.get(i)s to attr.get(0)
				if(i != (attrSize - 1)) {
					newAttr.addContent(newTabbedLine(numTabs1));
				}
			}
			newAttr.addContent(newTabbedLine(numTabs1 - 1));
			root.addContent(newTabbedLine(numTabs1 - 2));
		
		}
		
	}
	
	
	
}
