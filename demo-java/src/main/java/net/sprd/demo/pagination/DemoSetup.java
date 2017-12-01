package net.sprd.demo.pagination;

import org.hsqldb.jdbc.JDBCDataSource;
import org.hsqldb.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

public class DemoSetup {
    static Logger LOG = LoggerFactory.getLogger(Main.class);
    static String ADDRESS = "localhost";
    static int PORT = 9001;

    static NamedParameterJdbcTemplate init() {
        Server server = new Server();
        server.setAddress(ADDRESS);
        server.setPort(PORT);

        server.setDatabaseName(0, "pagination-demo-java");
        server.setDatabasePath(0, "pagination-demo-java");
        server.start();

        NamedParameterJdbcTemplate jdbcTemplate = null;
        try {
            DataSource dataSource = setupDataSource(server.getAddress(), server.getPort());
            createTable(dataSource);
            jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
            insertExampleData(jdbcTemplate);
        } catch (SQLException e) {
            LOG.error(e.getMessage());
            System.exit(1);
        }

        return jdbcTemplate;
    }

    private static DataSource setupDataSource(String address, int port) {
        JDBCDataSource jdbcDataSource = new JDBCDataSource();
        String dbURL = format("jdbc:hsqldb:hsql://%s:%d/pagination-demo-java", address, port);
        jdbcDataSource.setURL(dbURL);
        return jdbcDataSource;
    }

    private static void createTable(DataSource dataSource) throws SQLException {
        String dropIfExistsSQL = "DROP TABLE Entities IF EXISTS";
        String createTableSQL = "CREATE TABLE Entities ( id INT PRIMARY KEY IDENTITY, value VARCHAR(128), timestamp TIMESTAMP WITH TIME ZONE )";
        String createIndexSQL = "CREATE INDEX Entities_id_index ON Entities (id)";
        Connection connection = dataSource.getConnection();
        connection.prepareStatement(dropIfExistsSQL).execute();
        connection.prepareStatement(createTableSQL).execute();
        connection.prepareStatement(createIndexSQL).execute();
        connection.commit();
    }

    private static void insertExampleData(NamedParameterJdbcTemplate jdbcTemplate) {
        List<SqlParameterSource> entities = IntStream.range(1, 100).mapToObj(DemoSetup::createDemoEntity).collect(Collectors.toList());
        String insertSQL = "INSERT INTO Entities (id, value, timestamp) VALUES (:id, :value, :timestamp)";
        jdbcTemplate.batchUpdate(insertSQL, entities.toArray(new SqlParameterSource[]{}));
    }

    private static Entity createDemoEntity(int i) {
        Timestamp now = Timestamp.from(Instant.now().plusSeconds(i));
        int randomValue = i * i;
        return new Entity(i, format("some-random-value=%d", randomValue), now);
    }
}
