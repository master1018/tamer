public class StatsStore {
    static final String sysVersion = "1.0";
    static Connection conn;
    static Statement stmt;
    static PreparedStatement insertStmt, selectByNameStmt, updateStmt;
    static PreparedStatement insertDetStmt, insertEventStmt;
    static PreparedStatement selectAllStmt;
    public static long now;
    static int compareDuration(long dur, long refDur) {
        long diff = dur - refDur;
        if (diff <= 0) {
            if ((double)-diff / refDur > 0.5) return 1; 
            else return 0; 
        }
        else if (diff < 20) return 0; 
        else if ((double)diff / refDur < 0.2) return 0; 
        else return -1; 
    }
    static void initStats(PerfStatCollector.Item a) {
        a.statMinDuration = a.duration;
        a.statMaxDuration = a.duration;
        a.statAvgDuration = a.duration;
        a.statCount = 1;
    }
    static void adjustStats(PerfStatCollector.Item a) {
        if (a.duration < a.statMinDuration) a.statMinDuration = a.duration;
        else
        if (a.duration > a.statMaxDuration) a.statMaxDuration = a.duration;
        a.statAvgDuration = ((a.statAvgDuration * a.statCount + a.duration) / (a.statCount + 1));
        a.statCount++;
    }
    static void adjustStatsOptimistic(PerfStatCollector.Item a) {
        adjustStats(a);
    }
    static void use1(PerfStatCollector.Item a) {
        Test test;
        int pos;
        PreparedStatement selectStmt = selectByNameStmt;
        try {
            try {
                insertStmt.setString(1, a.test.toString());
                insertStmt.execute();
            } catch (SQLException e) {}
            selectStmt.setString(1, a.test.toString());
            ResultSet row = selectStmt.executeQuery();
            row.first();
            pos = 1;
            a.id = row.getInt(pos); pos++;
            a.bestRes = row.getInt(pos); pos++;
            a.lastBestAt = row.getLong(pos); pos++;
            a.lastRes = row.getInt(pos); pos++;
            a.lastDuration = row.getLong(pos); pos++;
            a.statCount = row.getInt(pos); pos++;
            a.statAvgDuration = row.getDouble(pos); pos++;
            a.statMinDuration = row.getLong(pos); pos++;
            a.statMaxDuration = row.getLong(pos); pos++;
            if (a.res == 0) {
                if (a.bestRes == 100) {
                    a.bestRes = 0; a.lastBestAt = now;
                    a.histRelevance = 0; 
                    a.isTransition = false;
                    initStats(a);
                } else if (a.bestRes != 0) {
                    a.bestRes = 0; a.lastBestAt = now;
                    a.histRelevance = 4; 
                    a.isTransition = true; 
                    initStats(a);
                } else if (a.lastRes != 0) {
                    a.bestRes = 0; a.lastBestAt = now;
                    a.histRelevance = 3; 
                    a.isTransition = true; 
                    adjustStats(a);
                } else {
                    int cmp = compareDuration(a.duration, a.statMinDuration);
                    if (cmp >= 0) {
                        a.bestRes = 0; a.lastBestAt = now;
                        if (cmp > 0) {
                            a.histRelevance = 2; 
                            a.isTransition = true;
                            adjustStatsOptimistic(a);
                        } else if (compareDuration(a.duration, a.lastDuration) > 0) {
                            a.histRelevance = 1; 
                            a.isTransition = true;
                            adjustStatsOptimistic(a);
                        } else {
                            a.histRelevance = 0; 
                            a.isTransition = false; 
                            adjustStats(a);
                        }
                    } else {
                        if (compareDuration(a.duration, a.lastDuration) < 0) {
                            a.histRelevance = -2; 
                            a.isTransition = true;
                            adjustStats(a);
                        } else {
                            a.histRelevance = -2; 
                            a.isTransition = false; 
                            adjustStats(a);
                        }
                    }
                }
            } else if (a.bestRes == 0) {
                if (a.lastRes == 0) {
                    a.histRelevance = -4; 
                    a.isTransition = true;
                } else {
                    a.histRelevance = -4; 
                    a.isTransition = false; 
                }
            } else if (a.bestRes == 100) {
                a.bestRes = -3; 
                a.histRelevance = -3; 
                a.isTransition = true;
                initStats(a);
            } else {
                a.histRelevance = 0; 
                a.isTransition = false; 
                adjustStats(a);
            }
            pos = 1;
            updateStmt.setInt(pos, a.bestRes); pos++;
            updateStmt.setLong(pos, a.lastBestAt); pos++;
            updateStmt.setInt(pos, a.res); pos++;
            updateStmt.setLong(pos, a.duration); pos++;
            updateStmt.setInt(pos, a.statCount); pos++;
            updateStmt.setDouble(pos, a.statAvgDuration); pos++;
            updateStmt.setLong(pos, a.statMinDuration); pos++;
            updateStmt.setLong(pos, a.statMaxDuration); pos++;
            updateStmt.setInt(pos, a.id); pos++;
            updateStmt.execute();
            pos = 1;
            insertDetStmt.setInt(pos, a.id); pos++;
            insertDetStmt.setLong(pos, now); pos++;
            insertDetStmt.setInt(pos, a.statCount); pos++;
            insertDetStmt.setInt(pos, a.res); pos++;
            insertDetStmt.setLong(pos, a.duration); pos++;
            insertDetStmt.execute();
            if (a.isTransition) {
                pos = 1;
                insertEventStmt.setInt(pos, a.id); pos++;
                insertEventStmt.setLong(pos, now); pos++;
                insertEventStmt.setInt(pos, a.histRelevance); pos++;
                insertEventStmt.setInt(pos, a.res); pos++;
                insertEventStmt.setLong(pos, a.duration); pos++;
                insertEventStmt.execute();
            }
        }
        catch (SQLException e) {
            int x = 0;
        }
    }
    static void execOrIgnore(String sql) {
        try { stmt.execute(sql); }
        catch (SQLException e) {}
    }
    static void open(String jdbcDriver, String connectionURL)
    throws Exception {
            Class.forName(jdbcDriver).newInstance();
            conn = DriverManager.getConnection(connectionURL);
            stmt = conn.createStatement();
            String dbVersion;
            try {
                ResultSet res = stmt.executeQuery("SELECT id FROM Version");
                res.first();
                dbVersion = res.getString(1);
            }
            catch (SQLException e) {
                dbVersion = "";
            }
            if (!dbVersion.equals(sysVersion)) {
                execOrIgnore("DROP TABLE Test_Cases;");
                stmt.execute("CREATE TABLE Test_Cases (" +
                        "  id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "  name VARCHAR(255) UNIQUE, " +
                        "  best_Res INTEGER, last_Best_At INTEGER, " +
                        "  last_Res INTEGER, last_Duration INTEGER, " +
                        "  stat_Cnt INTEGER, stat_Avg NUMBER(20, 2), " +
                        "  stat_Min INTEGER, stat_Max INTEGER);");
                execOrIgnore("DROP TABLE Test_Case_Runs;");
                stmt.execute("CREATE TABLE Test_Case_Runs (" +
                        "  test_Id INTEGER, run_At INTEGER, " +
                        "  iteration INTEGER, res INTEGER, duration INTEGER, " +
                        "  PRIMARY KEY (test_Id, run_At));");
                execOrIgnore("DROP TABLE Test_Case_Events;");
                stmt.execute("CREATE TABLE Test_Case_Events (" +
                        "  test_Id INTEGER, run_At INTEGER, " +
                        "  relevance INTEGER, " +
                        "  res INTEGER, duration INTEGER, " +
                        "  PRIMARY KEY (test_Id, run_At));");
                execOrIgnore("DROP TABLE Version;");
                stmt.execute("CREATE TABLE Version(id VARCHAR(31));");
                stmt.execute("INSERT INTO Version (id) VALUES ('" + sysVersion + "');");
            }
            insertStmt = conn.prepareStatement("INSERT " +
                    "INTO Test_Cases (name, stat_Cnt) VALUES (?, 0);");
            selectByNameStmt = conn.prepareStatement("SELECT id, " +
                    "  IFNULL(best_Res, 100), IFNULL(last_Best_At, 0), " +
                    "  IFNULL(last_Res, 100), IFNULL(last_Duration, 0), " +
                    "  IFNULL(stat_Cnt, 0), IFNULL(stat_Avg, 0), " +
                    "  IFNULL(stat_Min, 0), IFNULL(stat_Max, 0) " +
                    "FROM Test_Cases WHERE name = ?;");
            updateStmt = conn.prepareStatement("UPDATE Test_Cases SET " +
                    "  best_Res = ?, last_Best_At = ?, " +
                    "  last_Res = ?, last_Duration = ?, " +
                    "  stat_Cnt = ?, stat_Avg = ?, " +
                    "  stat_Min = ?, stat_Max = ? " +
                    "WHERE id = ?;");
            insertDetStmt = conn.prepareStatement("INSERT " +
                    "INTO Test_Case_Runs (test_Id, run_At, iteration, res, duration) " +
                    "VALUES (?, ?, ?, ?, ?);");
            insertEventStmt = conn.prepareStatement("INSERT " +
                    "INTO Test_Case_Events (test_Id, run_At, relevance, res, duration) " +
                    "VALUES (?, ?, ?, ?, ?);");
            selectAllStmt = conn.prepareStatement("SELECT id, name, " +
                    "last_Res, stat_Cnt, " +
                    "last_Duration, stat_Avg, stat_Min, stat_Max " +
                    "FROM Test_Cases;");
            try {
                stmt.execute("PRAGMA SYNCHRONOUS = OFF;");
                stmt.execute("PRAGMA temp_store = MEMORY;");
            }
            catch (SQLException e) {
                dbVersion = "";
            }
            stmt.close();
            conn.commit();
    }
    static void close() {
        try {
            conn.commit();
            conn.close();
            conn = null;
        }
        catch (Exception e) {
            conn = null;
        }
    }
}
