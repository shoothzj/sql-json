package io.github.shoothzj.sql.json.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqlJsonExecuteTest {

    private final SqlJsonParser parser = new SqlJsonParser();

    @Test
    public void executeSqlTestCase1() throws SqlParseException, JsonProcessingException {
        String sql = """
                SELECT a FROM aa WHERE b = 'yyy'
                """;
        String json = """
                {
                "a":5,
                "b": "yyy"
                }
                """;
        JsonNode jsonNode = parser.execute(sql, json);
        Assertions.assertNotNull(jsonNode);
        Assertions.assertEquals("{\"a\":5}", jsonNode.toString());
    }

    @Test
    public void executeSqlTestCase2() throws SqlParseException, JsonProcessingException {
        String sql = """
                SELECT name FROM XX WHERE age < 40
                """;
        String json = """
                {
                "name": "Tony Stark",
                "age": 42
                }
                """;
        JsonNode jsonNode = parser.execute(sql, json);
        Assertions.assertNotNull(jsonNode);
        Assertions.assertEquals("{}", jsonNode.toString());
    }

}
