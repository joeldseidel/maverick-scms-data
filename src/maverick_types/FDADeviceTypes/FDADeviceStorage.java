package maverick_types.FDADeviceTypes;

public class FDADeviceStorage {
    private String highValue, highUnit, lowValue, lowUnit, specialConditions, type;
    public FDADeviceStorage(String highValue, String highUnit, String lowValue, String lowUnit, String specialConditions, String type){
        this.highValue = highValue;
        this.highUnit = highUnit;
        this.lowValue = lowValue;
        this.lowUnit = lowUnit;
        this.specialConditions = specialConditions;
        this.type = type;
    }

    /**
     * Getter for high value property
     * @return high value
     */
    public String getHighValue(){
        return highValue;
    }

    /**
     * Getter for high unit property
     * @return high unit
     */
    public String getHighUnit(){
        return highUnit;
    }

    /**
     * Getter for low value property
     * @return low value property
     */
    public String getLowValue(){
        return lowValue;
    }

    /**
     * Getter for low unit property
     * @return low unit
     */
    public String getLowUnit(){
        return lowUnit;
    }

    /**
     * Getter for special conditions property
     * @return special conditions
     */
    public String getSpecialConditions(){
        return specialConditions;
    }

    /**
     * Getter for type property
     * @return type
     */
    public String getType(){
        return type;
    }
}
