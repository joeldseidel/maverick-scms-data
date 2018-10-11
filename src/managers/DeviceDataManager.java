package managers;

/*
 * Manages the database interaction of getting fda device data from the fda database
 */

import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.FDADevice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

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

    public FDADevice getDeviceByFdaId(String fdaId) throws SQLException{
        //Fetch the fda device from the database, create query to do so
        String getDeviceRecordSql = "SELECT * FROM fda_data_devices WHERE fdaid = ?";
        PreparedStatement getDeviceRecordQuery = database.prepareStatement(getDeviceRecordSql);
        getDeviceRecordQuery.setString(1, fdaId);
        //Get the device from the database from prepared statement by fda id
        ResultSet deviceRecordResults = database.query(getDeviceRecordQuery);
        FDADevice thisDevice = new FDADevice();
        //Increment the device record results to the first (and only) row matching the id
        if(deviceRecordResults.next()){
            //Parse the device from the row result
            thisDevice.parseDevice(deviceRecordResults);
        }
        return thisDevice;
    }
}
