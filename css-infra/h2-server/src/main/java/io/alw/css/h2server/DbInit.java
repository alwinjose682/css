package io.alw.css.h2server;

import org.h2.jdbcx.JdbcConnectionPool;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DbInit {
    private record DbStructureDetail(String user, String password, String rootSqlFileName, String jdbcUrl) {
    }

    //    private final Logger log; // DO NOT want a static logger. This class will be created only once when DB objects are created
    private final String genericJdbcUrl;
    private final List<DbStructureDetail> dbStructureDetails;
    private final DbStructureDetail primaryDbUser;

    private final Path rootSqlScriptsPath;
    private final Path dbInitFileFlag;

    private final String SQLPLUS__PROMPT;
    private final String SQLPLUS__FILE_EXEC_CHAR;
    private final Predicate<String> promptLine;
    private final Predicate<String> elemsRequiredFromRootSqlFile;

    private final String[] stringsToRemove;

    public DbInit() {
//        this.log = Logger.getLogger("SqlFormatter");
        this.genericJdbcUrl = "jdbc:h2:~/h2_db_files/css;DB_CLOSE_ON_EXIT=FALSE;MODE=Oracle;DEFAULT_NULL_ORDERING=HIGH;IFEXISTS=FALSE";

        this.dbStructureDetails = new ArrayList<>();
        this.dbStructureDetails.add(new DbStructureDetail("css", "freepass", "create_objects__css.sql", this.genericJdbcUrl + ";SCHEMA=css"));
        this.dbStructureDetails.add(new DbStructureDetail("css_refdata", "freepass", "create_objects__css_refdata.sql", this.genericJdbcUrl + ";SCHEMA=css_refdata"));
        this.primaryDbUser = this.dbStructureDetails.get(0);

        final Path rootDataPath = Path.of(System.getProperty("user.home")).resolve("h2_db_files");
        this.rootSqlScriptsPath = Path.of(System.getenv("CSS_SQL_SCRIPTS"));
        this.dbInitFileFlag = rootDataPath.resolve("cssH2DbInit.flag");

        this.SQLPLUS__PROMPT = "prompt";
        this.SQLPLUS__FILE_EXEC_CHAR = "@";
        this.promptLine = line -> line.startsWith(SQLPLUS__PROMPT);
        this.elemsRequiredFromRootSqlFile = l -> {
            String line = l.toLowerCase();
            return promptLine.test(line) || line.startsWith(SQLPLUS__FILE_EXEC_CHAR);
        };

        this.stringsToRemove = new String[]{"tablespace &DATA_TS".toLowerCase(), "USING INDEX tablespace &INDEX_TS".toLowerCase(), "tablespace &INDEX_TS".toLowerCase()};
    }

    public void createDbAndDbObjects() throws SQLException, IOException {
        if (Files.exists(dbInitFileFlag)) {
            System.out.println("DB objects(users, tables, sequences...) were created in the first run");
            return;
        }

        // Create DB user and schema
        JdbcConnectionPool connPool = JdbcConnectionPool.create(genericJdbcUrl, primaryDbUser.user(), primaryDbUser.password());
        try {
            for (DbStructureDetail dbStructureDetail : dbStructureDetails) {
                try (Connection conn = connPool.getConnection();
                     Statement st = conn.createStatement()) {
                    String user = dbStructureDetail.user();
                    String password = dbStructureDetail.password();

                    if (!user.equalsIgnoreCase(primaryDbUser.user)) {
                        st.addBatch("CREATE USER " + user + " PASSWORD '" + password + "'");
                        st.addBatch("CREATE SCHEMA " + user + " AUTHORIZATION " + user);
                        st.addBatch("grant all privileges to " + user);
                        st.executeBatch();
                        System.out.println("Create user and schema: " + user);
                    } else {
                        st.executeUpdate("CREATE SCHEMA " + user + " AUTHORIZATION " + user);
                    }

                    conn.commit();
                }
            }
        } finally {
            connPool.dispose();
        }


        // Create DB objects by reading the oracle sqlplus file referred to by 'rootSqlFileName'
        for (DbStructureDetail dbStructureDetail : dbStructureDetails) {
            String user = dbStructureDetail.user();
            String password = dbStructureDetail.password();
            String rootSqlFileName = dbStructureDetail.rootSqlFileName();
            String jdbcUrl = dbStructureDetail.jdbcUrl();

            JdbcConnectionPool cp = null;
            try {
                cp = JdbcConnectionPool.create(jdbcUrl, user, password);
                // Create DB objects: tables, sequences etc
                try (Connection conn = cp.getConnection();
                     Statement st = conn.createStatement();
                     var lines = Files.lines(rootSqlScriptsPath.resolve(rootSqlFileName))
                ) {
//                    st.executeUpdate("SET SCHEMA " + user); // schema name is same as user
                    Iterator<String> it = lines.iterator();
                    while (it.hasNext()) {
                        String line = it.next();
                        if (elemsRequiredFromRootSqlFile.test(line)) {
                            actionOnRootSqlFileLine(line, st);
                        }
                    }

                    conn.commit();
                }
            } finally {
                if (cp != null) {
                    cp.dispose();
                }
            }
        }


        System.out.println("==================================================");
        System.out.println("Executed ALL sql statements. Following tables are created" + System.lineSeparator());
        System.out.println("tableName" + "                    " + "tableSchema");

        for (DbStructureDetail dbStructureDetail : dbStructureDetails) {
            String user = dbStructureDetail.user();
            String password = dbStructureDetail.password();
            String jdbcUrl = dbStructureDetail.jdbcUrl();

            JdbcConnectionPool cp = JdbcConnectionPool.create(jdbcUrl, user, password);
            try {
                try (Connection conn = cp.getConnection();
                     Statement st = conn.createStatement()) {


                    st.execute("show tables from " + user);
                    ResultSet resultSet = st.getResultSet();
                    while (resultSet.next()) {
                        String tableName = (String) resultSet.getObject(1);
                        String tableSchema = (String) resultSet.getObject(2);
                        System.out.println(tableName + "                    " + tableSchema);
                    }
                }
            } finally {
                cp.dispose();
            }
        }


        // After successfully creating the DB objects, create the flag file, so that DB objects will not be attempted to be created again
        Files.writeString(dbInitFileFlag, "Created H2 DB and DB Objects successfully. date_time: " + LocalDateTime.now(), StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW);
    }

    private void actionOnRootSqlFileLine(String line, Statement st) throws SQLException {
        if (promptLine.test(line)) {
            String promptMessage = line.substring(SQLPLUS__PROMPT.length());
//            System.out.println(promptMessage);
        } else {
            int idx = line.lastIndexOf(SQLPLUS__FILE_EXEC_CHAR);
            if (idx == -1 || idx == line.length()) {
                throw new RuntimeException("The line is not specified to be an executable file. line: " + line);
            }
            final String fileName = line.substring(idx + 1);
            Path executableSqlFilePath = rootSqlScriptsPath.resolve(Path.of(fileName.endsWith(".sql") ? fileName : fileName + ".sql"));
            String formattedSql = formatExecutableSqlFile(executableSqlFilePath);
            if (!formattedSql.isBlank()) {
                System.out.println(System.lineSeparator() + System.lineSeparator() + "************ Formatted SQL: ************");
                System.out.println(formattedSql);
//            st.executeBatch(formattedSqls)
                st.executeUpdate(formattedSql);
                System.out.println(System.lineSeparator() + "==> Executed the sql statement <==");
            }
        }
    }

    private String formatExecutableSqlFile(Path sqlFilePath) {
//        Predicate<String> sqlCommentsFilter = l -> !l.startsWith("--");

        try {
            return Files
                    .readAllLines(sqlFilePath)
                    .stream()
//                    .filter(sqlCommentsFilter) // Skip single line comments
                    .map(this::replaceStrings)
                    .collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String replaceStrings(final String origLine) {
        if (origLine == null || origLine.isBlank()) {
            return "";
        }

        var rline = origLine.toLowerCase();
        for (String str : stringsToRemove) {
            rline = rline.replace(str, "");
        }

        if (rline.isBlank()) {
            return "";
        }

        // 'rline' contains the chars that are required after skipping the removable chars, but all its chars are in lower case
        // So, take the unmodified chars from 'origLine' by skipping the removed chars
        StringBuilder sb = new StringBuilder();
        int r_idx = -1, o_idx = -1;
        char o_ch;
        char r_ch;

        rLineLoop:
        while (++r_idx < rline.length()) { // rline.length will never be greater than origLine.length
            r_ch = rline.charAt(r_idx);

            origLineLoop:
            while (++o_idx < origLine.length()) {
                o_ch = origLine.charAt(o_idx);
                if (r_ch == Character.toLowerCase(o_ch)) {
                    sb.append(o_ch);
                    break origLineLoop;
                }
            }

        }

        String formattedLine1 = sb.toString();

        // Handling special caseNo_1: amount NUMBER(* ,5)
        if (formattedLine1.contains("*")) {
            int flp3Idx;
            int idx = formattedLine1.toLowerCase().indexOf("NUMBER(*".toLowerCase());
            flp3Idx = "NUMBER(*".length();
            if (idx == -1) {
                idx = formattedLine1.toLowerCase().indexOf("NUMBER( *".toLowerCase());
                flp3Idx = "NUMBER( *".length();
            }
            if (idx != -1) {
                final String replaceWithString = "NUMBER(19";
                String flp1 = formattedLine1.substring(0, idx);
                String flp2 = replaceWithString;
                String flp3 = formattedLine1.substring(idx + flp3Idx);
                return flp1 + flp2 + flp3;
            }
        }

        return formattedLine1;
    }
}
