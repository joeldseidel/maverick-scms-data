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
    private List<FDADeviceCustomerContact> getCustomerContacts(String fdaId){
        List<FDADeviceCustomerContact> customerContacts = new ArrayList<>();
        //Prepare query statement to get customer contacts for the specified device id
        String getCustomerContactsSql = "SELECT * FROM device_customer_contacts WHERE fda_id = ?";
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

    private List<FDADeviceSize> getDeviceSizes(String fdaId){
        List<FDADeviceSize> deviceSizes = new ArrayList<>();
        String getDeviceSizesSql = "SELECT * FROM device_device_sizes WHERE fda_id = ?";
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

    private List<FDADeviceGmdnTerm> getGmdnTerms(String fdaId){
        List<FDADeviceGmdnTerm> gmdnTerms = new ArrayList<>();
        String getGmdnTermsSql = "SELECT * FROM device_gmdn_terms WHERE fda_id = ?";
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
