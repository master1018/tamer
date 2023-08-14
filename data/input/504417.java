public final class KeystoreHelper {
    public static boolean createNewStore(
            String osKeyStorePath,
            String storeType,
            String storePassword,
            String alias,
            String keyPassword,
            String description,
            int validityYears,
            IKeyGenOutput output)
            throws KeyStoreException, NoSuchAlgorithmException, CertificateException,
            UnrecoverableEntryException, IOException, KeytoolException {
        String os = System.getProperty("os.name");
        String keytoolCommand;
        if (os.startsWith("Windows")) {
            keytoolCommand = "keytool.exe";
        } else {
            keytoolCommand = "keytool";
        }
        String javaHome = System.getProperty("java.home");
        if (javaHome != null && javaHome.length() > 0) {
            keytoolCommand = javaHome + File.separator + "bin" + File.separator + keytoolCommand; 
        }
        ArrayList<String> commandList = new ArrayList<String>();
        commandList.add(keytoolCommand);
        commandList.add("-genkey");
        commandList.add("-alias");
        commandList.add(alias);
        commandList.add("-keyalg");
        commandList.add("RSA");
        commandList.add("-dname");
        commandList.add(description);
        commandList.add("-validity");
        commandList.add(Integer.toString(validityYears * 365));
        commandList.add("-keypass");
        commandList.add(keyPassword);
        commandList.add("-keystore");
        commandList.add(osKeyStorePath);
        commandList.add("-storepass");
        commandList.add(storePassword);
        if (storeType != null) {
            commandList.add("-storetype");
            commandList.add(storeType);
        }
        String[] commandArray = commandList.toArray(new String[commandList.size()]);
        int result = 0;
        try {
            result = grabProcessOutput(Runtime.getRuntime().exec(commandArray), output);
        } catch (Exception e) {
            StringBuilder builder = new StringBuilder();
            boolean firstArg = true;
            for (String arg : commandArray) {
                boolean hasSpace = arg.indexOf(' ') != -1;
                if (firstArg == true) {
                    firstArg = false;
                } else {
                    builder.append(' ');
                }
                if (hasSpace) {
                    builder.append('"');
                }
                builder.append(arg);
                if (hasSpace) {
                    builder.append('"');
                }
            }
            throw new KeytoolException("Failed to create key: " + e.getMessage(),
                    javaHome, builder.toString());
        }
        if (result != 0) {
            return false;
        }
        return true;
    }
    private static int grabProcessOutput(final Process process, final IKeyGenOutput output) {
        Thread t1 = new Thread("") {
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getErrorStream());
                BufferedReader errReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = errReader.readLine();
                        if (line != null) {
                            if (output != null) {
                                output.err(line);
                            } else {
                                System.err.println(line);
                            }
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        };
        Thread t2 = new Thread("") {
            @Override
            public void run() {
                InputStreamReader is = new InputStreamReader(process.getInputStream());
                BufferedReader outReader = new BufferedReader(is);
                try {
                    while (true) {
                        String line = outReader.readLine();
                        if (line != null) {
                            if (output != null) {
                                output.out(line);
                            } else {
                                System.out.println(line);
                            }
                        } else {
                            break;
                        }
                    }
                } catch (IOException e) {
                }
            }
        };
        t1.start();
        t2.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
        }
        try {
            t2.join();
        } catch (InterruptedException e) {
        }
        try {
            return process.waitFor();
        } catch (InterruptedException e) {
            return 0;
        }
    }
}
