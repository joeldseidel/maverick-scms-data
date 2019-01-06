package maverick_types.FDADeviceTypes;

public class FDADeviceCustomerContact {
    private String email, phone, text;

    public FDADeviceCustomerContact(String email, String phone, String text){
        this.email = email;
        this.phone = phone;
        this.text = text;
    }

    public String getEmail(){
        return email;
    }

    public String getPhone(){
        return phone;
    }

    public String getText(){
        return text;
    }
}
