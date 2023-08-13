public class MonkeyRunner {
  static String monkeyServer = "127.0.0.1";
  static int monkeyPort = 1080;
  static Socket monkeySocket = null;
  static IDevice monkeyDevice;
  static BufferedReader monkeyReader;
  static BufferedWriter monkeyWriter;
  static String monkeyResponse;
  static MonkeyRecorder monkeyRecorder;
  static String scriptName = null;
  private static Logger logger = Logger.getLogger("com.android.monkeyrunner");
  final static int KEY_INPUT_DELAY = 1000;
  final static String monkeyRunnerVersion = "0.4";
  public static void main(String[] args) throws IOException {
    logger.setLevel(Level.parse("WARNING"));  
    processOptions(args);
    logger.info("initAdb");
    initAdbConnection();
    logger.info("openMonkeyConnection");
    openMonkeyConnection();
    logger.info("start_script");
    start_script();
    logger.info("ScriptRunner.run");
    ScriptRunner.run(scriptName);
    logger.info("end_script");
    end_script();
    logger.info("closeMonkeyConnection");
    closeMonkeyConnection();  
  }
  public static void initAdbConnection() {
    String adbLocation = "adb";
    boolean device = false;
    boolean emulator = false;
    String serial = null;
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
      monkeyDevice = null;
      if (emulator || device) {
        for (IDevice d : devices) {
          if (d.isEmulator() == emulator) {
            if (monkeyDevice != null) {
              if (emulator) {
                printAndExit("Error: more than one emulator launched!",
                    true );
              } else {
                printAndExit("Error: more than one device connected!",true );
              }
            }
            monkeyDevice = d;
          }
        }
      } else if (serial != null) {
        for (IDevice d : devices) {
          if (serial.equals(d.getSerialNumber())) {
            monkeyDevice = d;
            break;
          }
        }
      } else {
        if (devices.length > 1) {
          printAndExit("Error: more than one emulator or device available!",
              true );
        }
        monkeyDevice = devices[0];
      }
      monkeyDevice.createForward(monkeyPort, monkeyPort);
      String command = "monkey --port " + monkeyPort;
      monkeyDevice.executeShellCommand(command, new NullOutputReceiver());
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
  public static void openMonkeyConnection() {
    try {
      InetAddress addr = InetAddress.getByName(monkeyServer);
      monkeySocket = new Socket(addr, monkeyPort);
      monkeyWriter = new BufferedWriter(new OutputStreamWriter(monkeySocket.getOutputStream()));
      monkeyReader = new BufferedReader(new InputStreamReader(monkeySocket.getInputStream()));
    } catch (UnknownHostException e) {
      e.printStackTrace();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
  public static void closeMonkeyConnection() {
    try {
      monkeyReader.close();
      monkeyWriter.close();
      monkeySocket.close();
      AndroidDebugBridge.terminate();
    } catch(IOException e) {
      e.printStackTrace();
    }
  }
  public static void start_script() throws IOException {
    press("menu", false);
    press("menu", false);
    press("home", false);
    monkeyRecorder = new MonkeyRecorder(scriptName, monkeyRunnerVersion);
    addDeviceVars();
    monkeyRecorder.addComment("Script commands");
  }
  public static void end_script() throws IOException {
    String command = "done";
    sendMonkeyEvent(command, false, false);
    monkeyRecorder.close();
  }
  public static void launch_activity(String name) throws IOException {
    System.out.println("Launching: " + name);
    recordCommand("Launching: " + name);
    monkeyDevice.executeShellCommand("am start -a android.intent.action.MAIN -n " 
        + name, new NullOutputReceiver());
    monkeyRecorder.endCommand();
   }
  public static void grabscreen(String tag) throws IOException {
    tag += ".png";
    try {
      Thread.sleep(1000);
      getDeviceImage(monkeyDevice, tag, false);
    } catch (InterruptedException e) {
    }
  }
  public static void sleep(int msec) throws IOException {
    try {
      recordCommand("sleep: " + msec);
      Thread.sleep(msec);
      recordResponse("OK");
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }
  public static boolean tap(int x, int y) throws IOException {
    String command = "tap " + x + " " + y;
    boolean result = sendMonkeyEvent(command);
    return result;
  }
  public static boolean press(String key) throws IOException {
    return press(key, true);
  }
  private static boolean press(String key, boolean print) throws IOException {
    String command = "press " + key;
    boolean result = sendMonkeyEvent(command, print, true);
    return result;
  }
  public static boolean down() throws IOException {
    return press("dpad_down");
  }
  public static boolean up() throws IOException {
    return press("dpad_up");
  }
  public static boolean type(String text) throws IOException {
    boolean result = false;
    String[] lines = text.split("[\\r\\n]+");
    for (String line: lines) {
      result = sendMonkeyEvent("type " + line + "\n");
    }
    return result;
  }
  public static boolean getvar(String name) throws IOException {
    return sendMonkeyEvent("getvar " + name + "\n");
  }
  public static boolean listvar() throws IOException {
    return sendMonkeyEvent("listvar \n");
  }
  private static boolean sendMonkeyEvent(String command) throws IOException {
    return sendMonkeyEvent(command, true, true);
  }
  private static boolean sendMonkeyEvent(String command, Boolean print, Boolean record) throws IOException {
    command = command.trim();
    if (print)
      System.out.println("MonkeyCommand: " + command);
    if (record)
      recordCommand(command);
    logger.info("Monkey Command: " + command + ".");
    monkeyWriter.write(command + "\n");
    monkeyWriter.flush();
    monkeyResponse = monkeyReader.readLine();
    if(monkeyResponse != null) {
      if (print)
        System.out.println("MonkeyServer: " + monkeyResponse);
      if (record)
        recordResponse(monkeyResponse);
      logger.info("Monkey Response: " + monkeyResponse + ".");
      if (monkeyResponse.startsWith("ERROR"))
        return false;
      if(monkeyResponse.startsWith("OK"))
        return true;
      return false;
    }
    if (print)
      System.out.println("MonkeyServer: ??no response");
    if (record)
      recordResponse("??no response");
    logger.info("Monkey Response: ??no response.");
    return false;
  }
  private static void recordCommand(String command) throws IOException {
    if (monkeyRecorder != null) {                       
      monkeyRecorder.startCommand();
      monkeyRecorder.addInput(command);
    }
  }
  private static void recordResponse(String response) throws IOException {
    recordResponse(response, "");
  } 
  private static void recordResponse(String response, String filename) throws IOException {
    if (monkeyRecorder != null) {                    
      monkeyRecorder.addResult(response, filename);  
      monkeyRecorder.endCommand();
    }
  }
  private static void addDeviceVars() throws IOException {
    monkeyRecorder.addComment("Device specific variables");
    sendMonkeyEvent("listvar \n", false, false);
    if (monkeyResponse.startsWith("OK:")) {
      String[] varNames = monkeyResponse.substring(3).split("\\s+");
      for (String name: varNames) {
        sendMonkeyEvent("getvar " + name, false, false);
        if(monkeyResponse != null) {
          if (monkeyResponse.startsWith("OK") ) {
            if (monkeyResponse.length() > 2) {
              monkeyRecorder.addDeviceVar(name, monkeyResponse.substring(3));
            } else { 
              monkeyRecorder.addDeviceVar(name, "null");
            }
          } else { 
            monkeyRecorder.addDeviceVar(name, monkeyResponse);
          }
        } else { 
          monkeyRecorder.addDeviceVar(name, "null");
        }
      }
    } else {
      monkeyRecorder.addAttribute("listvar", monkeyResponse);
    }
  }
  private static void processOptions(String[] args) {
    int index = 0;
    do {
      String argument = args[index++];
      if ("-s".equals(argument)) {
        if(index == args.length) {
          printUsageAndQuit("Missing Server after -s");
        }
        monkeyServer = args[index++];
      } else if ("-p".equals(argument)) {
        if (index == args.length) {
          printUsageAndQuit("Missing Server port after -p");
        }
        monkeyPort = Integer.parseInt(args[index++]);
      } else if ("-v".equals(argument)) {
        if (index == args.length) {
          printUsageAndQuit("Missing Log Level after -v");
        }
        Level level = Level.parse(args[index++]);
        logger.setLevel(level);
        level = logger.getLevel();
        System.out.println("Log level set to: " + level + "(" + level.intValue() + ").");
        System.out.println("Warning: Log levels below INFO(800) not working currently... parent issues");
      } else if (argument.startsWith("-")) {
        printUsageAndQuit("Unrecognized argument: " + argument + ".");
        monkeyPort = Integer.parseInt(args[index++]);
      } else {
        scriptName = argument;
      }
    } while (index < args.length);
  }
  private static void getDeviceImage(IDevice device, String filepath, boolean landscape)
  throws IOException {
    RawImage rawImage;
    recordCommand("grabscreen");
    System.out.println("Grabbing Screeshot: " + filepath + ".");
    try {
      rawImage = device.getScreenshot();
    }
    catch (IOException ioe) {
      recordResponse("No frame buffer", "");
      printAndExit("Unable to get frame buffer: " + ioe.getMessage(), true );
      return;
    }
    if (rawImage == null) {
      recordResponse("No image", "");
      return;
    }
    assert rawImage.bpp == 16;
    BufferedImage image;
    logger.info("Raw Image - height: " + rawImage.height + ", width: " + rawImage.width);
    if (landscape) {
      image = new BufferedImage(rawImage.height, rawImage.width,
          BufferedImage.TYPE_INT_ARGB);
      byte[] buffer = rawImage.data;
      int index = 0;
      for (int y = 0 ; y < rawImage.height ; y++) {
        for (int x = 0 ; x < rawImage.width ; x++) {
          int value = buffer[index++] & 0x00FF;
          value |= (buffer[index++] << 8) & 0x0FF00;
          int r = ((value >> 11) & 0x01F) << 3;
          int g = ((value >> 5) & 0x03F) << 2;
          int b = ((value >> 0) & 0x01F) << 3;
          value = 0xFF << 24 | r << 16 | g << 8 | b;
          image.setRGB(y, rawImage.width - x - 1, value);
        }
      }
    } else {
      image = new BufferedImage(rawImage.width, rawImage.height,
          BufferedImage.TYPE_INT_ARGB);
      byte[] buffer = rawImage.data;
      int index = 0;
      for (int y = 0 ; y < rawImage.height ; y++) {
        for (int x = 0 ; x < rawImage.width ; x++) {
          int value = buffer[index++] & 0x00FF;
          value |= (buffer[index++] << 8) & 0x0FF00;
          int r = ((value >> 11) & 0x01F) << 3;
          int g = ((value >> 5) & 0x03F) << 2;
          int b = ((value >> 0) & 0x01F) << 3;
          value = 0xFF << 24 | r << 16 | g << 8 | b;
          image.setRGB(x, y, value);
        }
      }
    }
    if (!ImageIO.write(image, "png", new File(filepath))) {
      recordResponse("No png writer", "");
      throw new IOException("Failed to find png writer");
    }
    recordResponse("OK", filepath);
  }
  private static void printUsageAndQuit(String message) {
    System.out.println(message);
    System.out.println("Usage: monkeyrunner [options] SCRIPT_FILE");
    System.out.println("");
    System.out.println("    -s      MonkeyServer IP Address.");
    System.out.println("    -p      MonkeyServer TCP Port.");
    System.out.println("    -v      MonkeyServer Logging level (ALL, FINEST, FINER, FINE, CONFIG, INFO, WARNING, SEVERE, OFF)");
    System.out.println("");
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
