package maverick_types.FDADeviceTypes;

import java.util.List;

/**
 * Represents a property of an FDA Device that is made up of other propertise
 * Sometimes referred to an FDA Device sub object or something like that
 * @author Joel Seidel
 */
public class FDADeviceCompositePropertyObject {
    private List<FDADeviceProperty> properties;
    private String name;

    /**
     * Default constructor for the class to init properties
     * @param name name of the composite property that this represents
     * @param properties generic property list
     */
    public FDADeviceCompositePropertyObject(String name, List<FDADeviceProperty> properties){
        this.name = name;
        this.properties = properties;
    }

    /**
     * Getter for the properties list
     * @return list of properties
     */
    public List<FDADeviceProperty> getProperties(){
        return this.properties;
    }

    /**
     * Getter for the size of the properties list
     * @return size of the properties list
     */
    public int getPropertyCount(){
        return this.properties.size();
    }

    /**
     * Getter for name
     * @return name of the composite object
     */
    public String getName(){
        return this.name;
    }
}
