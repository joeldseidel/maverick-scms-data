package maverick_types.FDADeviceTypes;

public class FDADeviceProductCode {
    private String code, name;
    public FDADeviceProductCode(String code, String name){
        this.code = code;
        this.name = name;
    }

    /**
     * Getter for code property
     * @return code
     */
    public String getCode(){
        return code;
    }

    /**
     * Getter for name property
     * @return name
     */
    public String getName(){
        return name;
    }
}
