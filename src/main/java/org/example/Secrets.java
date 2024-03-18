package org.example;

public class Secrets {
    public static final String KAFKA_CLUSTER_KEY = System.getenv("CLUSTER_KEY");
    public static final String KAFKA_CLUSTER_SECRET = System.getenv("KAFKA_CLUSTER_SECRET");

    public static final String SCHEMA_REGISTRY_KEY = "REPLACE_WITH_SCHEMA_REGISTRY_KEY";
    public static final String SCHEMA_REGISTRY_SECRET = "REPLACE_WITH_SCHEMA_REGISTRY_SECRET";
    public static final String SERVER_URL = System.getenv("SERVER_URL");

}
