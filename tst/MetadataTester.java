import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

public class MetadataTester {

    private HashMap<String, Integer> map;
    private String fileName = "MetadataTester.txt";

    @Before
    public void makeMap(){
        map = new HashMap<>();

        map.put("this", 5);
        map.put("that", 6);
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
        HashMap<String, Integer> builtMap = Metadata.readMap(fileName);

        for(String s : map.keySet()){
            assert(builtMap.containsKey(s));
            assert(map.get(s) == builtMap.get(s));
        }

    }
}
