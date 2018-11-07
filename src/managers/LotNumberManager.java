package managers;

import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.LotType;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ThreadLocalRandom;

import net.sourceforge.barbecue.Barcode;
import net.sourceforge.barbecue.BarcodeFactory;
import net.sourceforge.barbecue.BarcodeImageHandler;
import org.apache.commons.codec.binary.Base64OutputStream;

import javax.imageio.ImageIO;

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

    /**
     * Create a barcode image from provided lot number and converts to base 64 string for return
     * @param lotNumber lot number to generate barcode for
     * @return Base64 string containing the barcode image
     */
    public String generateLotBarcodeString(long lotNumber){
        String barcodeString = "";
        try {
            //Generate the Codabar barcode image
            Barcode barCode = BarcodeFactory.createCodabar(Long.toString(lotNumber));
            //Convert the barcode into a buffered image
            BufferedImage barcodeImage = BarcodeImageHandler.getImage(barCode);
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            OutputStream b64 = new Base64OutputStream(os);
            //Convert the image to a base64 string from png
            ImageIO.write(barcodeImage, "png", b64);
            //Convert the base 64 string to a UTF-8 string for return
            barcodeString = os.toString("UTF-8");
        } catch(Exception barcodeException){
            //Things this ain't: it
            barcodeException.printStackTrace();
            return null;
        }
        return barcodeString;
    }
}
