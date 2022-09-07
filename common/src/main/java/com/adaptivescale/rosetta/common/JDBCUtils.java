package com.adaptivescale.rosetta.common;

import com.adaptivescale.rosetta.common.models.input.Connection;

import java.util.Properties;

public class JDBCUtils {
    public static final String USER_PROPERTY_NAME = "user";
    public static final String PASSWORD_PROPERTY_NAME = "password";


    private JDBCUtils() {
    }

    public static Properties setJDBCAuth(Connection connection) {
        Properties properties = new Properties();
        if (connection.getUserName() != null) {
            properties.setProperty(USER_PROPERTY_NAME, connection.getUserName());
        }
        if (connection.getPassword() != null) {
            properties.setProperty(PASSWORD_PROPERTY_NAME, connection.getPassword());
        }
        return properties;
    }
}

