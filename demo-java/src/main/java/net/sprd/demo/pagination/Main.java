package net.sprd.demo.pagination;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;

public class Main {
    private static String ADDRESS = "127.0.0.1";
    private static int PORT = 4567;

    static Logger LOG = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        Spark.ipAddress(ADDRESS);
        Spark.port(PORT);
        EntityResource resource = new EntityResource(DemoSetup.init(), ADDRESS, PORT);
        Spark.get("/", resource::handle);
    }
}
