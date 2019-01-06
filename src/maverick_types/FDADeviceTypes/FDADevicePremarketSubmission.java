package maverick_types.FDADeviceTypes;

public class FDADevicePremarketSubmission {
    private String submissionNumber, supplementNumber, submissionType;
    public FDADevicePremarketSubmission(String submissionNumber, String supplementNumber, String submissionType){
        this.submissionNumber = submissionNumber;
        this.supplementNumber = supplementNumber;
        this.submissionType = submissionType;
    }

    /**
     * Getter for submission number property
     * @return submission number
     */
    public String getSubmissionNumber(){
        return submissionNumber;
    }

    /**
     * Getter for supplement number
     * @return supplement number
     */
    public String getSupplementNumber(){
        return supplementNumber;
    }

    /**
     * Getter for submission type
     * @return submission type
     */
    public String getSubmissionType(){
        return submissionType;
    }
}
