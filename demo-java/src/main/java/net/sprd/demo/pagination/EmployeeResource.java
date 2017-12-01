package net.sprd.demo.pagination;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import net.sprd.common.continuationtoken.ContinuationToken;
import net.sprd.common.continuationtoken.ContinuationTokenParser;
import net.sprd.common.continuationtoken.InvalidContinuationTokenException;
import net.sprd.common.continuationtoken.Pagination;
import net.sprd.common.continuationtoken.QueryAdvice;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.lang.Nullable;
import spark.Request;
import spark.Response;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static java.lang.String.format;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

public class EmployeeResource implements RowMapper<Employee> {
    private final String httpAddress;
    private final int httpPort;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Gson gson;

    public EmployeeResource(NamedParameterJdbcTemplate jdbcTemplate, String httpAddress, int port) {
        this.jdbcTemplate = jdbcTemplate;
        this.httpAddress = httpAddress;
        this.httpPort = port;
        this.gson = new Gson();
    }

    public Object handle(Request request, Response response) {
        ContinuationToken token = null;
        if (!Strings.isNullOrEmpty(request.queryParams("continue"))) {
            try {
                token = ContinuationTokenParser.toContinuationToken(request.queryParams("continue"));
            } catch (InvalidContinuationTokenException e) {
                response.status(HTTP_BAD_REQUEST);
                return response.body();
            }
        }
        int pageSize = 10;
        if (!Strings.isNullOrEmpty(request.queryParams("pageSize"))) {
            pageSize = Integer.valueOf(request.queryParams("pageSize"));
        }

        QueryAdvice queryAdvice = Pagination.calculateQueryAdvice(token, pageSize);
        List<Employee> entities = jdbcTemplate.query(createQuery(queryAdvice), this);

        EmployeePage page = new EmployeePage(Pagination.createPage(entities, token, pageSize), format("%s:%d", httpAddress, httpPort));
        response.type("application/json");
        response.body(gson.toJson(page));
        return response.body();
    }

    private String createQuery(QueryAdvice queryAdvice) {
        return format("SELECT * FROM Employees" +
                " WHERE UNIX_TIMESTAMP(timestamp) >= %d" +
                " LIMIT %d", queryAdvice.getTimestamp(), queryAdvice.getLimit());
    }

    @Nullable
    @Override
    public Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Employee(rs.getInt("id"), rs.getString("name"), rs.getTimestamp("timestamp"));
    }
}
