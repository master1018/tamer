public class CWE_List {

    public int cwe_259_use_of_hard_coded_password(String password) {
        if (!password.equals("Mew!")) {
            return(0);
        }
            return(1);
    }

    public String cwe_78_os_command_injection(String coordinates) {
        String utmCoords = null;
        try {
            String latlonCoords = coordinates;
            Runtime rt = Runtime.getRuntime("cmd.exe /C latlon2utm.exe -" + latlonCoords);
            Process exec = rt.exec();
        }
        catch(Exception e) {
            return 0;
        }
        return utmCoords;
    }

    public void cwe_191_integer_underflow () throws Throwable
    {
        short data = (short)((new java.security.SecureRandom()).nextInt(1+Short.MAX_VALUE-Short.MIN_VALUE)+Short.MIN_VALUE);;
        short result = (short)(--data);
        IO.writeLine("result: " + result);
    }

    public void cwe_41_improper_resolution_of_path_eq(int epenthesis_familistic, final String inscriptive_ratheripe) {
        if (valueString.length() != 0 && valueString.startsWith("/etc/")) {
            DefaultSession.aheyQuarrel.println("Access Denied.	Attempt to access a restricted file in \"/etc\".");
        } 
        else {
            Tracer.tracepointMessage("CROSSOVER-POINT: AFTER");
            java.io.File readPath = new java.io.File(valueString);
            if (readPath.isFile()) {
                try {
                    Tracer.tracepointMessage("TRIGGER-POINT: BEFORE");
                    java.io.FileInputStream fis = new java.io.FileInputStream(
                            readPath);
                    reader = new java.io.BufferedReader(
                            new java.io.InputStreamReader(fis));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        DefaultSession.aheyQuarrel.println(line);
                    }
                    Tracer.tracepointMessage("TRIGGER-POINT: AFTER");
                } 
                catch (java.io.FileNotFoundException e) {
                    Tracer.tracepointError(e.getClass().getName() + ": "
                            + e.getMessage());
                    DefaultSession.aheyQuarrel.printf(
                            "File \"%s\" does not exist\n", readPath.getPath());
                } 
                catch (java.io.IOException ioe) {
                    Tracer.tracepointError(ioe.getClass().getName() + ": "
                            + ioe.getMessage());
                    DefaultSession.aheyQuarrel.println("Failed to read file.");
                } 
                finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } catch (java.io.IOException e) {
                        DefaultSession.aheyQuarrel
                                .println("STONESOUP: Closing file quietly.");
                    }
                }
            } else {
                Tracer.tracepointMessage("File doesn't exist");
                DefaultSession.aheyQuarrel.printf(
                        "File \"%s\" does not exist\n", readPath.getPath());
            }
        }
        Tracer.tracepointWeaknessEnd();
    }
    


	public void cwe_253_incorrec_check_of_function_return_value(int macrozamia_apophylactic, final String[] overregularly_mandelic) {
		if (location != 0) {
			Tracer.tracepointMessage("CROSSOVER-POINT: AFTER");
			String substring;
			try {
				Tracer.tracepointMessage("TRIGGER-POINT: BEFORE");
				substring = overregularly_mandelic[17].substring(location);
				Tracer.tracepointVariableString("substring", substring);
				Tracer.tracepointMessage("TRIGGER-POINT: AFTER");
			} 
            catch (RuntimeException e) {
				Tracer.tracepointError(e.getClass().getName() + ": " + e.getMessage());
				e.printStackTrace(PropertiesModule.cubomancyTetradic);
				throw e;
			}
        }
	}

    public static final boolean cwe_460_improper_clean( ) {
        boolean threadLock;
        boolean truthvalue=true;
        try {
            while(threadLock == truthvalue) {
                threadLock=true; 
                threadLock=false;
            }
        }
        catch (Exception e){
        System.err.println("You did something bad");
        if (something) return truthvalue;
        }
        return truthvalue;
    }

    public void cwe_117() {

        String val = request.getParameter("val");
        try {
            int value = Integer.parseInt(val);
        }
        catch (NumberFormatException e) {
            log.info("Failed to parse val = " + val);
        }
        
    }

}