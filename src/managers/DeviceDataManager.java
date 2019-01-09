package managers;

/*
 * Manages the database interaction of getting fda device data from the fda database
 */

import com.joelseidel.java_datatable.DataTable;
import com.joelseidel.java_datatable.TableRow;
import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.FDADeviceTypes.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DeviceDataManager {
    private DatabaseInteraction database;
    public DeviceDataManager(){
        this.database = new DatabaseInteraction(DatabaseType.Devices);
    }

    /**
     * @param fdaId the id of the fda device to fetch and parse from the database
     * @return the fda device object that was fetched and parsed from the database
     * @throws SQLException if and when the database fetching does not work or the results are null
     */

    public FDADevice getDeviceByFdaId(String fdaId) {
        FDADevice thisDevice = new FDADevice();
        //Fetch the fda device from the database, create query to do so
        thisDevice = getDeviceDetails(thisDevice, fdaId);
        thisDevice.getDevicePropertyObjects(this);
        return thisDevice;
    }

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
                String email = customerContactRecord.getField(0).getValue().toString();
                String phone = customerContactRecord.getField(1).getValue().toString();
                String text = customerContactRecord.getField(2).getValue().toString();
                //Add the customer contact record to the object list
                customerContacts.add(new FDADeviceCustomerContact(email, phone, text));
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return customerContacts;
    }

    public List<FDADeviceSize> getDeviceSizes(String fdaId){
        List<FDADeviceSize> deviceSizes = new ArrayList<>();
        String getDeviceSizesSql = "SELECT text, type, value, unit FROM device_device_sizes WHERE fda_id = ?";
        PreparedStatement getDeviceSizesQuery = database.prepareStatement(getDeviceSizesSql);
        try{
            getDeviceSizesQuery.setString(1, fdaId);
            DataTable deviceSizesResult = new DataTable(database.query(getDeviceSizesQuery));
            for(TableRow deviceSizeRecord : deviceSizesResult.getRows()){
                String text = deviceSizeRecord.getField(0).getValue().toString();
                String type = deviceSizeRecord.getField(1).getValue().toString();
                String value = deviceSizeRecord.getField(2).getValue().toString();
                String unit = deviceSizeRecord.getField(3).getValue().toString();
                deviceSizes.add(new FDADeviceSize(text, type, value, unit));
            }
        } catch(SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return deviceSizes;
    }

    public List<FDADeviceGmdnTerm> getGmdnTerms(String fdaId){
        List<FDADeviceGmdnTerm> gmdnTerms = new ArrayList<>();
        String getGmdnTermsSql = "SELECT name, definition FROM device_gmdn_terms WHERE fda_id = ?";
        PreparedStatement getGmdnTermsQuery = database.prepareStatement(getGmdnTermsSql);
        try{
            getGmdnTermsQuery.setString(1, fdaId);
            DataTable gmdnTermsResult = new DataTable(database.query(getGmdnTermsQuery));
            for(TableRow gmdnTermRecord : gmdnTermsResult.getRows()){
                String name = gmdnTermRecord.getField(0).getValue().toString();
                String definition = gmdnTermRecord.getField(1).getValue().toString();
                gmdnTerms.add(new FDADeviceGmdnTerm(name, definition));
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return gmdnTerms;
    }

    public List<FDADeviceIdentifier> getIdentifiers(String fdaId){
        List<FDADeviceIdentifier> identifers = new ArrayList<>();
        String getIdentifiers = "SELECT id, type, issuing_agency, package_discontinue_date, package_status, package_type, quantity_per_package, unit_of_use_id FROM device_identifiers WHERE fda_id = ?";
        PreparedStatement getIdentifiersQuery = database.prepareStatement(getIdentifiers);
        try {
            getIdentifiersQuery.setString(1, fdaId);
            for(TableRow identifierRecord : new DataTable(database.query(getIdentifiersQuery)).getRows()){
                String id = identifierRecord.getField(0).getValue().toString();
                String type = identifierRecord.getField(1).getValue().toString();
                String issuingAgency = identifierRecord.getField(2).getValue().toString();
                String packageDiscontDate = identifierRecord.getField(3).getValue().toString();
                String packageStatus = identifierRecord.getField(4).getValue().toString();
                String packageType = identifierRecord.getField(5).getValue().toString();
                String quantPerPack = identifierRecord.getField(6).getValue().toString();
                String unitUseId = identifierRecord.getField(7).getValue().toString();
                identifers.add(new FDADeviceIdentifier(id, type, issuingAgency, packageDiscontDate, packageStatus, packageType, quantPerPack, unitUseId));
            }
        } catch(SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return identifers;
    }

    public List<FDADevicePremarketSubmission> getPremarketSubmissions(String fdaId){
        List<FDADevicePremarketSubmission> premarkSubs = new ArrayList<>();
        String getPremarkSubsSql = "SELECT submission_number, supplement_number, submission_type FROM device_premarket_submissions WHERE fda_id = ?";
        PreparedStatement getPremarkSubsQuery = database.prepareStatement(getPremarkSubsSql);
        try{
            getPremarkSubsQuery.setString(1, fdaId);
            for(TableRow premarkSubRecord : new DataTable(database.query(getPremarkSubsQuery)).getRows()){
                String submissionNo = premarkSubRecord.getField(0).getValue().toString();
                String supplementNo = premarkSubRecord.getField(1).getValue().toString();
                String submissionType = premarkSubRecord.getField(2).getValue().toString();
                premarkSubs.add(new FDADevicePremarketSubmission(submissionNo, supplementNo, submissionType));
            }
        } catch(SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return premarkSubs;
    }

    public List<FDADeviceProductCode> getProductCodes(String fdaId){
        List<FDADeviceProductCode> productCodes = new ArrayList<>();
        String getProductCodesSql = "SELECT code, name FROM device_product_codes WHERE fda_id = ?";
        PreparedStatement getProductCodesQuery = database.prepareStatement(getProductCodesSql);
        try {
            getProductCodesQuery.setString(1, fdaId);
            for(TableRow productCodeRecord : new DataTable(database.query(getProductCodesQuery)).getRows()){
                String code = productCodeRecord.getField(0).getValue().toString();
                String name = productCodeRecord.getField(1).getValue().toString();
                productCodes.add(new FDADeviceProductCode(code, name));
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return productCodes;
    }

    public List<FDADeviceStorage> getStorages(String fdaId){
        List<FDADeviceStorage> deviceStorages = new ArrayList<>();
        String getStorageSql = "SELECT high_value, high_unit, low_value, low_unit, special_conditions, type FROM device_storage WHERE fda_id = ?";
        PreparedStatement getStorageQuery = database.prepareStatement(getStorageSql);
        try {
            getStorageQuery.setString(1, fdaId);
            for(TableRow storageRecord : new DataTable(database.query(getStorageQuery)).getRows()){
                String highValue = storageRecord.getField(0).getValue().toString();
                String highUnit = storageRecord.getField(1).getValue().toString();
                String lowValue = storageRecord.getField(2).getValue().toString();
                String lowUnit = storageRecord.getField(3).getValue().toString();
                String specialConditions = storageRecord.getField(4).getValue().toString();
                String type = storageRecord.getField(5).getValue().toString();
                deviceStorages.add(new FDADeviceStorage(highValue, highUnit, lowValue, lowUnit, specialConditions, type));
            }
        } catch (SQLException sqlEx) {
            sqlEx.printStackTrace();
        }
        return deviceStorages;
    }

    public List<FDADevice> getCompanyDevicesForImport(String company_name) {
        String getCompanyDeviceRecordSql = "SELECT fda_id, device_name, medical_specialty_description FROM devices WHERE company_name = ?";
        PreparedStatement getCompanyDeviceStatement = database.prepareStatement(getCompanyDeviceRecordSql);
        DataTable companyDevices;
        try {
            getCompanyDeviceStatement.setString(1, company_name);
            companyDevices = new DataTable(database.query(getCompanyDeviceStatement));
        } catch(SQLException sqlEx) {
            sqlEx.printStackTrace();
            return null;
        }
        List<FDADevice> companyDevicesList = new ArrayList<>();
        for(TableRow companyDeviceRecord : companyDevices.getRows()){
            companyDevicesList.add(new FDADevice(companyDeviceRecord));
        }
        return companyDevicesList;
    }
}
