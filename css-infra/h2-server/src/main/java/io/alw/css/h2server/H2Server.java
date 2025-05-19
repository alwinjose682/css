package io.alw.css.h2server;

import org.h2.tools.Server;

import java.io.IOException;
import java.sql.SQLException;

/// Reference:
/// - https://www.h2database.com/html/tutorial.html
/// - https://www.h2database.com/html/features.html
///
/// jdbc URL:
/// - Server:
/// jdbc:h2:~/h2_db_files/css;DB_CLOSE_ON_EXIT=FALSE;MODE=Oracle;DEFAULT_NULL_ORDERING=HIGH;IFEXISTS=FALSE
/// - Client:
/// jdbc:h2:tcp://localhost:1531/~/h2_db_files/css;DB_CLOSE_ON_EXIT=FALSE;MODE=Oracle;DEFAULT_NULL_ORDERING=HIGH;IFEXISTS=TRUE;SCHEMA=<schema_name>
public class H2Server {
    private Server server;

    public static void main(String[] args) {
        new H2Server().start();
    }

    private void start() {
        try {
            String allowClientConnections = "-tcpAllowOthers";
            String[] virtualThreads = new String[]{"-tcpVirtualThreads", "true"};
            String[] port = new String[]{"-tcpPort", "1531"};
//            String[] baseDir = new String[]{"-baseDir","<...>"};
            server = Server.createTcpServer(allowClientConnections, virtualThreads[0], virtualThreads[1], port[0], port[1]).start();
            System.out.println("Started H2 DB server on port: " + port[1]);
            new DbInit().createDbAndDbObjects();
        } catch (SQLException e) {
            System.out.println("Unable to start H2 DB or Exception in creating DB objects");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

//        String url = "jdbc:h2:" + dir + "/" + dbName;
//        String file = "data/test.sql";
//        Script.execute(url, user, password, file);

    }

    private void stop() {
        server.stop();
    }
}
