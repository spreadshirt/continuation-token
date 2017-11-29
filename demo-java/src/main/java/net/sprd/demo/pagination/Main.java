package net.sprd.demo.pagination;

import spark.Spark;

public class Main {
    public static void main(String[] args) {
        EntityResource resource = new EntityResource();

        Spark.get("/", resource::handle);
    }
}
