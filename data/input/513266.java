public class FieldTestFileGenerator {
    public static void main(String[] args) throws IOException {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        Support_GetPutFields toSerialize = new Support_GetPutFields();
        Support_GetPutFieldsDeprecated toSerializeDeprecated = 
                new Support_GetPutFieldsDeprecated();
        Support_GetPutFieldsDefaulted toSerializeDefaulted = 
                new Support_GetPutFieldsDefaulted();
        boolean success = true;
        toSerialize.initTestValues();
        toSerializeDeprecated.initTestValues();
        toSerializeDefaulted.initTestValues();
        System.out.println("Trying to write the test file 'testFields.ser'...");
        try {
            fos = new FileOutputStream("testFields.ser");
            oos = new ObjectOutputStream(fos);
            oos.writeObject(toSerialize);
            oos.close();
        }
        catch (Exception e) {
            System.out.println("Exception occured while writing the file: " + e);
            success = false;
        }
        finally {
            if (fos != null) fos.close();
        }
        System.out.println("Trying to write the test file 'testFieldsDeprecated.ser'...");
        try {
            fos = new FileOutputStream("testFieldsDeprecated.ser");
            oos = new ObjectOutputStream(fos);
            oos.writeObject(toSerializeDeprecated);
            oos.close();
        }
        catch (Exception e) {
            System.out.println("Exception occured while writing the file: " + e);
            success = false;
        }       
        finally {
            if (fos != null) fos.close();
        }
        System.out.println("Trying to write the test file 'testFieldsDefaulted.ser'...");
        try {
            fos = new FileOutputStream("testFieldsDefaulted.ser");
            oos = new ObjectOutputStream(fos);
            oos.writeObject(toSerializeDefaulted);
            oos.close();
        }
        catch (Exception e) {
            System.out.println("Exception occured while writing the file: " + e);
            success = false;
        }       
        finally {
            if (fos != null) fos.close();
        }
        if (success) { 
            System.out.println("Success!");
        } else {
            System.out.println("Failure!");
        }
    }
 }
