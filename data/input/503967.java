public class Screenshot {
    public static void main(String[] args) {
        boolean device = false;
        boolean emulator = false;
        String serial = null;
        String filepath = null;
        boolean landscape = false;
        if (args.length == 0) {
            printUsageAndQuit();
        }
        int index = 0;
        do {
            String argument = args[index++];
            if ("-d".equals(argument)) {
                if (emulator || serial != null) {
                    printAndExit("-d conflicts with -e and -s", false );
                }
                device = true;
            } else if ("-e".equals(argument)) {
                if (device || serial != null) {
                    printAndExit("-e conflicts with -d and -s", false );
                }
                emulator = true;
            } else if ("-s".equals(argument)) {
                if (index == args.length) {
                    printAndExit("Missing serial number after -s", false );
                }
                if (device || emulator) {
                    printAndExit("-s conflicts with -d and -e", false );
                }
                serial = args[index++];
            } else if ("-l".equals(argument)) {
                landscape = true;
            } else {
                filepath = argument;
                if (index < args.length) {
                    printAndExit("Too many arguments!", false );
                }
            }
        } while (index < args.length);
        if (filepath == null) {
            printUsageAndQuit();
        }
        Log.setLogOutput(new ILogOutput() {
            public void printAndPromptLog(LogLevel logLevel, String tag, String message) {
                System.err.println(logLevel.getStringValue() + ":" + tag + ":" + message);
            }
            public void printLog(LogLevel logLevel, String tag, String message) {
                System.err.println(logLevel.getStringValue() + ":" + tag + ":" + message);
            }
        });
        String adbLocation = System.getProperty("com.android.screenshot.bindir"); 
        if (adbLocation != null && adbLocation.length() != 0) {
            adbLocation += File.separator + "adb"; 
        } else {
            adbLocation = "adb"; 
        }
        AndroidDebugBridge.init(false );
        try {
            AndroidDebugBridge bridge = AndroidDebugBridge.createBridge(
                    adbLocation, true );
            int count = 0;
            while (bridge.hasInitialDeviceList() == false) {
                try {
                    Thread.sleep(100);
                    count++;
                } catch (InterruptedException e) {
                }
                if (count > 100) {
                    System.err.println("Timeout getting device list!");
                    return;
                }
            }
            IDevice[] devices = bridge.getDevices();
            if (devices.length == 0) {
                printAndExit("No devices found!", true );
            }
            IDevice target = null;
            if (emulator || device) {
                for (IDevice d : devices) {
                    if (d.isEmulator() == emulator) {
                        if (target != null) {
                            if (emulator) {
                                printAndExit("Error: more than one emulator launched!",
                                        true );
                            } else {
                                printAndExit("Error: more than one device connected!",true );
                            }
                        }
                        target = d;
                    }
                }
            } else if (serial != null) {
                for (IDevice d : devices) {
                    if (serial.equals(d.getSerialNumber())) {
                        target = d;
                        break;
                    }
                }
            } else {
                if (devices.length > 1) {
                    printAndExit("Error: more than one emulator or device available!",
                            true );
                }
                target = devices[0];
            }
            if (target != null) {
                try {
                    System.out.println("Taking screenshot from: " + target.getSerialNumber());
                    getDeviceImage(target, filepath, landscape);
                    System.out.println("Success.");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                printAndExit("Could not find matching device/emulator.", true );
            }
        } finally {
            AndroidDebugBridge.terminate();
        }
    }
    private static void getDeviceImage(IDevice device, String filepath, boolean landscape)
            throws IOException {
        RawImage rawImage;
        try {
            rawImage = device.getScreenshot();
        }
        catch (IOException ioe) {
            printAndExit("Unable to get frame buffer: " + ioe.getMessage(), true );
            return;
        }
        if (rawImage == null)
            return;
        if (landscape) {
            rawImage = rawImage.getRotated();
        }
        BufferedImage image = new BufferedImage(rawImage.width, rawImage.height,
                BufferedImage.TYPE_INT_ARGB);
        int index = 0;
        int IndexInc = rawImage.bpp >> 3;
        for (int y = 0 ; y < rawImage.height ; y++) {
            for (int x = 0 ; x < rawImage.width ; x++) {
                int value = rawImage.getARGB(index);
                index += IndexInc;
                image.setRGB(x, y, value);
            }
        }
        if (!ImageIO.write(image, "png", new File(filepath))) {
            throw new IOException("Failed to find png writer");
        }
    }
    private static void printUsageAndQuit() {
        System.out.println("Usage: screenshot2 [-d | -e | -s SERIAL] [-l] OUT_FILE");
        System.out.println("");
        System.out.println("    -d      Uses the first device found.");
        System.out.println("    -e      Uses the first emulator found.");
        System.out.println("    -s      Targets the device by serial number.");
        System.out.println("");
        System.out.println("    -l      Rotate images for landscape mode.");
        System.out.println("");
        System.exit(1);
    }
    private static void printAndExit(String message, boolean terminate) {
        System.out.println(message);
        if (terminate) {
            AndroidDebugBridge.terminate();
        }
        System.exit(1);
    }
}
