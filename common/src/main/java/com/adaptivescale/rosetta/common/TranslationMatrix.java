package com.adaptivescale.rosetta.common;

import com.adaptivescale.rosetta.common.models.TranslationModel;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.sql.*;

@Slf4j
public class TranslationMatrix {

    private static final String EXTERNAL_TRANSLATION_FILE_ENV = "EXTERNAL_TRANSLATION_FILE";
    private static final String DEFAULT_TRANSLATION_MATRIX_FILE = "translation_matrix/translation.csv";
    private static final String DELIMITER = ";;";
    private static final String URL = "jdbc:h2:mem:translation;DB_CLOSE_DELAY=-1";
    private static final String TABLE_NAME = "TRANSLATION";

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
                "source_type VARCHAR(255) not null, " +
                "source_column_type VARCHAR(255) not null, " +
                "target_type VARCHAR(255) not null, " +
                "target_column_type VARCHAR(255) not null" +
                ");";
        execute(translationTable);

        String index = "CREATE INDEX source_translation_index " +
                "ON "+TABLE_NAME+" (source_type, source_column_type)";
        String uniqueIndex = "CREATE UNIQUE INDEX unique_translation_index " +
                "ON "+TABLE_NAME+" (source_type, source_column_type, target_type)";
        execute(index);
        execute(uniqueIndex);
        loadCSVData();
    }

    void loadCSVData() throws IOException {
        String line = "";

        BufferedReader br = readTranslationMatrixFile();
        StringBuilder dataInsertQuery = new StringBuilder();
        while ((line = br.readLine()) != null)
        {
            String[] translation = line.split(DELIMITER);
            TranslationModel translationModel = new TranslationModel();
            translationModel.setId(Integer.valueOf(translation[0]));
            translationModel.setSourceType(translation[1]);
            translationModel.setSourceColumnType(translation[2]);
            translationModel.setTargetType(translation[3]);
            translationModel.setTargetColumnType(translation[4]);
            String insertStatement = translationModel.generateInsertStatement(TABLE_NAME);

            dataInsertQuery.append(insertStatement);
        }
        execute(dataInsertQuery.toString());
    }

    void execute(String sql) {
        try {
            Connection connection = DriverManager.getConnection(URL);
            Statement statement = connection.createStatement();
            statement.execute(sql);
            connection.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

    public TranslationModel findById(Integer id) {
        String query = String.format("SELECT TOP 1 * from %s where id=%s", TABLE_NAME, id);
        return getSingleRecord(query);
    }

    public TranslationModel findBySourceTypeAndSourceColumnType(String sourceType, String sourceColumnType) {
        String query = String.format("SELECT * from %s where source_type='%s' and source_column_type='%s'", TABLE_NAME, sourceType, sourceColumnType);
        return getSingleRecord(query);
    }

    public TranslationModel findBySourceTypeAndSourceColumnTypeAndTargetType(String sourceType, String sourceColumnType, String targetType) {
        String query = String.format("SELECT * from %s where source_type='%s' and source_column_type='%s' and target_type='%s'",
                TABLE_NAME, sourceType, sourceColumnType.toLowerCase(), targetType);
        return getSingleRecord(query);
    }

    private BufferedReader readTranslationMatrixFile() throws FileNotFoundException {
        //Check for the translation file from the ENV variable EXTERNAL_TRANSLATION_FILE
        String externalTranslationFile = System.getenv(EXTERNAL_TRANSLATION_FILE_ENV);
        if (externalTranslationFile != null) {
            File translationFile = new File(externalTranslationFile);
            InputStream targetStream = new FileInputStream(translationFile);
            return new BufferedReader(new InputStreamReader(targetStream));
        }

        //If the file is not provided in the project read it from the resources
        InputStream resourceAsStream = TranslationMatrix.class.getClassLoader().getResourceAsStream(DEFAULT_TRANSLATION_MATRIX_FILE);
        return new BufferedReader(new InputStreamReader(resourceAsStream));
    }
}
