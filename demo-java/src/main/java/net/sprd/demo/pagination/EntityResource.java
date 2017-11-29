package net.sprd.demo.pagination;

import spark.Request;
import spark.Response;

public class EntityResource {
    public Object handle(Request request, Response response) {
        response.status(420);
        response.body("blaze it");
        return response.body();
    }
}
