public class AddressParcelHelper {
    private static Class[] sAddressClasses = new Class[] {
        ImpsUserAddress.class,
        ImpsGroupAddress.class,
        ImpsContactListAddress.class,
    };
    private AddressParcelHelper() {
    }
    public static Address readFromParcel(Parcel source) {
        int classIndex = source.readInt();
        if(classIndex == -1) {
            return null;
        }
        if(classIndex < 0 || classIndex >= sAddressClasses.length) {
            throw new RuntimeException("Unknown Address type index: " + classIndex);
        }
        try {
            Address address = (Address)sAddressClasses[classIndex].newInstance();
            address.readFromParcel(source);
            return address;
        } catch (InstantiationException e) {
            Log.e("AddressParcel", "Default constructor are required on Class"
                    + sAddressClasses[classIndex].getName());
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            Log.e("AddressParcel", "Default constructor are required on Class"
                    + sAddressClasses[classIndex].getName());
            throw new RuntimeException(e);
        }
    }
    public static void writeToParcel(Parcel dest, Address address) {
        if(address == null) {
            dest.writeInt(-1);
        } else {
            dest.writeInt(getClassIndex(address));
            address.writeToParcel(dest);
        }
    }
    private static int getClassIndex(Address address) {
        for(int i = 0; i < sAddressClasses.length; i++) {
            if(address.getClass() == sAddressClasses[i]) {
                return i;
            }
        }
        throw new RuntimeException("Unregistered Address type: " + address);
    }
}
