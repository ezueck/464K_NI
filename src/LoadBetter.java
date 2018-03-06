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

public class LoadBetter {
	
	//GUID-ID mapping, guid = key, id = key
	private static HashMap<String, String> idMap = new HashMap<String, String>(); 
	private static Element justForInstantiation;
	
	public static void main(String[] args) {
		String filename = new String("XML_output1.xml");
		Document doc = Parser.parseXML(filename);
		Element root = doc.getRootElement();
		justForInstantiation = root;
		
		//revert structure to labview compatible
		//load hash between GUID and IDs. Update if necesarry 
		//change GUID to IDs in xml
		BackToOriginal(root);
		createHash();
		
		
		//create xml file using the labview compatible structure 
		try {
			XMLOutputter XMLoutput = new XMLOutputter();
			XMLoutput.output(doc, new FileWriter("testing.xml"));		
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
		revertAttributes(blockDiagram); 
				
	
	}
	
	
	//recursively take a node and move the "Attributes" element into the node's attributes while also deleting that "Attributes" element
	public static void revertAttributes(Element node){
		
		List<Element> block = node.getChildren();
		List<Element> children = justForInstantiation;//list of child elements of current node
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
		}		
	}
	
	//scan table for all guid-id pairs
	public static void createHash(){
		idMap = Metadata.readMap("GUID_map.txt");
	}
	
	//change GUIDs to labview IDs
	public static void hashChange(){
		
	}
	
	//find highest id so we know when to start incrementing for new elements
	public static int HighestID(){
		return 0;
		
	}


}//end of file


