package com.adaptivescale;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;


public class AIServiceTest {

    private static boolean isSelectStatement(String query) {
        boolean isSelectStatement = true;
        try {
            Statement statement = CCJSqlParserUtil.parse(query);
            if (!(statement instanceof Select)) {
                return false;
            }
        } catch (JSQLParserException e) {
            return false;
        }
        return isSelectStatement;
    }

    @Test
    void testIsSelectStatementGoodCase() {
        String goodQuery = "SELECT * FROM table_name;";
        assertTrue(isSelectStatement(goodQuery));
    }

    @Test
    void testIsSelectStatementBadCase() {
        String badQuery = "UPDATE table_name SET column_name = value WHERE condition;";
        assertFalse(isSelectStatement(badQuery));
    }

}
