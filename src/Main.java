package XML_Parse;

import org.jdom2.*;
import java.util.List;

import java.io.File;
import java.io.IOException;

public class Main {
	public static void main(String[] args) {
		String filename = new String("Function.gvi");
		Document doc = Parser.parseXML(filename);
		Element root = doc.getRootElement();
		List<Element> children = root.getChildren(); // For Function.gvi, this list contains SourceModelFeatureSet and VirtualInstrument
		Element VI = children.get(0);
		for(int i = 0; i < children.size(); i++) {
			if(children.get(i).getName().equals("VirtualInstrument")) {
				VI = children.get(i);
			}
		}
		// By this point, VI contains the VirtualInstrument Element. From here, move its attributes into a child element,
		// and do the same for every element beneath VirtualInstrument
	}
}
