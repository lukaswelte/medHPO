package Main.helper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseCleanup {
    public static void closeAll(ResultSet resultSet, Statement statement, Connection connection) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException ignored) {
            } // nothing we can do
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException ignored) {
            } // nothing we can do
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException ignored) {
            } // nothing we can do
        }
    }
}

