package maverick_types.FDADeviceTypes;

public class FDADeviceSize {
    String text, type, value, unit;

    public FDADeviceSize(String text, String type, String value, String unit){
        this.text = text;
        this.type = type;
        this.value = value;
        this.unit = unit;
    }

    /**
     * Getter for text property
     * @return text property
     */
    public String getText(){
        return text;
    }

    /**
     * Getter for type property
     * @return type property
     */
    public String getType(){
        return type;
    }

    /**
     * Getter for value property
     * @return value property
     */
    public String getValue(){
        return value;
    }

    /**
     * Getter for unit property
     * @return unit property
     */
    public String getUnit(){
        return unit;
    }
}
