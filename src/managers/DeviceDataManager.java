package managers;

import com.joelseidel.java_datatable.DataTable;
import com.joelseidel.java_datatable.TableRow;
import com.joelseidel.java_datatable.Field;
import maverick_types.DatabaseType;
import maverick_types.FDADeviceTypes.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the database interaction of the handlers as pertains to the FDA device data
 * @author Joel Seidel
 */
public class DeviceDataManager extends ManagerPrototype {
    /**
     * Default constructor
     * Creates database connection in the super class
     */
    public DeviceDataManager(){
        //Create db connection
        initDb(DatabaseType.Devices);
    }

    /**
     * @param fdaId the id of the fda device to fetch and parse from the database
     * @return the fda device object that was fetched and parsed from the database
     */
    public FDADevice getDeviceByFdaId(String fdaId) {
        FDADevice thisDevice = new FDADevice();
        //Fetch the fda device from the database
        thisDevice = getDeviceDetails(thisDevice, fdaId);
        return thisDevice;
    }

    /**
     * Get main device record from the database and parse to an FDA Device object
     * @param thisDevice FDA device to parse to (this is a by ref work around)
     * @param fdaId fda of the device to fetch
     * @return altered FDA device from argument (by ref if it existed)
     */
    private FDADevice getDeviceDetails(FDADevice thisDevice, String fdaId) {
        String getDeviceRecordSql = "SELECT * FROM devices WHERE fda_id = ?";
        PreparedStatement getDeviceRecordQuery = database.prepareStatement(getDeviceRecordSql);
        try {
            getDeviceRecordQuery.setString(1, fdaId);
            //Get the device from the database from prepared statement by fda id
            ResultSet deviceRecordResults = database.query(getDeviceRecordQuery);
            //Increment the device record results to the first (and only) row matching the id
            if (deviceRecordResults.next()) {
                //Parse the device from the row result
                thisDevice.setDeviceProperties(getDeviceProperties(deviceRecordResults));
                //Get the composite property objects of the device
                thisDevice.setCompositeProperties(getDeviceCompositeProperties(fdaId));
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return thisDevice;
    }

    /**
     * getDeviceProperties parses a device from its database record to an FDADevice object which access to its properties
     * @param resultSet the incremented result set containing the row/record from the database
     */
    private List<FDADeviceProperty> getDeviceProperties(ResultSet resultSet){
        List<FDADeviceProperty> deviceProperties = new ArrayList<>();
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
        return deviceProperties;
    }


    /**
     * It's a long story and you can read it here now and in book stores all over the world in the near future as it will be a best seller
     *
     * A device has a main record, this is what is pulled in and called the device details. This was just parsed. Now we must go and get the
     *      subobjects of the device. This can be confusing because of the naming. These subobjects are called composite property objects
     *      because they are properties made of other properties. See? It makes sense. All composite properties are generically the same and
     *      are only unique in the fields they have, but that is all abstracted away in other classes. This method goes and gets all of them at
     *      once in the most efficient way I could figure out. Thanks for reading, this comment dedicated to John McCain, miss you father
     *
     * @param fdaId fda id of the device to get the composite properties of
     * @return a list of generic composite properties containing their generic properties if they have any
     */
    private List<FDADeviceCompositePropertyObject> getDeviceCompositeProperties(String fdaId){
        //2D array, rows are sub objects, col 1 is name, col 2 is select statement to get it
        String getPropertyObjectsSql[][] = {
                {"Customer Contacts",       "SELECT email, phone, text FROM device_customer_contacts WHERE fda_id = ?"},
                {"Sizes",                   "SELECT text, type, value, unit FROM device_device_sizes WHERE fda_id = ?"},
                {"GMDN Terms",              "SELECT name, definition FROM device_gmdn_terms WHERE fda_id = ?"},
                {"Identifiers",             "SELECT id, type, issuing_agency, package_discontinue_date, package_status, package_type, quantity_per_package, unit_of_use_id FROM device_identifiers WHERE fda_id = ?"},
                {"Premarket Submissions",   "SELECT submission_number, supplement_number, submission_type FROM device_premarket_submissions WHERE fda_id = ?"},
                {"Product Codes",           "SELECT code, name FROM device_product_codes WHERE fda_id = ?"},
                {"Storage",                 "SELECT high_value, high_unit, low_value, low_unit, special_conditions, type FROM device_storage WHERE fda_id = ?"}
        };
        List<FDADeviceCompositePropertyObject> compositePropertyObjects = new ArrayList<>();
        for(String propertyObject[] : getPropertyObjectsSql){
            //Create query statement with sql string in col 2
            PreparedStatement getPropertyStmt = database.prepareStatement(propertyObject[1]);
            try{
                //Prepare primary key to all statements
                getPropertyStmt.setString(1, fdaId);
                //Perform get property object query
                DataTable propertyResults = new DataTable(database.query(getPropertyStmt));
                //Get records from the data table
                for(TableRow compositePropertyRecord : propertyResults.getRows()){
                    //Get the properties of the resulting property objects
                    List<FDADeviceProperty> propertyProperties = getCompositePropertySubProperties(compositePropertyRecord);
                    //Instantiate a generic composite property object object with the name in col 1 and the resulting sub properties and add to collection
                    compositePropertyObjects.add(new FDADeviceCompositePropertyObject(propertyObject[0], propertyProperties));
                }
            } catch (SQLException sqlEx){
                sqlEx.printStackTrace();
            }
        }
        return compositePropertyObjects;
    }

    /**
     * Get the properties of the composite property objects
     * @param compositePropertyRecord db query results table row
     * @return list of compositite property properties
     */
    private List<FDADeviceProperty> getCompositePropertySubProperties(TableRow compositePropertyRecord){
        List<FDADeviceProperty> compositePropertyProperties = new ArrayList<>();
        //Get fields from the data rows
        for(Field compositePropertyField : compositePropertyRecord.getFields()){
            if(compositePropertyField.getValue() != null){
                //This field is not null so it represents a property that this composite object has
                compositePropertyProperties.add(new FDADeviceProperty(compositePropertyField.getColumn().getName(), compositePropertyField.getValue().toString()));
            }
        }
        return compositePropertyProperties;
    }

    /**
     * Get the data from the fda database needed to create a maverick database record
     * @param company_name explicit name of the company as found in the FDA data (not determined by us)
     * @return a list of devices matching the company name
     */
    public List<FDADevice> getCompanyDevicesForImport(String company_name) {
        //Create the get company devices query
        //Select only the properties that match in the maverick data
        String getCompanyDeviceRecordSql = "SELECT fda_id, device_name, medical_specialty_description FROM devices WHERE company_name = ?";
        PreparedStatement getCompanyDeviceStatement = database.prepareStatement(getCompanyDeviceRecordSql);
        DataTable companyDevices;
        try {
            getCompanyDeviceStatement.setString(1, company_name);
            //Do the company devices query
            companyDevices = new DataTable(database.query(getCompanyDeviceStatement));
        } catch(SQLException sqlEx) {
            sqlEx.printStackTrace();
            return null;
        }
        List<FDADevice> companyDevicesList = new ArrayList<>();
        //Parse the data table into a list of the devices it represents
        for(TableRow companyDeviceRecord : companyDevices.getRows()){
            //Add the parsed fda device to the collection
            FDADevice thisDevice = new FDADevice();
            //Parse the fda device properties from the record
            for(Field devicePropertyField : companyDeviceRecord.getFields()){
                thisDevice.addProperty(new FDADeviceProperty(devicePropertyField.getColumn().getName(), devicePropertyField.getValue()));
            }
            //Add the new device to the collection
            companyDevicesList.add(thisDevice);
        }
        return companyDevicesList;
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
        //Create a JSON array to hold all of the objects
        JSONArray compositePropertyArray = new JSONArray();
        //Serialize the property objects
        for(FDADeviceCompositePropertyObject compositeProperty : fdaDevice.getDeviceCompositeProperties()){
            //Determine if this composite property is relevant to the device based on property count
            if(compositeProperty.getPropertyCount() > 0){
                //Create a JSON object to hold the properties of this composite property
                JSONObject thisCompositeProperty = new JSONObject();
                //Set the name of the composite property itself outside its property array
                thisCompositeProperty.put("prop-name", compositeProperty.getName());
                //Create a JSON array to hold all of the properties within this composite property
                JSONArray compositePropertyPropertiesArray = new JSONArray();
                for(FDADeviceProperty thisProperty : compositeProperty.getProperties()){
                    //Create a JSON object to hold the details
                    JSONObject thisPropertyObj = new JSONObject();
                    //Set the property key and value from property object
                    thisPropertyObj.put(thisProperty.getPropertyName(), thisProperty.getPropertyValue());
                    //Add the new property object to the collection
                    compositePropertyPropertiesArray.put(thisPropertyObj);
                }
                //Add the properties array to the composite property object
                thisCompositeProperty.put("properties", compositePropertyPropertiesArray);
                //Add the composite property object to the array of composite properties
                compositePropertyArray.put(thisCompositeProperty);
            }
        }
        //Add the device composite properties to the device JSON object
        thisDeviceJson.put("comp-props", compositePropertyArray);
        return thisDeviceJson;
    }
}
