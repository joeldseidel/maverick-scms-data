package maverick_types.FDADeviceTypes;

import java.util.ArrayList;
import java.util.List;

public class FDADevice {
    private List<FDADeviceProperty> deviceProperties = new ArrayList<>();
    private List<FDADeviceCompositePropertyObject> compositeProperties = new ArrayList<>();

    public FDADevice() { }

    /**
     * getPropertyCount returns the size of the device properties array
     * @return the size of the device properties array
     */
    public int getPropertyCount(){
        return this.deviceProperties.size();
    }
    /**
     * getProperty fetches the device property at the specified index
     * @param propertyIndex the index of the device property within the device property list
     * @return the fda device property at the index specified
     */
    public FDADeviceProperty getProperty(int propertyIndex){
        return this.deviceProperties.get(propertyIndex);
    }

    /**
     * Get a property by its name
     * @param propertyName name of the property
     * @return property of that name
     */
    public FDADeviceProperty getProperty(String propertyName){
        for(FDADeviceProperty property : this.deviceProperties){
            if(property.getPropertyName().equals(propertyName)){
                return property;
            }
        }
        return null;
    }

    /**
     * Add a property to the device property collection
     * @param deviceProperty property to add
     */
    public void addProperty(FDADeviceProperty deviceProperty){
        this.deviceProperties.add(deviceProperty);
    }

    /**
     * Getter for device composite properties
     * @return list of composite properties
     */
    public List<FDADeviceCompositePropertyObject> getDeviceCompositeProperties(){
        return this.compositeProperties;
    }

    /**
     * Setter for the composite properties of this device from their objects
     * @param compositeProperties list of composite properties to relate to this device
     */
    public void setCompositeProperties(List<FDADeviceCompositePropertyObject> compositeProperties){
        this.compositeProperties = compositeProperties;
    }

    /**
     * Setter for the generic device properties
     * @param deviceProperties list of device properties
     */
    public void setDeviceProperties(List<FDADeviceProperty> deviceProperties){
        this.deviceProperties = deviceProperties;
    }
}