package server_events;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
            fetchFDAFiles();
        }
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
}
