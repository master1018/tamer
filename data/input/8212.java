class WindowsSystemEnvironment extends SystemEnvironment {
    WindowsSystemEnvironment() {
        super();
        getWmicResult("computersystem", "get", "model");
        setSystemModel(getWmicResult("computersystem", "get", "model"));
        setSystemManufacturer(getWmicResult("computersystem", "get", "manufacturer"));
        setSerialNumber(getWmicResult("bios", "get", "serialnumber"));
        String cpuMfr = getWmicResult("cpu", "get", "manufacturer");
        if (cpuMfr.length() == 0) {
            String procId = System.getenv("processor_identifer");
            if (procId != null) {
                String[] s = procId.split(",");
                cpuMfr = s[s.length - 1].trim();
            }
        }
        setCpuManufacturer(cpuMfr);
        try {
            File f = new File("TempWmicBatchFile.bat");
            if (f.exists()) {
                f.delete();
            }
        } catch (Exception e) {
        }
    }
    private String getWmicResult(String alias, String verb, String property) {
        String res = "";
        BufferedReader in = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("cmd", "/C", "WMIC", alias, verb, property);
            Process p = pb.start();
            BufferedWriter bw = null;
            try {
                bw = new BufferedWriter(
                         new OutputStreamWriter(p.getOutputStream()));
                bw.write(13);
                bw.flush();
            } finally {
                if (bw != null) {
                    bw.close();
                }
            }
            p.waitFor();
            if (p.exitValue() == 0) {
                in = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line = null;
                while ((line = in.readLine()) != null) {
                    line = line.trim();
                    if (line.length() == 0) {
                        continue;
                    }
                    res = line;
                }
                return res;
            }
        } catch (Exception e) {
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                }
            }
        }
        return res.trim();
    }
}
