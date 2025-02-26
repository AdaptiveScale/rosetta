package queryhelper.utils;

import com.adaptivescale.rosetta.common.DriverManagerDriverProvider;
import com.adaptivescale.rosetta.common.JDBCUtils;
import com.adaptivescale.rosetta.common.models.input.Connection;
import com.adataptivescale.rosetta.source.common.QueryHelper;

import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class QueryUtils {
    public static List<Map<String, Object>> executeQueryAndGetRecords(String query, Connection source, Integer showRowLimit) {
        try {
            DriverManagerDriverProvider driverManagerDriverProvider = new DriverManagerDriverProvider();
            Driver driver = driverManagerDriverProvider.getDriver(source);
            Properties properties = JDBCUtils.setJDBCAuth(source);
            java.sql.Connection jdbcConnection = driver.connect(source.getUrl(), properties);
            Statement statement = jdbcConnection.createStatement();
            statement.setMaxRows(showRowLimit);
            List<Map<String, Object>> select = QueryHelper.select(statement, query);
            return select;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
