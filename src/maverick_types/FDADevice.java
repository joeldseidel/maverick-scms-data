package maverick_types;

import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FDADevice {
    List<FDADeviceProperty> deviceProperties = new ArrayList<>();
    public FDADevice() { }
    /**
     * parseDevice parses a device from its database record to an FDADevice object which access to its properties
     * @param resultSet the incremented result set containing the row/record from the database
     */
    public void parseDevice(ResultSet resultSet){
        try {
            //Get the column count from the result set to allow for the increment to occur
            ResultSetMetaData metaData = resultSet.getMetaData();
            int columnCount = metaData.getColumnCount();
            //Loop through all the columns, get the column name and column value, and create a device property
            for(int i = 1; i <= columnCount; i++){
                //Get the property value from the specified column
                Object propertyValue = resultSet.getObject(i);
                //Get the column name of the specified column
                String columnName = metaData.getColumnName(i);
                //Create the new device property and add the new property to the list of device properties
                deviceProperties.add(new FDADeviceProperty(columnName, propertyValue));
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
    }
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
     * serializeToJson converts a device object back into a json object of its properties
     * @param fdaDevice the device to be serialized into a json object
     * @return the json object created from serializing the fda device object
     */
    public JSONObject serializeToJson(FDADevice fdaDevice){
        //Create the empty json object for this device to be serialized into
        JSONObject thisDeviceJson = new JSONObject();
        //Loop through every device property and convert it to a JSON property
        for(int i = 0; i < fdaDevice.getPropertyCount(); i++){
            //Get the specified device property
            FDADeviceProperty thisProperty = fdaDevice.getProperty(i);
            //Enter the specified property into the json object with key name and value
            thisDeviceJson.put(thisProperty.getPropertyName(), thisProperty.getPropertyValue());
        }
        return thisDeviceJson;
    }
}
