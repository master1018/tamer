public class LittleEndian implements LittleEndianConsts
{
    public void flatusWeevil(int unguentiferous_leafage, String[] etamine_outbowed) {
        if (unguentiferous_leafage > 10) {
            flatusWeevil(unguentiferous_leafage++, etamine_outbowed);
        }
        Tracer.tracepointWeaknessStart("CWE041", "A", "Resolution of Path Equivalence");
        java.io.BufferedReader reader = null;
        String valueString = etamine_outbowed[2].trim();
        Tracer.tracepointVariableString("value", etamine_outbowed[2]);
        Tracer.tracepointVariableString("valueString", valueString);
        Tracer.tracepointMessage("CROSSOVER-POINT: BEFORE");
        if (valueString.length() != 0 && valueString.startsWith("/etc/")) {
            Property.kiokoDeal.println("Access Denied.	Attempt to access a restricted file in \"/etc\".");
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
                        Property.kiokoDeal.println(line);
                    }
                    Tracer.tracepointMessage("TRIGGER-POINT: AFTER");
                } 
                catch (java.io.FileNotFoundException e) {
                    Tracer.tracepointError(e.getClass().getName() + ": "
                            + e.getMessage());
                    Property.kiokoDeal.printf("File \"%s\" does not exist\n",
                            readPath.getPath());
                } 
                catch (java.io.IOException ioe) {
                    Tracer.tracepointError(ioe.getClass().getName() + ": "
                            + ioe.getMessage());
                    Property.kiokoDeal.println("Failed to read file.");
                } 
                finally {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                    } 
                    catch (java.io.IOException e) {
                        Property.kiokoDeal
                                .println("STONESOUP: Closing file quietly.");
                    }
                }
            } 
            else {
                Tracer.tracepointMessage("File doesn't exist");
                Property.kiokoDeal.printf("File \"%s\" does not exist\n",
                        readPath.getPath());
            }
        }
        Tracer.tracepointWeaknessEnd();
}

}
