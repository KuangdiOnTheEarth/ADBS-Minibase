package ed.inf.adbs.minibase.operator;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

public class DBCatalog {

    public static DBCatalog instance;
    private String dbDirectory;

    Map<String, List<String>> relationSchemaMap = new HashMap<>();
    // <relation name : ArrayList of data type>
    // e.g. <'R' : ['int', 'int', 'string']>

    private DBCatalog() {}

    public static DBCatalog getInstance(){
        if (instance == null)
            instance = new DBCatalog();
        return instance;
    }

    /**
     *
     * @param dbDirectory the relative path to the 'db' directory
     */
    public void init(String dbDirectory) {
        this.dbDirectory = dbDirectory;
        String schema_path = this.dbDirectory + File.separator + "schema.txt";
        try {
            File f = new File(schema_path);
            Scanner scanner = new Scanner(f);
            while (scanner.hasNextLine()) {
                ArrayList<String> line = new ArrayList<>(Arrays.asList(scanner.nextLine().split("\\s+")));
                this.relationSchemaMap.put(line.get(0), line.subList(1, line.size()));
            }
            scanner.close();
//            System.out.println(this.relationSchemaMap.get("R"));
        } catch (FileNotFoundException e) {
            System.out.println("Schema file not found at : " + schema_path);
            e.printStackTrace();
        }
    }

    /**
     * Return the relative path to the file of required relation
     * @param relationName the name of relation
     * @return the relative path as a String
     */
    public String getRelationPath(String relationName) {
        return (this.dbDirectory + File.separator + "files" + File.separator + relationName + ".csv");
    }

    /**
     * Return the schema of a relation as a List of data types ('int' or 'string')
     * @param relationName a String of the relation name
     * @return a List of strings represent the data types of attributes of tuples in that relation
     */
    public List<String> getSchema(String relationName) {
        return relationSchemaMap.get(relationName);
    }
}
