import java.io.*;
import java.util.HashMap;
import java.util.Scanner;

public class Metadata {

    /**
     * Stores a metadata map into a file
     * @param fileName: string of the filename to be created
     * @param map: the map to be stored
     */
    public static void storeMap(String fileName, HashMap<String, Integer> map) {

        PrintWriter pw = null;

        try {

            File file = new File(fileName);
            FileWriter fw = new FileWriter(file, true);
            pw = new PrintWriter(fw);

            for(String e : map.keySet()){
                pw.println(e + " " + map.get(e));
            }

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            if (pw != null) {
                pw.close();
            }
        }
    }

    /**
     * Build a HashMap stored in the filename given by file
     * @param fileName: string of the filename with stored map
     */
    public static HashMap<String, Integer> readMap(String fileName) {

        HashMap<String, Integer> map = new HashMap<>();

        File file = new File(fileName);
        Scanner input;

        try{
            input = new Scanner(file);

            while (input.hasNextLine()) {
                String line = input.nextLine();
                String[] tokens = line.split("\\s");

                String guid = tokens[0];
                int id  = Integer.parseInt(tokens[1]);

                map.put(guid, id);
            }
        } catch(FileNotFoundException e){
            e.printStackTrace();

        }

        return map;
    }

}
