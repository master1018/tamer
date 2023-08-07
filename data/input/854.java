public class SysInfoEmail extends SysInfo {
    @Override
    public int service(Recordset inputParams) throws Throwable {
        super.service(inputParams);
        String enabled = getConfig().getConfigValue("
        if (enabled.equals("true")) {
            Recordset rs1 = getRecordset("dbpool");
            Recordset rs2 = getRecordset("threadpool");
            Recordset rs3 = getRecordset("webappsinfo");
            Recordset rs4 = getRecordset("serverinfo");
            Recordset rs5 = getRecordset("threaddump");
            String host = getConfig().getConfigValue("
            String subject = getConfig().getConfigValue("
            String from = getConfig().getConfigValue("
            String fromName = getConfig().getConfigValue("
            ;
            String to = getConfig().getConfigValue("
            String body = getResource("body.txt");
            TemplateEngine t = new TemplateEngine(getContext(), getRequest(), body);
            t.replaceDefaultValues();
            t.replaceLabels();
            t.replaceRequestAttributes();
            t.replace(rs1, "", "pool");
            t.replace(rs2, "", "thread");
            t.replace(rs3, "", "webapp");
            t.replace(rs5, "", "threaddump");
            t.replace(rs4, "");
            body = t.toString();
            SimpleMail s = new SimpleMail();
            s.send(host, from, fromName, to, subject, body);
        }
        return 0;
    }
}
