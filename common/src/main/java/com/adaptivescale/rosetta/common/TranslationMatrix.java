package com.adaptivescale.rosetta.common;

//import com.adaptivescale.rosetta.translator.model.TranslateInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.sql.*;

@Slf4j
public class TranslationMatrix {
    static String URL = "jdbc:h2:mem:translation;DB_CLOSE_DELAY=-1";
    static String TABLE_NAME = "TRANSLATION";
    private static TranslationMatrix instance = null;

    public TranslationMatrix() {
        try {
            initTables();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized TranslationMatrix getInstance() {
        if(instance == null) {
            instance = new TranslationMatrix();
        }
        return instance;
    }

    void initTables() throws IOException {
        String translationTable = "CREATE TABLE "+TABLE_NAME+"(id INT PRIMARY KEY AUTO_INCREMENT, " +
                "source_type VARCHAR(255), " +
                "source_column_type VARCHAR(255), " +
                "target_type VARCHAR(255), " +
                "target_column_type VARCHAR(255)" +
                ");";
        execute(translationTable);
        loadCSVData();
    }

    void loadCSVData() throws IOException {
        String line = "";
        String splitBy = ";;";
        //parsing a CSV file into BufferedReader class constructor
        BufferedReader br = new BufferedReader(new FileReader("translation.csv"));
        StringBuilder dataInsertQuery = new StringBuilder();
        while ((line = br.readLine()) != null)   //returns a Boolean value
        {
            String[] translation = line.split(splitBy);    // use comma as separator
            TranslationModel translationModel = new TranslationModel();
            translationModel.setId(Integer.valueOf(translation[0]));
            translationModel.setSourceType(translation[1]);
            translationModel.setSourceColumnType(translation[2]);
            translationModel.setTargetType(translation[3]);
            translationModel.setTargetColumnType(translation[4]);
//            System.out.println(translationModel);
            String insertStatement = translationModel.generateInsertStatement(TABLE_NAME);
            dataInsertQuery.append(insertStatement);
//            System.out.println("Employee [First Name=" + employee[0] + ", Last Name=" + employee[1] + ", Designation=" + employee[2] + ", Contact=" + employee[3] + ", Salary= " + employee[4] +"]");
        }
        execute(dataInsertQuery.toString());
//    Scanner sc = new Scanner(new File("translation.csv"));
//        sc.useDelimiter(",");   //sets the delimiter pattern
//        while (sc.hasNext())  //returns a boolean value
//        {
//            System.out.print(sc.next());  //find and returns the next complete token from this scanner
//        }
//        sc.close();  //closes the scanner
    }

    void execute(String sql) {
//        System.out.printf("Executing sql:%s\n",sql);
        try {
            Connection connection = DriverManager.getConnection(URL);
            Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    void executeQuery(String query) throws SQLException {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(URL);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);
            connection.close();

//            if (resultSet.next()) {
//
//                System.out.println(resultSet.getInt(1));
//            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            if(connection!=null) {
                connection.close();
            }
        }
    }

    TranslationModel getSingleRecord(String query) {
        try {
            Connection connection = DriverManager.getConnection(URL);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            if (resultSet.next()) {
                TranslationModel translationModel = new TranslationModel();
                translationModel.setId(resultSet.getInt(1));
                translationModel.setSourceType(resultSet.getString(2));
                translationModel.setSourceColumnType(resultSet.getString(3));
                translationModel.setTargetType(resultSet.getString(4));
                translationModel.setTargetColumnType(resultSet.getString(5));
                return translationModel;
            }
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public TranslationModel get(Integer id) {
        String query = String.format("SELECT TOP 1 * from %s where id=%s", TABLE_NAME, id);
        return getSingleRecord(query);
    }

    public TranslationModel get(String sourceType, String sourceColumnType) {
        String query = String.format("SELECT * from %s where source_type='%s' and source_column_type='%s'", TABLE_NAME, sourceType, sourceColumnType);
        return getSingleRecord(query);
    }

    public TranslationModel get(String sourceType, String sourceColumnType, String targetType) {
        String query = String.format("SELECT * from %s where source_type='%s' and source_column_type='%s' and target_type='%s'",
                TABLE_NAME, sourceType, sourceColumnType, targetType);
        return getSingleRecord(query);
    }




    public class TranslationModel {
        private Integer id;
        private String sourceType;
        private String sourceColumnType;
        private String targetType;
        private String targetColumnType;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getSourceType() {
            return sourceType;
        }

        public void setSourceType(String sourceType) {
            this.sourceType = sourceType;
        }

        public String getSourceColumnType() {
            return sourceColumnType;
        }

        public void setSourceColumnType(String sourceColumnType) {
            this.sourceColumnType = sourceColumnType;
        }

        public String getTargetType() {
            return targetType;
        }

        public void setTargetType(String targetType) {
            this.targetType = targetType;
        }

        public String getTargetColumnType() {
            return targetColumnType;
        }

        public void setTargetColumnType(String targetColumnType) {
            this.targetColumnType = targetColumnType;
        }

        @Override
        public String toString() {
            return "TranslationModel{" +
                    "id=" + id +
                    ", sourceType='" + sourceType + '\'' +
                    ", sourceColumnType='" + sourceColumnType + '\'' +
                    ", targetType='" + targetType + '\'' +
                    ", targetColumnType='" + targetColumnType + '\'' +
                    '}';
        }

        public String generateInsertStatement(String tableName) {
            StringBuilder builder = new StringBuilder();
            builder.append("insert into ").append(tableName)
                    .append(" values (")
                    .append(id)
                    .append(", '").append(sourceType).append("' ")
                    .append(", '").append(sourceColumnType).append("' ")
                    .append(", '").append(targetType).append("' ")
                    .append(", '").append(targetColumnType).append("' ")
                    .append(");");
            return builder.toString();
        }
    }

}
