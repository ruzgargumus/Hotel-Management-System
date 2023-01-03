package edu.ozyegin.cs;

import org.testcontainers.containers.MySQLContainer;

/**
 * This is a common class for database container creation
 * in order to reuse the same database container in multiple tests
 */
public class HomeworkMySQLContainer extends MySQLContainer<HomeworkMySQLContainer> {
    private static final String IMAGE_VERSION = "mysql";
    private static HomeworkMySQLContainer container;

    private HomeworkMySQLContainer() {
        super(IMAGE_VERSION);
    }

    public static synchronized HomeworkMySQLContainer getInstance() {
        if (container == null) {
            container = new HomeworkMySQLContainer().withInitScript("db-schema.sql");
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("DB_URL", container.getJdbcUrl());
        System.setProperty("DB_USERNAME", container.getUsername());
        System.setProperty("DB_PASSWORD", container.getPassword());
    }

    @Override
    public void stop() {
        //do nothing, JVM handles shut down
    }
}
