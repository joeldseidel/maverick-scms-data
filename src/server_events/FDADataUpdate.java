package server_events;

import maverick_data.Config;
import maverick_data.DatabaseInteraction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static java.util.Objects.isNull;

/**
 * This class handles the server scheduled event to update the local FDA data
 *
 * @author Joel Seidel
 */

public class FDADataUpdate implements Runnable {
    private Thread fdaUpdateThread;
    private File destinationFile;
    private final String localDataFile = System.getProperty("user.home") +"/fda_data_files";

    public FDADataUpdate(){
        fdaUpdateThread = new Thread(this);
        fdaUpdateThread.start();
    }

    @Override
    public void run(){
        System.out.println("Starting to update FDA data on thread " + fdaUpdateThread.getName());
        while(!Thread.interrupted()){
            //fetchFDAFiles();
            parseFDAFiles();
        }
    }

    private void parseFDAFiles(){
        //Get all local storage file contents (this will be all the data files)
        File dataFilesList[] = new File(localDataFile).listFiles();
        for(int i = 0; i < dataFilesList.length; i++){
            System.out.println("Working on parsing data file " + (i + 1) + "/" + dataFilesList.length);
            //It's going to be the only file in its parent, just get the path from it
            File thisDataFilePath = new File(dataFilesList[i].listFiles()[0].getPath());
            parseFDAFileToDatabase(thisDataFilePath);
        }
    }

    private void parseFDAFileToDatabase(File parseFromFile){
        JSONObject queryMetaDataObject = getMetaData(parseFromFile);
        int totalRecordCount = queryMetaDataObject.getJSONObject("results").getInt("total") - queryMetaDataObject.getJSONObject("results").getInt("skip");
        int recordCounter = 1;
        boolean remainingObjectsInFile = true;
        DatabaseInteraction database = new DatabaseInteraction(Config.host, Config.port, Config.user, Config.pass, Config.databaseName);
        do {
            System.out.println("Writing record " + recordCounter + "/" + totalRecordCount);
            JSONObject readObject = getNextJsonObjectFromFile(parseFromFile);
            if(!isNull(readObject)){
                writeDevice(readObject, database);
            } else {
                remainingObjectsInFile = false;
            }
            recordCounter++;
        } while(remainingObjectsInFile);
    }

    private JSONObject getNextJsonObjectFromFile(File parseFromFile){
        String thisJsonObjectString = "";
        try(BufferedReader br = new BufferedReader(new FileReader(parseFromFile))){
            //Advance reader to the line we need
            for(int i = 0; i <= lastLineIndex; i++){
                br.readLine();
            }
            String thisLine;
            int openObjectCount = 0; int closedOjectCount = 0;
            //Check to see if we have to throw out that first line
            if(resultIndex == 0){
                //Yup, throw out that first line
                br.readLine();
            }
            while((thisLine = br.readLine()) != null && (openObjectCount != closedOjectCount || openObjectCount == 0)){
                if(thisLine.contains("{")){
                    openObjectCount++;
                }
                if(thisLine.contains("}")){
                    closedOjectCount++;
                }
                thisJsonObjectString += thisLine;
                lastLineIndex++;
            }
        } catch(IOException ioE){
            return null;
        }
        return new JSONObject(thisJsonObjectString);
    }

    //I hate to make this global but we need to return a few different values, I'm sorry programming gods
    private int lastLineIndex = 0; private int resultIndex = 0;
    private JSONObject getMetaData(File parseFromFile){
        String metaDataObjectString = "{ ";
        try(BufferedReader br = new BufferedReader(new FileReader(parseFromFile))){
            String thisReadLine;
            int openObjectCount = 0; int closedObjectCount = 0;
            //Throw out the first line, it does not matter
            br.readLine();
            while((thisReadLine = br.readLine()) != null && (openObjectCount != closedObjectCount || openObjectCount == 0)){
                if (thisReadLine.contains("{")) {
                    openObjectCount++;
                }
                if(thisReadLine.contains("}")){
                    closedObjectCount++;
                }
                metaDataObjectString += thisReadLine;
                lastLineIndex++;
            }
        } catch(IOException ioExcept){
            //This won't happen. bet.
            return null;
        }
        metaDataObjectString += "}";
        return new JSONObject(metaDataObjectString).getJSONObject("meta");
    }

    private void fetchFDAFiles(){
        String fdaFilesUrls[] = getFilesUrl();
        for(int i = 0; i < fdaFilesUrls.length; i++){
            String thisFileUrlString = fdaFilesUrls[i];
            URL url;
            try{
               url = new URL(thisFileUrlString);
            } catch(MalformedURLException malformedUrlException){
                System.out.println("Bad url to fetch file at " + thisFileUrlString);
                return;
            }
            HttpURLConnection httpConn;
            int fetchFileResponseCode;
            try{
                httpConn = (HttpURLConnection)url.openConnection();
                fetchFileResponseCode = httpConn.getResponseCode();
            } catch(IOException ioException){
                System.out.println("Could not open connection to fetch file at " + thisFileUrlString);
                return;
            }
            if(fetchFileResponseCode == HttpURLConnection.HTTP_OK){
                if(!fetchFile(httpConn)){
                    return;
                }
            } else {
                System.out.println("Bad request code to fetch file at " + thisFileUrlString);
            }
            parseFDAFiles();
        }
    }

    private String[] getFilesUrl(){
        //Fetch the FDA data files meta file
        String fdaFileDataUrlString = "https://api.fda.gov/download.json";
        URL fdaFileDataFile;
        try{
            fdaFileDataFile = new URL(fdaFileDataUrlString);
        } catch(MalformedURLException mUException){
            System.out.println("Could not fetch FDA files meta file (bad url). Is the FDA system up?");
            fdaUpdateThread.interrupt();
            return null;
        }
        HttpURLConnection httpConn;
        int fdaFileDataFileResponseCode;
        try{
            httpConn = (HttpURLConnection)fdaFileDataFile.openConnection();
            fdaFileDataFileResponseCode = httpConn.getResponseCode();
        } catch(IOException ioException) {
            System.out.println("FDA server rejected request (bad response code)");
            fdaUpdateThread.interrupt();
            return null;
        }
        if(fdaFileDataFileResponseCode == HttpURLConnection.HTTP_OK){
            if(!fetchFile(httpConn)){
                System.out.println("Could not fetch FDA files meta file. Is the FDA system up?");
                fdaUpdateThread.interrupt();
            }
        }
        JSONObject fileListRoot = getJSONRootFromFile(destinationFile);
        JSONArray fileJSONArray = fileListRoot.getJSONObject("results").getJSONObject("device").getJSONObject("udi").getJSONArray("partitions");
        System.out.println("Found " + fileJSONArray.length() + " files to fetch");
        String fileUrlArray[] = new String[fileJSONArray.length()];
        for(int i = 0; i < fileJSONArray.length(); i++){
            String thisFileUrlString = fileJSONArray.getJSONObject(i).getString("file");
            fileUrlArray[i] = thisFileUrlString;
        }
        do {
            //The delete process sometimes needs to be written more than once
           destinationFile.delete();
        } while(destinationFile.exists());
        return fileUrlArray;
    }

    private boolean fetchFile(HttpURLConnection httpConn){
        FDAFile thisFile = getFileAttributes(httpConn);
        System.out.println("Fetching " + thisFile.getFileName() + ";  Disposition: " + thisFile.getDisposition() + ";  Content Type: " + thisFile.getContentType() + ";  Content Length: " + thisFile.getContentLength());
        try{
            destinationFile = createFetchDestination(thisFile);
        } catch(IOException ioException){
            System.out.println("Error in creating destination file and directory");
            return false;
        }
        //Fetch file from URL and read into destination file
        try{
            System.out.println("Started fetching file " + thisFile.getFileName());
            downloadFileContent(httpConn, destinationFile);
            System.out.println("Completed fetching file " + thisFile.getFileName());
        } catch(IOException ioException) {
            System.out.println("Could not download the file " + thisFile.getFileName());
            return false;
        }
        if(thisFile.getContentType().contains("zip")){
            //Decompress the zip file created from downloading the file (this is how it is downloaded from FDA)
            try{
                System.out.println("\nStarted decompressing file " + thisFile.getFileName());
                decompressFileContent(destinationFile, thisFile);
            } catch(IOException ioException){
                System.out.println("Could not decompress the file: " + thisFile.getFileName());
                return false;
            }
        }
        return true;
    }

    private void downloadFileContent(HttpURLConnection httpConn, File destinationFile) throws IOException{
        InputStream inputStream = httpConn.getInputStream();
        FileOutputStream outputStream = new FileOutputStream(destinationFile);
        int bytesRead;
        byte[] inputBuffer = new byte[256000];
        while((bytesRead = inputStream.read(inputBuffer)) != -1){
            outputStream.write(inputBuffer, 0, bytesRead);
        }
        outputStream.close();
        inputStream.close();
    }

    private void decompressFileContent(File compressedFile, FDAFile thisFile) throws IOException{
        String zipFilePathString = compressedFile.getPath();
        String destinationDirectoryPathString = zipFilePathString.substring(zipFilePathString.lastIndexOf("/") + 1, zipFilePathString.lastIndexOf("."));
        File decompressedFileDestination = new File(destinationDirectoryPathString);
        decompressedFileDestination.mkdir();
        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(compressedFile));
        ZipEntry zipEntry = zipInputStream.getNextEntry();
        while(zipEntry != null){
            File contentFile = new File(destinationDirectoryPathString + File.separator + zipEntry.getName());
            new File(contentFile.getParent()).mkdirs();
            FileOutputStream outputStream = new FileOutputStream(contentFile);
            int bytesRead;
            byte buffer[] = new byte[256000];
            while((bytesRead = zipInputStream.read(buffer)) > 0){
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.close();
            zipEntry = zipInputStream.getNextEntry();
        }
        zipInputStream.closeEntry();
        zipInputStream.close();
        removeCompressedFile(compressedFile);
    }

    private void removeCompressedFile(File compressedFile){
        //Remove compressed file that was just decompressed
        compressedFile.delete();
    }

    private JSONObject getJSONRootFromFile(File parseFromFile){
        JSONTokener jsonParser;
        FileReader jsonFileReader;
        JSONObject jsonObjectRoot;
        try{
            jsonFileReader = new FileReader(parseFromFile);
            jsonParser = new JSONTokener(jsonFileReader);
            jsonObjectRoot = new JSONObject(jsonParser);
            jsonFileReader.close();
        } catch(IOException ioException){
            //"Do nothing, file is passed, this can not happen" - Joel, who knows damn well it just might happen
            return null;
        }
        return jsonObjectRoot;
    }

    private FDAFile getFileAttributes(HttpURLConnection httpConn){
        //Get the URL string back so it can be manipulated
        String fileUrlString = httpConn.getURL().toString();
        //Get file name from URL
        String fileName = fileUrlString.substring(fileUrlString.lastIndexOf("/") + 1, fileUrlString.length());
        //Get disposition from connection header file (if exists)
        String disposition = httpConn.getHeaderField("Content-Disposition");
        //Get content type from connection
        String contentType = httpConn.getContentType();
        //Get content length from connection
        int contentLength = httpConn.getContentLength();
        //Create preliminary file object
        return new FDAFile(fileName, disposition, contentType, contentLength);
    }

    private File createFetchDestination(FDAFile fetchingFile) throws IOException {
        //Create destination directory
        File fetchedDataDestinationDirectory = new File(localDataFile);
        fetchedDataDestinationDirectory.mkdirs();
        //Create destination file if necessary
        File fetchedDataDestinationFile = new File(fetchedDataDestinationDirectory.getPath() + "/" + fetchingFile.getFileName());
        fetchedDataDestinationFile.createNewFile();
        //Return the destination for the file that is fetched
        return fetchedDataDestinationFile;
    }

    private class FDAFile{
        private String fileName;
        private String disposition;
        private String contentType;
        private int contentLength;

        FDAFile(String fileName, String disposition, String contentType, int contentLength){
            this.fileName = fileName;
            this.disposition = disposition;
            this.contentType = contentType;
            this.contentLength = contentLength;
        }
        String getFileName(){
            return this.fileName;
        }

        String getDisposition(){
            return this.disposition;
        }

        String getContentType(){
            return this.contentType;
        }

        int getContentLength(){
            return this.contentLength;
        }
    }

    private class FDADeviceProperty{
        String keyName;
        Object value;
        String colName;

        //Constructor for when the property and the destination column name are not the same
        FDADeviceProperty(String keyName, Object value, String colName){
            this.keyName = keyName;
            this.value = value;
            this.colName = colName;
        }

        //Constructor for when the property and the destination column name are the same
        FDADeviceProperty(String keyName, Object value){
            this.keyName = keyName;
            this.value = value;
            this.colName = keyName;
        }

        String getColumnName(){
            return this.colName;
        }

        Object getValue(){
            return this.value;
        }
    }

    private void writeDevice(JSONObject readObject, DatabaseInteraction database){
        String fdaId = readObject.getJSONArray("identifiers").getJSONObject(0).getString("id");
        List<FDADeviceProperty> deviceProperties = getDeviceProperties(readObject);
        String deviceInsertSql = fdaDevicePropertyQueryBuilder("fda_data_devices", fdaId ,deviceProperties);
        PreparedStatement deviceInsertQuery = database.prepareStatement(deviceInsertSql);
        database.nonQuery(deviceInsertQuery);
        if(readObject.has("customer_contacts")){
            writeDeviceCustomerContacts(readObject, fdaId, database);
        }
        if(readObject.has("device_sizes")){
            writeDeviceSizes(readObject, fdaId, database);
        }
        if(readObject.has("gmdn_terms")){
            writeDeviceGmdnTerms(readObject, fdaId, database);
        }
        if(readObject.has("identifiers")){
            writeDeviceIdentifiers(readObject, fdaId, database);
        }
        if(readObject.has("premarket_submissions")){
            writeDevicePremarketSubmissions(readObject, fdaId, database);
        }
        if(readObject.has("product_codes")){
            writeDeviceProductCodes(readObject, fdaId, database);
        }
        if(readObject.has("storage")){
            writeDeviceStorage(readObject, fdaId, database);
        }
    }

    private void writeDeviceCustomerContacts(JSONObject readObject, String fdaId, DatabaseInteraction database){
        JSONArray customerContactsArray = readObject.getJSONArray("customer_contacts");
        for (int i = 0; i < customerContactsArray.length(); i++){
            JSONObject thisCustomerContact = customerContactsArray.getJSONObject(i);
            List<FDADeviceProperty> props = new ArrayList<>();
            if(thisCustomerContact.has("phone")){ props.add(new FDADeviceProperty("phone", thisCustomerContact.getString("phone"))); }
            if(thisCustomerContact.has("email")){ props.add(new FDADeviceProperty("email", thisCustomerContact.getString("email"))); }
            String thisCustomerContactInsertSql = fdaDevicePropertyQueryBuilder("fda_data_device_customer_contacts", fdaId, props);
            PreparedStatement customerContactInsertQuery = database.prepareStatement(thisCustomerContactInsertSql);
            database.nonQuery(customerContactInsertQuery);
        }
    }

    private void writeDeviceSizes(JSONObject readObject, String fdaId, DatabaseInteraction database){
        JSONArray deviceSizesArray = readObject.getJSONArray("device_sizes");
        for(int i = 0; i < deviceSizesArray.length(); i++){
            JSONObject thisDeviceSize = deviceSizesArray.getJSONObject(i);
            List<FDADeviceProperty> props = new ArrayList<>();
            if(thisDeviceSize.has("text")){ props.add(new FDADeviceProperty("text", thisDeviceSize.getString("text"), "size_text")); }
            if(thisDeviceSize.has("type")){ props.add(new FDADeviceProperty("type", thisDeviceSize.getString("type"), "size_type")); }
            if(thisDeviceSize.has("value")){ props.add(new FDADeviceProperty("value", thisDeviceSize.getString("value"), "size_value")); }
            if(thisDeviceSize.has("unit")) { props.add(new FDADeviceProperty("unit", thisDeviceSize.getString("unit"))); }
            String thisDeviceSizeInsertSql = fdaDevicePropertyQueryBuilder("fda_data_device_device_sizes", fdaId, props);
            PreparedStatement deviceSizeInsertQuery = database.prepareStatement(thisDeviceSizeInsertSql);
            database.nonQuery(deviceSizeInsertQuery);
        }
    }

    private void writeDeviceGmdnTerms(JSONObject readObject, String fdaId, DatabaseInteraction database){
        JSONArray deviceGmdnTermsArray = readObject.getJSONArray("gmdn_terms");
        for(int i = 0; i < deviceGmdnTermsArray.length(); i++){
            JSONObject thisGmdnTerm = deviceGmdnTermsArray.getJSONObject(i);
            List<FDADeviceProperty> props = new ArrayList<>();
            if(thisGmdnTerm.has("name")){ props.add(new FDADeviceProperty("name", thisGmdnTerm.getString("name"))); }
            if(thisGmdnTerm.has("definition")){ props.add(new FDADeviceProperty("definition", thisGmdnTerm.getString("definition"))); }
            String thisGmdnTermsInsertSql = fdaDevicePropertyQueryBuilder("fda_data_device_gmdn_terms", fdaId, props);
            PreparedStatement gmdnTermsInsertQuery = database.prepareStatement(thisGmdnTermsInsertSql);
            database.nonQuery(gmdnTermsInsertQuery);
        }
    }

    private void writeDeviceIdentifiers(JSONObject readObject, String fdaId, DatabaseInteraction database){
        JSONArray deviceIdentifiers = readObject.getJSONArray("identifiers");
        for(int i = 0; i <deviceIdentifiers.length(); i++){
            JSONObject thisDeviceIdentifier = deviceIdentifiers.getJSONObject(i);
            List<FDADeviceProperty> props = new ArrayList<>();
            if(thisDeviceIdentifier.has("id")){ props.add(new FDADeviceProperty("id", thisDeviceIdentifier.getString("id"), "identifier_id")); }
            if(thisDeviceIdentifier.has("issuing_agency")){ props.add(new FDADeviceProperty("issuing_agency", thisDeviceIdentifier.getString("issuing_agency"))); }
            if(thisDeviceIdentifier.has("package_discontinue_date")){ props.add(new FDADeviceProperty("package_discontinue_date", thisDeviceIdentifier.getString("package_discontinue_date"))); }
            if(thisDeviceIdentifier.has("package_status")){ props.add(new FDADeviceProperty("package_status", thisDeviceIdentifier.getString("package_status"))); }
            if(thisDeviceIdentifier.has("package_type")){ props.add(new FDADeviceProperty("package_type", thisDeviceIdentifier.getString("package_type"))); }
            if(thisDeviceIdentifier.has("quantity_per_package")){ props.add(new FDADeviceProperty("quantity_per_package", thisDeviceIdentifier.getInt("quantity_per_package"))); }
            if(thisDeviceIdentifier.has("type")){ props.add(new FDADeviceProperty("type", thisDeviceIdentifier.getString("type"), "identifier_type")); }
            if(thisDeviceIdentifier.has("unit_of_use_id")){ props.add(new FDADeviceProperty("unit_of_use_id", thisDeviceIdentifier.getString("unit_of_use_id"))); }
            String thisDeviceIdentifierInsertSql = fdaDevicePropertyQueryBuilder("fda_data_device_identifiers", fdaId, props);
            PreparedStatement deviceIdentifierQuery = database.prepareStatement(thisDeviceIdentifierInsertSql);
            database.nonQuery(deviceIdentifierQuery);
        }
    }

    private void writeDevicePremarketSubmissions(JSONObject readObject, String fdaId, DatabaseInteraction database){
        JSONArray devicePremarketSubmissions = readObject.getJSONArray("premarket_submissions");
        for(int i = 0; i < devicePremarketSubmissions.length(); i++){
            JSONObject thisPremarketSubmission = devicePremarketSubmissions.getJSONObject(i);
            List<FDADeviceProperty> props = new ArrayList<>();
            if(thisPremarketSubmission.has("submission_number")){ props.add(new FDADeviceProperty("submission_number", thisPremarketSubmission.getString("submission_number"))); }
            if(thisPremarketSubmission.has("supplement_number")){ props.add(new FDADeviceProperty("supplement_number", thisPremarketSubmission.getString("supplement_number"))); }
            if(thisPremarketSubmission.has("submission_type")){ props.add(new FDADeviceProperty("submission_type", thisPremarketSubmission.getString("submission_type"))); }
            String thisPremarketSubmissionSql = fdaDevicePropertyQueryBuilder("fda_data_device_premarket_submissions", fdaId, props);
            PreparedStatement premarketSubmissionQuery = database.prepareStatement(thisPremarketSubmissionSql);
            database.nonQuery(premarketSubmissionQuery);
        }
    }

    private void writeDeviceProductCodes(JSONObject readObject, String fdaId, DatabaseInteraction database){
        JSONArray deviceProductCodes = readObject.getJSONArray("product_codes");
        for(int i = 0; i < deviceProductCodes.length(); i++){
            JSONObject thisProductCode = deviceProductCodes.getJSONObject(i);
            List<FDADeviceProperty> props = new ArrayList<>();
            if(thisProductCode.has("code")){ props.add(new FDADeviceProperty("code", thisProductCode.getString("code"))); }
            if(thisProductCode.has("name")){ props.add(new FDADeviceProperty("name", thisProductCode.getString("name"))); }
            String thisProductCodeInsertSql = fdaDevicePropertyQueryBuilder("fda_data_device_product_codes", fdaId, props);
            PreparedStatement productCodeQuery = database.prepareStatement(thisProductCodeInsertSql);
            database.nonQuery(productCodeQuery);
        }
    }

    private void writeDeviceStorage(JSONObject readObject, String fdaId, DatabaseInteraction database){
        JSONArray deviceStorage = readObject.getJSONArray("storage");
        for(int i = 0; i < deviceStorage.length(); i++){
            JSONObject thisStorage = deviceStorage.getJSONObject(i);
            List<FDADeviceProperty> props = new ArrayList<>();
            if(thisStorage.has("high")){
                JSONObject storageHigh = thisStorage.getJSONObject("high");
                if(storageHigh.has("value")){ props.add(new FDADeviceProperty("value", storageHigh.getString("value"), "high_value")); }
                if(storageHigh.has("unit")){ props.add(new FDADeviceProperty("unit", storageHigh.getString("unit"), "high_unit")); }
            }
            if(thisStorage.has("low")){
                JSONObject storageLow = thisStorage.getJSONObject("low");
                if(storageLow.has("value")){ props.add(new FDADeviceProperty("value", storageLow.getString("value"), "low_value")); }
                if(storageLow.has("unit")){ props.add(new FDADeviceProperty("unit", storageLow.getString("unit"), "low_unit")); }
            }
            if(thisStorage.has("special_conditions")){ props.add(new FDADeviceProperty("special_conditions", thisStorage.getString("special_conditions"))); }
            if(thisStorage.has("type")){ props.add(new FDADeviceProperty("type", thisStorage.getString("type"), "storage_type")); }
            String thisStorageInsertSql = fdaDevicePropertyQueryBuilder("fda_data_device_storage", fdaId, props);
            PreparedStatement storageQuery = database.prepareStatement(thisStorageInsertSql);
            database.nonQuery(storageQuery);
        }
    }

    private String fdaDevicePropertyQueryBuilder(String tableName, String fdaId, List<FDADeviceProperty> props){
        String writeDeviceColumnsSql = "INSERT INTO " + tableName + "(fda_id";
        String writeDeviceValuesSql = ") VALUES ('" + fdaId + "'";
        for (FDADeviceProperty thisProperty : props) {
            writeDeviceColumnsSql += ", " + thisProperty.getColumnName();
            writeDeviceValuesSql += ", ";
            Object propertyValue = thisProperty.getValue();
            if(propertyValue.getClass() == String.class || propertyValue instanceof Date){
                String propValString = propertyValue.toString();
                propValString = propValString.replace("'", "''");
                writeDeviceValuesSql += "'" + propValString + "'";
            }
            else if(propertyValue.getClass() == Boolean.class){
                if((Boolean)propertyValue){
                    writeDeviceValuesSql += "1";
                } else {
                    writeDeviceValuesSql += "0";
                }
            } else if (propertyValue.getClass() == int.class || propertyValue.getClass() == Integer.class) {
                writeDeviceValuesSql += propertyValue;
            }
        }
        return writeDeviceColumnsSql + writeDeviceValuesSql + ")";
    }

    private List<FDADeviceProperty> getDeviceProperties(JSONObject readObject){
        List<FDADeviceProperty> props = new ArrayList<>();
        if(readObject.has("brand_name")){ props.add(new FDADeviceProperty("brand_name", readObject.getString("brand_name"))); }
        if(readObject.has("catalog_number")){ props.add(new FDADeviceProperty("catalog_number", readObject.getString("catalog_number"))); }
        if(readObject.has("commercial_distribution_end_date")){ props.add(new FDADeviceProperty("commercial_distribution_end_date", readObject.getString("commercial_distribution_end_date"))); }
        if(readObject.has("commercial_distribution_status")){ props.add(new FDADeviceProperty("commercial_distribution_status", readObject.getString("commercial_distribution_status"))); }
        if(readObject.has("company_name")){ props.add(new FDADeviceProperty("company_name", readObject.getString("company_name"))); }
        if(readObject.has("device_count_in_base_package")){ props.add(new FDADeviceProperty("device_count_in_base_package", readObject.getInt("device_count_in_base_package"))); }
        if(readObject.has("device_description")){ props.add(new FDADeviceProperty("device_description", readObject.getString("device_description"))); }
        if(readObject.has("has_donation_id_number")){ props.add(new FDADeviceProperty("has_donation_id_number", readObject.getBoolean("has_donation_id_number"))); }
        if(readObject.has("has_expiration_date")){ props.add(new FDADeviceProperty("has_expiration_date", readObject.getBoolean("has_expiration_date"))); }
        if(readObject.has("has_lot_or_batch_number")){ props.add(new FDADeviceProperty("has_lot_or_batch_number", readObject.getBoolean("has_lot_or_batch_number"))); }
        if(readObject.has("has_manufacturing_date")){ props.add(new FDADeviceProperty("has_manufacturing_date", readObject.getBoolean("has_manufacturing_date"))); }
        if(readObject.has("has_serial_number")){ props.add(new FDADeviceProperty("has_serial_number", readObject.getBoolean("has_serial_number"))); }
        if(readObject.has("is_combination_product")){ props.add(new FDADeviceProperty("is_combination_product", readObject.getBoolean("is_combination_product"))); }
        if(readObject.has("is_direct_marking_exempt")){ props.add(new FDADeviceProperty("is_direct_marking_exempt", readObject.getBoolean("is_direct_marking_exempt"))); }
        if(readObject.has("is_hct_p")){ props.add(new FDADeviceProperty("is_hct_p", readObject.getBoolean("is_hct_p"))); }
        if(readObject.has("is_kit")){ props.add(new FDADeviceProperty("is_kit", readObject.getBoolean("is_kit"))); }
        if(readObject.has("is_labeled_as_no_nrl")){ props.add(new FDADeviceProperty("is_labeled_as_no_nrl", readObject.getBoolean("is_labeled_as_no_nrl"))); }
        if(readObject.has("is_labeled_as_nrl")){ props.add(new FDADeviceProperty("is_labeled_as_nrl", readObject.getBoolean("is_labeled_as_nrl"))); }
        if(readObject.has("is_otc")){ props.add(new FDADeviceProperty("is_otc", readObject.getBoolean("is_otc"))); }
        if(readObject.has("is_pm_exempt")){ props.add(new FDADeviceProperty("is_pm_exempt", readObject.getBoolean("is_pm_exempt"))); }
        if(readObject.has("is_rx")){ props.add(new FDADeviceProperty("is_rx", readObject.getBoolean("is_rx"))); }
        if(readObject.has("is_single_use")){ props.add(new FDADeviceProperty("is_single_use", readObject.getBoolean("is_single_use"))); }
        if(readObject.has("labeler_duns_number")){ props.add(new FDADeviceProperty("labeler_duns_number", readObject.getString("labeler_duns_number"))); }
        if(readObject.has("mri_safety")){ props.add(new FDADeviceProperty("mri_safety", readObject.getString("mri_safety"))); }
        if(readObject.has("public_version_date")){ props.add(new FDADeviceProperty("public_version_date", readObject.getString("public_version_date"))); }
        if(readObject.has("public_version_number")){ props.add(new FDADeviceProperty("public_version_number", readObject.getString("public_version_number"))); }
        if(readObject.has("public_version_status")){ props.add(new FDADeviceProperty("public_version_status", readObject.getString("public_version_status"))); }
        if(readObject.has("publish_date")){ props.add(new FDADeviceProperty("publish_date", readObject.getString("publish_date"))); }
        if(readObject.has("record_key")){ props.add(new FDADeviceProperty("record_key", readObject.getString("record_key"))); }
        if(readObject.has("record_status")){ props.add(new FDADeviceProperty("record_status", readObject.getString("record_status"))); }
        if(readObject.has("is_sterile")){ props.add(new FDADeviceProperty("is_sterile", readObject.getBoolean("is_sterile"))); }
        if(readObject.has("is_sterilization_prior_use")){ props.add(new FDADeviceProperty("is_sterilization_prior_use", readObject.getBoolean("is_sterilization_prior_use"))); }
        if(readObject.has("sterilization_methods")){ props.add(new FDADeviceProperty("sterilization_methods", readObject.getString("sterilization_methods"))); }
        if(readObject.has("version_or_model_number")){ props.add(new FDADeviceProperty("version_or_model_number", readObject.getString("version_or_model_number"))); }
        if(readObject.has("product_codes")){
            JSONArray productCodes = readObject.getJSONArray("product_codes");
            if(productCodes.length() != 0) {
                if (productCodes.getJSONObject(0).has("openfda")) {
                    JSONObject openFda = productCodes.getJSONObject(0).getJSONObject("openfda");
                    if (openFda.has("device_class")) {
                        props.add(new FDADeviceProperty("device_class", openFda.getString("device_class")));
                    }
                    if (openFda.has("device_name")) {
                        props.add(new FDADeviceProperty("device_name", openFda.getString("device_name")));
                    }
                    if (openFda.has("fei_number")) {
                        props.add(new FDADeviceProperty("fei_number", openFda.getString("fei_number")));
                    }
                    if (openFda.has("medical_specialty_description")) {
                        props.add(new FDADeviceProperty("medical_specialty_description", openFda.getString("medical_specialty_description")));
                    }
                    if (openFda.has("regulation_number")) {
                        props.add(new FDADeviceProperty("regulation_number", openFda.getString("regulation_number")));
                    }
                }
            }
        }
        if(readObject.has("device_name")){ props.add(new FDADeviceProperty("device_name", readObject.getString("device_name"))); }
        if(readObject.has("fei_number")){ props.add(new FDADeviceProperty("fei_number", readObject.getString("fei_number"))); }
        if(readObject.has("medical_specialty_description")){ props.add(new FDADeviceProperty("medical_specialty_description", readObject.getString("medical_specialty_description"))); }
        if(readObject.has("regulation_number")){ props.add(new FDADeviceProperty("regulation_number", readObject.getString("regulation_number"))); }
        return props;
    }
}