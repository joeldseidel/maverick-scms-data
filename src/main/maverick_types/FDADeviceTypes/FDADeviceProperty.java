package maverick_types.FDADeviceTypes;

public class FDADeviceProperty {
    private String propertyName;
    private Object propertyValue;
    public FDADeviceProperty(String propertyName, Object propertyValue){
        this.propertyName = propertyName;
        this.propertyValue = propertyValue;
    }
    public String getPropertyName(){
        return this.propertyName;
    }
    public void setPropertyName(String propertyName){
        this.propertyName = propertyName;
    }
    public Object getPropertyValue(){
        return this.propertyValue;
    }
    public void setPropertyValue(Object propertyValue){
        this.propertyValue = propertyValue;
    }
}
