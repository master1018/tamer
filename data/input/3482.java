public class TestAllDevices {
    static boolean failed = false;
    public static void main(String[] args) throws Exception {
        out("default receiver:");
        try {
            Receiver recv = MidiSystem.getReceiver();
            out("  receiver: " + recv);
            if (recv instanceof MidiDeviceReceiver) {
                out("    OK");
            } else {
                out("    ERROR: not an instance of MidiDeviceReceiver");
                failed = true;
            }
        } catch (MidiUnavailableException ex) {
            out("  receiver: MidiUnavailableException (test NOT failed)");
        }
        out("default transmitter:");
        try {
            Transmitter tran = MidiSystem.getTransmitter();
            out("  transmitter: " + tran);
            if (tran instanceof MidiDeviceTransmitter) {
                out("    OK");
            } else {
                out("    ERROR: not an instance of MidiDeviceTransmitter");
                failed = true;
            }
        } catch (MidiUnavailableException ex) {
            out("  transmitter: MidiUnavailableException (test NOT failed)");
        }
        MidiDevice.Info[] infos = MidiSystem .getMidiDeviceInfo();
        for (MidiDevice.Info info: infos) {
            out(info.toString() + ":");
            try {
                MidiDevice dev = MidiSystem.getMidiDevice(info);
                dev.open();
                try {
                    Receiver recv = dev.getReceiver();
                    out("  receiver: " + recv);
                    if (recv instanceof MidiDeviceReceiver) {
                        MidiDeviceReceiver devRecv = (MidiDeviceReceiver)recv;
                        MidiDevice retDev = devRecv.getMidiDevice();
                        if (retDev == dev) {
                            out("    OK");
                        } else {
                            out("    ERROR: getMidiDevice returned incorrect device: " + retDev);
                            failed = true;
                        }
                    } else {
                        out("    ERROR: not an instance of MidiDeviceReceiver");
                        failed = true;
                    }
                } catch (MidiUnavailableException ex) {
                    out("  receiver: MidiUnavailableException (test NOT failed)");
                }
                try {
                    Transmitter tran = dev.getTransmitter();
                    out("  transmitter: " + tran);
                    if (tran instanceof MidiDeviceTransmitter) {
                        MidiDeviceTransmitter devTran = (MidiDeviceTransmitter)tran;
                        MidiDevice retDev = devTran.getMidiDevice();
                        if (retDev == dev) {
                            out("    OK");
                        } else {
                            out("    ERROR: getMidiDevice retur4ned incorrect device: " + retDev);
                            failed = true;
                        }
                    } else {
                        out("    ERROR: not an instance of MidiDeviceTransmitter");
                        failed = true;
                    }
                } catch (MidiUnavailableException ex) {
                    out("  transmitter: MidiUnavailableException (test NOT failed)");
                }
                dev.close();
            } catch (MidiUnavailableException ex) {
                out("  device: MidiUnavailableException (test NOT failed)");
            }
        }
        if (failed) {
            throw new Exception("Test failed.");
        }
    }
    static void out(String s) {
        System.out.println(s);
    }
}
