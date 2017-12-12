package net.sprd.demo.pagination;

import com.google.common.base.Strings;
import com.google.common.collect.Streams;
import com.google.gson.Gson;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;

import java.util.stream.IntStream;

import static java.net.HttpURLConnection.HTTP_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EmployeeResourceTest {
    EmployeeResource resource;
    private Gson gson;

    @BeforeEach
    public void init() {
        resource = new EmployeeResource(DemoSetup.init());
        gson = new Gson();
    }

    @Test
    public void testHandle() {
        // fetch initial page
        Response response = RequestResponseFactory.create(new MockHttpServletResponse());
        resource.handle(createRequest(10, ""), response);

        assertEquals(HTTP_OK, response.status());
        assertNotNull(response.body());
        EmployeePage page = gson.fromJson(response.body(), EmployeePage.class);
        assertNotNull(page);
        assertTrue(page.hasNext());
        assertNotNull(page.getToken());
        Streams.forEachPair(
                IntStream.range(1, 10).boxed(),
                page.getEntities().stream(),
                (id, entity) -> assertEquals(id.intValue(), entity.getId())
        );

        // fetch second page
        response = RequestResponseFactory.create(new MockHttpServletResponse());
        resource.handle(createRequest(100, page.getToken()), response);

        assertEquals(HTTP_OK, response.status());
        assertNotNull(response.body());
        page = gson.fromJson(response.body(), EmployeePage.class);
        assertNotNull(page);
        assertFalse(page.hasNext());
        Streams.forEachPair(
                IntStream.range(11, 100).boxed(),
                page.getEntities().stream(),
                (id, entity) -> assertEquals(id.intValue(), entity.getId())
        );
    }

    private Request createRequest(int pageSize, String token) {
        MockHttpServletRequest servletRequest = new MockHttpServletRequest(null, "GET", "/");
        servletRequest.setParameter("pageSize", Integer.toString(pageSize));
        if (!Strings.isNullOrEmpty(token)) {
            servletRequest.setParameter("continue", token);
        }
        return RequestResponseFactory.create(servletRequest);
    }
}