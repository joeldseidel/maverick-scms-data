package managers;

import maverick_data.Config;
import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.LotType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

/*
 * @author Joel Seidel
 *
 * Manager class for generating an maintain lot numbers for both items and pallets
 */


public class LotNumberManager {
    public long generateLotNumber(LotType lotType){
        //Generate a random lot number to lot specifications, check if unique, and re-gen until unique
        if(lotType == LotType.Item){
            //Generate an item lot number until the generated lot is unique
            long randomLot;
            do{
                //Generate the random lot number
                randomLot = getRandomLotNumber(lotType);
                //Generate lot numbers until unique
            } while(!isUniqueLot(lotType, randomLot));
            return randomLot;
        }
        //TODO: write the pallet manifest generation when pallet manifests are built
        return 0;
    }

    private long getRandomLotNumber(LotType lotType){
        ThreadLocalRandom randomLot = ThreadLocalRandom.current();
        long generatedLotNumber = 0;
        if(lotType == LotType.Item){
            generatedLotNumber = randomLot.nextLong(10_000_000L, 100_000_000L);
        } else if(lotType == LotType.Pallet) {
            generatedLotNumber = randomLot.nextLong(1_000_000_000L, 10_000_000_000L);
        }
        return generatedLotNumber;
    }

    private boolean isUniqueLot(LotType lotType, long generatedLot){
        String checkTable = lotType == LotType.Item ? "table_items" : "table_pallets";
        String getMatchingLotNumberSql = "SELECT COUNT(1) FROM " + checkTable + " WHERE mid = ?";
        DatabaseInteraction database = new DatabaseInteraction(DatabaseType.AppData);
        int matchingLotCount = 0;
        try{
            PreparedStatement matchingLotQuery = database.prepareStatement(getMatchingLotNumberSql);
            matchingLotQuery.setString(1, Long.toString(generatedLot));
            ResultSet matchingLotResult = database.query(matchingLotQuery);
            if(matchingLotResult.next()){
                matchingLotCount = matchingLotResult.getInt(0);
            }
        } catch(SQLException sqlEx){
            sqlEx.printStackTrace();
        }
        return matchingLotCount == 0;
    }
}
