package net.sprd.demo.pagination;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import net.sprd.common.continuationtoken.ContinuationToken;
import net.sprd.common.continuationtoken.ContinuationTokenParser;
import net.sprd.common.continuationtoken.InvalidContinuationTokenException;
import net.sprd.common.continuationtoken.Page;
import net.sprd.common.continuationtoken.Pagination;
import net.sprd.common.continuationtoken.QueryAdvice;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import spark.Request;
import spark.Response;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static java.lang.String.format;
import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

public class EmployeeResource {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Gson gson;

    public EmployeeResource(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.gson = new Gson();
    }

    public Object handle(Request request, Response response) {
        try {
            ContinuationToken token = ContinuationTokenParser.toContinuationToken(request.queryParams("continue"));
            int pageSize = getPageSizeOrDefault(request);

            QueryAdvice queryAdvice = Pagination.calculateQueryAdvice(token, pageSize);
            List<Employee> entities = jdbcTemplate.query(createQuery(queryAdvice), this::mapRow);

            Page<Employee> page = Pagination.createPage(entities, token, pageSize);

            EmployeePage pageDto = new EmployeePage(page);
            response.type("application/json");
            response.body(gson.toJson(pageDto));
            return response.body();
        } catch (InvalidContinuationTokenException | DataAccessException e) {
            response.status(HTTP_BAD_REQUEST);
            return response.body();
        }
    }

    private String createQuery(QueryAdvice queryAdvice) {
        return format("SELECT * FROM Employees" +
                " WHERE UNIX_TIMESTAMP(timestamp) >= %d" +
                " ORDER BY timestamp ASC, id ASC" +
                " LIMIT %d", queryAdvice.getTimestamp(), queryAdvice.getLimit());
    }

    private int getPageSizeOrDefault(Request request) {
        if (!Strings.isNullOrEmpty(request.queryParams("pageSize"))) {
            return Integer.valueOf(request.queryParams("pageSize"));
        }
        return 10;
    }

    private Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Employee(rs.getInt("id"), rs.getString("name"), rs.getTimestamp("timestamp"));
    }
}
