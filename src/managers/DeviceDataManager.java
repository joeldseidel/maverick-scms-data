package managers;

/*
 * Manages the database interaction of getting fda device data from the fda database
 */

import com.joelseidel.java_datatable.DataTable;
import com.joelseidel.java_datatable.TableRow;
import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.FDADevice;

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
        //Fetch the fda device from the database, create query to do so
        String getDeviceRecordSql = "SELECT * FROM devices WHERE fda_id = ?";
        PreparedStatement getDeviceRecordQuery = database.prepareStatement(getDeviceRecordSql);
        FDADevice thisDevice = new FDADevice();
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
