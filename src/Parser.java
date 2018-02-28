package XML_Parse;

import org.jdom2.Document;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;

public class Parser {

    public static Document parseXML(String file) {

        SAXBuilder builder = new SAXBuilder();
        File xmlFile = new File(file);

        try{
            Document document = builder.build(xmlFile);
            return document;

        } catch (JDOMException e) {

            e.printStackTrace();

        } catch (IOException e) {

            e.printStackTrace();
        }

        return null;
    }
}
