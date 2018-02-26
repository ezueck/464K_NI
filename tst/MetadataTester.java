import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

public class MetadataTester {

    private HashMap<String, String> map;
    private String fileName = "MetadataTester.txt";

    @Before
    public void makeMap(){
        map = new HashMap<>();

        map.put("GUID1", "5");
        map.put("GUID2", "6");
        map.put("GUID3", "6");

    }

    @After
    public void cleanUp(){
        File file = new File(fileName);
        file.delete();
    }

    @Test
    public void testStoreData(){
        Metadata.storeMap(fileName, map);
    }

    @Test
    public void testBuildMap(){

        Metadata.storeMap(fileName, map);
        HashMap<String, String> builtMap = Metadata.readMap(fileName);

        for(String s : map.keySet()){
            assert(builtMap.containsKey(s));
            assert(map.get(s).equals(builtMap.get(s)));
        }

    }
}
