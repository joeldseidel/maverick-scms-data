package managers;

import com.joelseidel.java_datatable.DataTable;
import com.joelseidel.java_datatable.TableRow;
import maverick_types.DatabaseType;
import maverick_types.FDADeviceTypes.*;
import org.json.JSONObject;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
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
        //Get the related property objects
        //NOTE: this manager is passed to prevent a redundant database connection
        thisDevice.getDevicePropertyObjects(this);
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
                thisDevice.parseDevice(deviceRecordResults);
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return thisDevice;
    }

    /**
     * Create a list of customer contacts for a given fda id from the database
     * @param fdaId fda id of device
     * @return list of customer contacts for device
     */
    public List<FDADeviceCustomerContact> getCustomerContacts(String fdaId){
        List<FDADeviceCustomerContact> customerContacts = new ArrayList<>();
        //Prepare query statement to get customer contacts for the specified device id
        String getCustomerContactsSql = "SELECT email, phone, text FROM device_customer_contacts WHERE fda_id = ?";
        PreparedStatement getCustomerContactsQuery = database.prepareStatement(getCustomerContactsSql);
        try{
            getCustomerContactsQuery.setString(1, fdaId);
            //Get data table result of customer contact query
            DataTable customerContactsResult = new DataTable(database.query(getCustomerContactsQuery));
            //Create a customer contact object for each query result record
            for(TableRow customerContactRecord : customerContactsResult.getRows()){
                //Get the necessary data from each of the record
                String email = String.valueOf(customerContactRecord.getField(0).getValue());
                String phone = String.valueOf(customerContactRecord.getField(1).getValue());
                String text = String.valueOf(customerContactRecord.getField(2).getValue());
                //Add the customer contact record to the object list
                customerContacts.add(new FDADeviceCustomerContact(email, phone, text));
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return customerContacts;
    }

    /**
     * Get device sizes for specific device
     * @param fdaId fda of the device's sizes to fetch
     * @return list of device sizes pertaining to the specifed device
     */
    public List<FDADeviceSize> getDeviceSizes(String fdaId){
        List<FDADeviceSize> deviceSizes = new ArrayList<>();
        //Prepare get device sizes sql statement
        String getDeviceSizesSql = "SELECT text, type, value, unit FROM device_device_sizes WHERE fda_id = ?";
        PreparedStatement getDeviceSizesQuery = database.prepareStatement(getDeviceSizesSql);
        try{
            getDeviceSizesQuery.setString(1, fdaId);
            //Create data table for the result query
            DataTable deviceSizesResult = new DataTable(database.query(getDeviceSizesQuery));
            for(TableRow deviceSizeRecord : deviceSizesResult.getRows()){
                //Get necessary fields values for the object instantiation
                String text = String.valueOf(deviceSizeRecord.getField(0).getValue());
                String type = String.valueOf(deviceSizeRecord.getField(1).getValue().toString());
                String value = String.valueOf(deviceSizeRecord.getField(2).getValue());
                String unit = String.valueOf(deviceSizeRecord.getField(3).getValue());
                //Add device size object to the device collection
                deviceSizes.add(new FDADeviceSize(text, type, value, unit));
            }
        } catch(SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return deviceSizes;
    }

    /**
     * Get the GMDN term objects related to a specified device
     * @param fdaId specified fda device id
     * @return a list of the resulting GMDN term objects
     */
    public List<FDADeviceGmdnTerm> getGmdnTerms(String fdaId){
        List<FDADeviceGmdnTerm> gmdnTerms = new ArrayList<>();
        //Create get gmdn terms sql statement
        String getGmdnTermsSql = "SELECT name, definition FROM device_gmdn_terms WHERE fda_id = ?";
        PreparedStatement getGmdnTermsQuery = database.prepareStatement(getGmdnTermsSql);
        try{
            getGmdnTermsQuery.setString(1, fdaId);
            //Create data table for the result
            DataTable gmdnTermsResult = new DataTable(database.query(getGmdnTermsQuery));
            for(TableRow gmdnTermRecord : gmdnTermsResult.getRows()){
                //Get fields for gmdn term instantiation
                String name = String.valueOf(gmdnTermRecord.getField(0).getValue());
                String definition = String.valueOf(gmdnTermRecord.getField(1).getValue());
                //Add new gmdn term to device collection
                gmdnTerms.add(new FDADeviceGmdnTerm(name, definition));
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return gmdnTerms;
    }

    /**
     * Get the identifier property objects for a specified device
     * @param fdaId the specified device fda id
     * @return a list of the device identifiers related to the specified device
     */
    public List<FDADeviceIdentifier> getIdentifiers(String fdaId){
        List<FDADeviceIdentifier> identifers = new ArrayList<>();
        //Create get identifiers sql statement
        String getIdentifiers = "SELECT id, type, issuing_agency, package_discontinue_date, package_status, package_type, quantity_per_package, unit_of_use_id FROM device_identifiers WHERE fda_id = ?";
        PreparedStatement getIdentifiersQuery = database.prepareStatement(getIdentifiers);
        try {
            getIdentifiersQuery.setString(1, fdaId);
            //Iterate through each device identifier record from query
            for(TableRow identifierRecord : new DataTable(database.query(getIdentifiersQuery)).getRows()){
                //Get field values necessary for device identifier instantiation
                String id = String.valueOf(identifierRecord.getField(0).getValue());
                String type = String.valueOf(identifierRecord.getField(1).getValue());
                String issuingAgency = String.valueOf(identifierRecord.getField(2).getValue());
                String packageDiscontDate = String.valueOf(identifierRecord.getField(3).getValue());
                String packageStatus = String.valueOf(identifierRecord.getField(4).getValue());
                String packageType = String.valueOf(identifierRecord.getField(5).getValue());
                String quantPerPack = String.valueOf(identifierRecord.getField(6).getValue());
                String unitUseId = String.valueOf(identifierRecord.getField(7).getValue());
                //Add identifier to the device collection
                identifers.add(new FDADeviceIdentifier(id, type, issuingAgency, packageDiscontDate, packageStatus, packageType, quantPerPack, unitUseId));
            }
        } catch(SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return identifers;
    }

    /**
     * Get premarket submission property object for a specified device
     * @param fdaId the specified device id
     * @return a list of premarket submission objects related to the specified device
     */
    public List<FDADevicePremarketSubmission> getPremarketSubmissions(String fdaId){
        List<FDADevicePremarketSubmission> premarkSubs = new ArrayList<>();
        //Create the get premarket submission query
        String getPremarkSubsSql = "SELECT submission_number, supplement_number, submission_type FROM device_premarket_submissions WHERE fda_id = ?";
        PreparedStatement getPremarkSubsQuery = database.prepareStatement(getPremarkSubsSql);
        try{
            getPremarkSubsQuery.setString(1, fdaId);
            //Iterate through each of the premarket submission records from performed query
            for(TableRow premarkSubRecord : new DataTable(database.query(getPremarkSubsQuery)).getRows()){
                //Get the necessary fields to instantiate a premarket submission object
                String submissionNo = String.valueOf(premarkSubRecord.getField(0).getValue());
                String supplementNo = String.valueOf(premarkSubRecord.getField(1).getValue());
                String submissionType = String.valueOf(premarkSubRecord.getField(2).getValue());
                //Add the new premarket submission to the device collection
                premarkSubs.add(new FDADevicePremarketSubmission(submissionNo, supplementNo, submissionType));
            }
        } catch(SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return premarkSubs;
    }

    /**
     * Get the product code property objects related to a specified device
     * @param fdaId the fda id of the specified device
     * @return a list of the product code objects related to the specified device
     */
    public List<FDADeviceProductCode> getProductCodes(String fdaId){
        List<FDADeviceProductCode> productCodes = new ArrayList<>();
        //Create the get product codes sql query
        String getProductCodesSql = "SELECT code, name FROM device_product_codes WHERE fda_id = ?";
        PreparedStatement getProductCodesQuery = database.prepareStatement(getProductCodesSql);
        try {
            getProductCodesQuery.setString(1, fdaId);
            //Iterate through each of the product code records from the performed query
            for(TableRow productCodeRecord : new DataTable(database.query(getProductCodesQuery)).getRows()){
                //Get the field values necessary to instantiate a new product code object
                String code = String.valueOf(productCodeRecord.getField(0).getValue());
                String name = String.valueOf(productCodeRecord.getField(1).getValue());
                //Add a new product code to the device collection
                productCodes.add(new FDADeviceProductCode(code, name));
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return productCodes;
    }

    /**
     * Get the storage property objects related to a specified device
     * @param fdaId the specified device fda id
     * @return list of storage objects related to the specified device
     */
    public List<FDADeviceStorage> getStorages(String fdaId){
        List<FDADeviceStorage> deviceStorages = new ArrayList<>();
        //Create the get storage query sql statement
        String getStorageSql = "SELECT high_value, high_unit, low_value, low_unit, special_conditions, type FROM device_storage WHERE fda_id = ?";
        PreparedStatement getStorageQuery = database.prepareStatement(getStorageSql);
        try {
            getStorageQuery.setString(1, fdaId);
            //Iterate through each of the storage records from the performed query
            for(TableRow storageRecord : new DataTable(database.query(getStorageQuery)).getRows()){
                //Get the fields necessary for instantiation of a storage object
                String highValue = String.valueOf(storageRecord.getField(0).getValue());
                String highUnit = String.valueOf(storageRecord.getField(1).getValue());
                String lowValue = String.valueOf(storageRecord.getField(2).getValue());
                String lowUnit = String.valueOf(storageRecord.getField(3).getValue());
                String specialConditions = String.valueOf(storageRecord.getField(4).getValue());
                String type = String.valueOf(storageRecord.getField(5).getValue());
                //Add a new storage object to the device collection
                deviceStorages.add(new FDADeviceStorage(highValue, highUnit, lowValue, lowUnit, specialConditions, type));
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return deviceStorages;
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
            companyDevicesList.add(new FDADevice(companyDeviceRecord));
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
        //Serialize the property objects

        return thisDeviceJson;
    }
}
