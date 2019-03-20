package manager_tests;

import managers.DeviceDataManager;
import maverick_data.DatabaseInteraction;
import maverick_types.DatabaseType;
import maverick_types.FDADeviceTypes.FDADevice;
import org.json.JSONObject;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class TestDeviceDataManager {
    private DeviceDataManager deviceDataManager = new DeviceDataManager(new DatabaseInteraction(DatabaseType.Devices));

    private static final String fdaId = "e124c470-a338-4033-b23c-48863b11367f";
    private static final int testFdaDevicePropertyCount = 42;
    private static final int testFdaDeviceCompPropCount = 5;
    private static final String testFdaDeviceDetailPropertyName = "id_num";
    private static final String testFdaDeviceDetailPropertyValue = "2384328";
    private static final String testFdaDeviceCompPropName = "GMDN Terms";
    private static final String testFdaDeviceGmdnTermValue = "Orthopaedic surgical procedure kit, non-medicated, single-use";

    @Test
    public void testGetDeviceByFdaId(){
        System.out.println("\nTesting get device by fda id");
        FDADevice thisDevice = deviceDataManager.getDeviceByFdaId(fdaId);
        assertNotNull(thisDevice);
        //Test property and composite property counts
        assertEquals(testFdaDevicePropertyCount, thisDevice.getPropertyCount());
        assertEquals(testFdaDeviceCompPropCount, thisDevice.getDeviceCompositeProperties().size());
        //Test first device detail property by index
        assertEquals(testFdaDeviceDetailPropertyName, thisDevice.getProperty(0).getPropertyName());
        assertEquals(testFdaDeviceDetailPropertyValue, thisDevice.getProperty(0).getPropertyValue().toString());
        //Test second device detail property by name
        assertEquals(testFdaDeviceDetailPropertyName, thisDevice.getProperty(testFdaDeviceDetailPropertyName).getPropertyName());
        assertEquals(testFdaDeviceDetailPropertyValue, thisDevice.getProperty(testFdaDeviceDetailPropertyName).getPropertyValue().toString());
        //Test first device composite property
        assertEquals(testFdaDeviceCompPropName, thisDevice.getDeviceCompositeProperties().get(0).getName());
        assertEquals(testFdaDeviceGmdnTermValue, thisDevice.getDeviceCompositeProperties().get(0).getProperties().get(0).getPropertyValue());
    }

    private static final String testCompanyCid = "BIOMET 3I, LLC";

    @Test
    public void testGetCompanyDevicesForImport(){
        System.out.println("\nTesting get company devices for import");
        List<FDADevice> testCompanyDevices = deviceDataManager.getCompanyDevicesForImport(testCompanyCid);
        assertNotNull(testCompanyDevices);
        assertEquals(1, testCompanyDevices.size());
    }

    private static final String testObjJson = "{\"has_donation_id_number\":\"0\",\"mri_safety\":\"Labeling does not contain MRI Safety Information\",\"record_status\":\"Published\",\"is_labeled_as_nrl\":\"0\",\"is_rx\":\"1\",\"commercial_distribution_status\":\"In Commercial Distribution\",\"device_description\":\"STERILE NELSON SHOULDER PACK\",\"fda_id\":\"e124c470-a338-4033-b23c-48863b11367f\",\"has_serial_number\":\"0\",\"public_version_date\":\"2018-07-19\",\"is_sterilization_prior_use\":\"0\",\"device_name\":\"Orthopedic Tray\",\"medical_specialty_description\":\"General, Plastic Surgery\",\"regulation_number\":\"878.4800\",\"is_direct_marking_exempt\":\"0\",\"is_labeled_as_no_nrl\":\"1\",\"labeler_duns_number\":\"961027315\",\"public_version_number\":\"3\",\"comp-props\":[{\"prop-name\":\"GMDN Terms\",\"props\":[{\"name\":\"Orthopaedic surgical procedure kit, non-medicated, single-use\"},{\"definition\":\"A collection of various sterile orthopaedic surgical instruments, dressings, and other materials intended to be used to perform an orthopaedic surgical procedure, however the kit is not dedicated to orthopaedic implantation. It does not contain pharmaceuticals. This is a single-use device.\"}]},{\"prop-name\":\"Identifiers\",\"props\":[{\"id\":\"50887488773542\"},{\"type\":\"Package\"},{\"issuing_agency\":\"GS1\"},{\"package_status\":\"In Commercial Distribution\"},{\"package_type\":\"CASE\"},{\"quantity_per_package\":\"2\"},{\"unit_of_use_id\":\"10887488773544\"}]},{\"prop-name\":\"Identifiers\",\"props\":[{\"id\":\"10887488773544\"},{\"type\":\"Primary\"},{\"issuing_agency\":\"GS1\"}]},{\"prop-name\":\"Product Codes\",\"props\":[{\"code\":\"OJH\"},{\"name\":\"Orthopedic tray\"}]},{\"prop-name\":\"Storage\",\"props\":[{\"high_value\":\"30\"},{\"high_unit\":\"Degrees Celsius\"},{\"low_value\":\"15\"},{\"low_unit\":\"Degrees Celsius\"},{\"type\":\"Storage Environment Temperature\"}]}],\"is_single_use\":\"1\",\"is_otc\":\"0\",\"version_or_model_number\":\"SOP41NSNU1\",\"has_manufacturing_date\":\"1\",\"brand_name\":\"CARDINAL HEALTH\",\"is_combination_product\":\"0\",\"is_kit\":\"1\",\"is_sterile\":\"1\",\"device_count_in_base_package\":1,\"is_pm_exempt\":\"1\",\"has_lot_or_batch_number\":\"1\",\"catalog_number\":\"SOP41NSNU1\",\"company_name\":\"Cardinal Health 200, LLC\",\"id_num\":2384328,\"has_expiration_date\":\"1\",\"public_version_status\":\"Update\",\"is_hct_p\":\"0\",\"device_class\":\"1\",\"publish_date\":\"2017-07-07\"}";

    @Test
    public void testSerializeToJson(){
        System.out.println("\nTesting serialize device to json");
        FDADevice device = deviceDataManager.getDeviceByFdaId(fdaId);
        JSONObject deviceJsonObj = deviceDataManager.serializeToJson(device);
        assertEquals(testObjJson, deviceJsonObj.toString());
    }
}
