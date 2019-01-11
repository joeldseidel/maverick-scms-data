package maverick_types.FDADeviceTypes;

import com.joelseidel.java_datatable.Field;
import com.joelseidel.java_datatable.TableRow;
import managers.DeviceDataManager;
import maverick_types.DeviceMovementEvent;
import org.json.JSONObject;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FDADevice {
    List<FDADeviceProperty> deviceProperties = new ArrayList<>();
    List<FDADeviceCustomerContact> deviceCustomerContacts = new ArrayList<>();
    List<FDADeviceSize> deviceSizes = new ArrayList<>();
    List<FDADeviceGmdnTerm> deviceGmdnTerms = new ArrayList<>();
    List<FDADeviceIdentifier> deviceIdentifiers = new ArrayList<>();
    List<FDADevicePremarketSubmission> devicePremarketSubmissions = new ArrayList<>();
    List<FDADeviceProductCode> deviceProductCodes = new ArrayList<>();
    List<FDADeviceStorage> deviceStorages = new ArrayList<>();

    public FDADevice() { }
    public FDADevice(TableRow deviceRecordRow){
        parseDevice(deviceRecordRow);
    }
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
    public void parseDevice(TableRow deviceRecordRow){
        for(Field devicePropertyField : deviceRecordRow.getFields()){
            deviceProperties.add(new FDADeviceProperty(devicePropertyField.getColumn().getName(), devicePropertyField.getValue()));
        }
    }
    public void getDevicePropertyObjects(DeviceDataManager deviceDataManager){
        String fdaId = getProperty("fda_id").getPropertyValue().toString();
        this.deviceCustomerContacts = deviceDataManager.getCustomerContacts(fdaId);
        this.deviceSizes = deviceDataManager.getDeviceSizes(fdaId);
        this.deviceGmdnTerms = deviceDataManager.getGmdnTerms(fdaId);
        this.deviceIdentifiers = deviceDataManager.getIdentifiers(fdaId);
        this.devicePremarketSubmissions = deviceDataManager.getPremarketSubmissions(fdaId);
        this.deviceProductCodes = deviceDataManager.getProductCodes(fdaId);
        this.deviceStorages = deviceDataManager.getStorages(fdaId);
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

    public FDADeviceProperty getProperty(String propertyName){
        for(FDADeviceProperty property : this.deviceProperties){
            if(property.getPropertyName().equals(propertyName)){
                return property;
            }
        }
        return null;
    }

}
