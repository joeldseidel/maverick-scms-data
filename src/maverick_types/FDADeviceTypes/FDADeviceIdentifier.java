package maverick_types.FDADeviceTypes;

public class FDADeviceIdentifier {
    private String id, type, issuingAgency, packageDiscontinueDate, packageStatus, packageType, quantityPerPackage, unitOfUseId;
    public FDADeviceIdentifier(String id, String type, String issuingAgency, String packageDiscontinueDate, String packageStatus, String packageType, String quantityPerPackage, String unitOfUseId){
        this.id = id;
        this.type = type;
        this.issuingAgency = issuingAgency;
        this.packageDiscontinueDate = packageDiscontinueDate;
        this.packageStatus = packageStatus;
        this.packageType = packageType;
        this.quantityPerPackage = quantityPerPackage;
        this.unitOfUseId = unitOfUseId;
    }

    /**
     * Getter for id property
     * @return id property
     */
    public String getId(){
        return id;
    }

    /**
     * Getter for type property
     * @return type property
     */
    public String getType(){
        return type;
    }

    /**
     * Getter for issuing agency property
     * @return issuing agency property
     */
    public String getIssuingAgency(){
        return issuingAgency;
    }

    /**
     * Getter for package discontinue date property
     * @return package discontinue date
     */
    public String getPackageDiscontinueDate(){
        return packageDiscontinueDate;
    }

    /**
     * Getter for package status property
     * @return package status property
     */
    public String getPackageStatus(){
        return packageStatus;
    }

    /**
     * Getter for package type property
     * @return package type property
     */
    public String getPackageType(){
        return packageType;
    }

    /**
     * Getter for quantity per package property
     * @return quantity per package property
     */
    public String getQuantityPerPackage(){
        return quantityPerPackage;
    }

    /**
     * Getter for unit of use id property
     * @return unit of use property
     */
    public String getUnitOfUseId(){
        return unitOfUseId;
    }
}
