package maverick_types.FDADeviceTypes;

public class FDADeviceGmdnTerm {
    String name, definition;
    public FDADeviceGmdnTerm(String name, String definition) {
        this.name = name;
        this.definition = definition;
    }

    /**
     * Getter for the name property
     * @return name property
     */
    public String getName(){
        return name;
    }

    /**
     * Getter for the definition property
     * @return definition property
     */
    public String getDefinition(){
        return definition;
    }
}
