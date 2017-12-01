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

public class EntityResource implements RowMapper<Entity> {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final Gson gson;

    public EntityResource(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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
        List<Entity> entities = jdbcTemplate.query(createQuery(queryAdvice), this);

        EntityPage page = new EntityPage(Pagination.createPage(entities, token, pageSize));
        response.type("application/json");
        response.body(gson.toJson(page));
        return response.body();
    }

    private String createQuery(QueryAdvice queryAdvice) {
        return format("SELECT * FROM Entities" +
                " WHERE UNIX_TIMESTAMP(timestamp) >= %d" +
                " LIMIT %d", queryAdvice.getTimestamp(), queryAdvice.getLimit());
    }

    @Nullable
    @Override
    public Entity mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Entity(rs.getInt("id"), rs.getString("value"), rs.getTimestamp("timestamp"));
    }
}
