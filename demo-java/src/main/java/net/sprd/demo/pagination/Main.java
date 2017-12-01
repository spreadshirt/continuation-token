package net.sprd.demo.pagination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

public class Main {

    static Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        EmployeeResource resource = new EmployeeResource(DemoSetup.init());
        Spark.get("/", resource::handle);
    }
}
