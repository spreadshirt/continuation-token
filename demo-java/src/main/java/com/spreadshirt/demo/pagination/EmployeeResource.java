package com.spreadshirt.demo.pagination;

import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.spreadshirt.continuationtoken.ContinuationToken;
import com.spreadshirt.continuationtoken.ContinuationTokenParser;
import com.spreadshirt.continuationtoken.InvalidContinuationTokenException;
import com.spreadshirt.continuationtoken.Page;
import com.spreadshirt.continuationtoken.Pagination;
import com.spreadshirt.continuationtoken.QueryAdvice;
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
        ContinuationToken token = null;
        try {
            token = ContinuationTokenParser.toContinuationToken(request.queryParams("continuationToken"));
        } catch (InvalidContinuationTokenException e) {
            response.status(HTTP_BAD_REQUEST);
            return response.body();
        }
        int pageSize = getPageSizeOrDefault(request, 10);

        QueryAdvice queryAdvice = Pagination.calculateQueryAdvice(token, pageSize);
        List<Employee> entities = jdbcTemplate.query(createQuery(queryAdvice), this::mapRow);

        Page<Employee> page = Pagination.createPage(entities, token, pageSize);

        EmployeePage pageDto = new EmployeePage(page);
        response.type("application/json");
        response.body(gson.toJson(pageDto));
        return response.body();
    }

    private String createQuery(QueryAdvice queryAdvice) {
        return format("SELECT * FROM Employees" +
                " WHERE UNIX_TIMESTAMP(timestamp) >= %d" +
                " ORDER BY timestamp, id ASC" +
                " LIMIT %d", queryAdvice.getTimestamp(), queryAdvice.getLimit());
    }

    private int getPageSizeOrDefault(Request request, int defaultValue) {
        if (!Strings.isNullOrEmpty(request.queryParams("pageSize"))) {
            return Integer.valueOf(request.queryParams("pageSize"));
        }
        return defaultValue;
    }

    private Employee mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Employee(rs.getInt("id"), rs.getString("name"), rs.getTimestamp("timestamp"));
    }
}
