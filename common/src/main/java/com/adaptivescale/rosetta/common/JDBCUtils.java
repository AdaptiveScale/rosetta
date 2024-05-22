package com.adaptivescale.rosetta.common;

import com.adaptivescale.rosetta.common.models.input.Connection;
import lombok.extern.slf4j.Slf4j;

import java.util.Properties;

@Slf4j
public class JDBCUtils {
    public static final String USER_PROPERTY_NAME = "user";
    public static final String PASSWORD_PROPERTY_NAME = "password";


    private JDBCUtils() {
    }

    public static Properties setJDBCAuth(Connection connection) {
        Properties properties = new Properties();
        log.debug("Creating properties for JDBC authentication.");
        if (connection.getUserName() != null) {
            properties.setProperty(USER_PROPERTY_NAME, connection.getUserName());
        }
        if (connection.getPassword() != null) {
            properties.setProperty(PASSWORD_PROPERTY_NAME, connection.getPassword());
        }
        log.debug("Properties for JDBC authentication created successfully.");
        return properties;
    }
}

