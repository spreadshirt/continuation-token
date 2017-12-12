package com.spreadshirt.demo.pagination;

import spark.Spark;

public class Main {
    public static void main(String[] args) {
        EmployeeResource resource = new EmployeeResource(DemoSetup.init());
        Spark.get("/", resource::handle);
    }
}
