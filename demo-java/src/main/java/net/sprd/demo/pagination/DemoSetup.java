package net.sprd.demo.pagination;

import com.github.javafaker.Faker;
import com.google.common.collect.ImmutableMap;
import org.hsqldb.jdbc.JDBCDataSource;
import org.hsqldb.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.String.format;

public class DemoSetup {
    static Logger LOG = LoggerFactory.getLogger(Main.class);
    static String ADDRESS = "localhost";
    static int PORT = 9001;
    private static Faker faker;

    static NamedParameterJdbcTemplate init() {
        faker = new Faker();
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
        String dropIfExistsSQL = "DROP TABLE Employees IF EXISTS";
        String createTableSQL = "CREATE TABLE Employees ( id INT PRIMARY KEY IDENTITY, name VARCHAR(128), timestamp TIMESTAMP WITH TIME ZONE )";
        String createIndexSQL = "CREATE INDEX Employees_id_index ON Employees (id)";
        Connection connection = dataSource.getConnection();
        connection.prepareStatement(dropIfExistsSQL).execute();
        connection.prepareStatement(createTableSQL).execute();
        connection.prepareStatement(createIndexSQL).execute();
        connection.commit();
    }

    private static void insertExampleData(NamedParameterJdbcTemplate jdbcTemplate) {
        List<Employee> entities = IntStream.range(1, 100).mapToObj(DemoSetup::createDemoEmployee)
                .collect(Collectors.toList());
        String insertSQL = "INSERT INTO Employees (id, name, timestamp) VALUES (:id, :name, :timestamp)";
        jdbcTemplate.batchUpdate(insertSQL, toArguments(entities));
    }

    private static Map<String, Object>[] toArguments(List<Employee> entities) {
        return entities.stream().map(DemoSetup::toArguments).toArray((IntFunction<Map<String, Object>[]>) Map[]::new);
    }

    private static Map<String, Object> toArguments(Employee entity) {
        return ImmutableMap.of(
                "id", entity.getId(),
                "name", entity.getName(),
                "timestamp", entity.getTimestamp2()
        );
    }

    private static Employee createDemoEmployee(int i) {
        Timestamp now = Timestamp.from(Instant.now().plusSeconds(i));
        return new Employee(i, faker.name().fullName(), now);
    }
}
