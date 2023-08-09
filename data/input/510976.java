abstract class ValueObject {
    abstract ComprehensionTlvTag getTag();
}
class CommandDetails extends ValueObject implements Parcelable {
    public boolean compRequired;
    public int commandNumber;
    public int typeOfCommand;
    public int commandQualifier;
    public ComprehensionTlvTag getTag() {
        return ComprehensionTlvTag.COMMAND_DETAILS;
    }
    CommandDetails() {
    }
    public boolean compareTo(CommandDetails other) {
        return (this.compRequired == other.compRequired &&
                this.commandNumber == other.commandNumber &&
                this.commandQualifier == other.commandQualifier &&
                this.typeOfCommand == other.typeOfCommand);
    }
    public CommandDetails(Parcel in) {
        compRequired = true;
        commandNumber = in.readInt();
        typeOfCommand = in.readInt();
        commandQualifier = in.readInt();
    }
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(commandNumber);
        dest.writeInt(typeOfCommand);
        dest.writeInt(commandQualifier);
    }
    public static final Parcelable.Creator<CommandDetails> CREATOR =
                                new Parcelable.Creator<CommandDetails>() {
        public CommandDetails createFromParcel(Parcel in) {
            return new CommandDetails(in);
        }
        public CommandDetails[] newArray(int size) {
            return new CommandDetails[size];
        }
    };
    public int describeContents() {
        return 0;
    }
}
class DeviceIdentities extends ValueObject {
    public int sourceId;
    public int destinationId;
    ComprehensionTlvTag getTag() {
        return ComprehensionTlvTag.DEVICE_IDENTITIES;
    }
}
class IconId extends ValueObject {
    int recordNumber;
    boolean selfExplanatory;
    ComprehensionTlvTag getTag() {
        return ComprehensionTlvTag.ICON_ID;
    }
}
class ItemsIconId extends ValueObject {
    int [] recordNumbers;
    boolean selfExplanatory;
    ComprehensionTlvTag getTag() {
        return ComprehensionTlvTag.ITEM_ICON_ID_LIST;
    }
}