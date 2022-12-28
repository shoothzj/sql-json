package io.github.shoothzj.sql.json.core;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.avatica.util.Casing;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlBinaryOperator;
import org.apache.calcite.sql.SqlCharStringLiteral;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNumericLiteral;
import org.apache.calcite.sql.SqlOperator;
import org.apache.calcite.sql.SqlSelect;
import org.apache.calcite.sql.fun.SqlStdOperatorTable;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.parser.impl.SqlParserImpl;
import org.apache.calcite.tools.FrameworkConfig;
import org.apache.calcite.tools.Frameworks;
import org.apache.calcite.util.NlsString;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
public class SqlJsonParser {

    private final ObjectMapper mapper;

    private final FrameworkConfig config;

    public SqlJsonParser() {
        SqlStdOperatorTable operatorTable = SqlStdOperatorTable.instance();
        SqlParser.Config parserConfig = SqlParser.config()
                .withParserFactory(SqlParserImpl.FACTORY)
                .withCaseSensitive(true)
                .withQuotedCasing(Casing.UNCHANGED)
                .withUnquotedCasing(Casing.UNCHANGED);
        Frameworks.ConfigBuilder builder = Frameworks.newConfigBuilder()
                .parserConfig(parserConfig).operatorTable(operatorTable);
        config = builder.build();
        mapper = new ObjectMapper();
    }

    public SqlNode parseSql(String sql) throws SqlParseException {
        SqlParser parser = SqlParser.create(sql, config.getParserConfig());
        return parser.parseQuery();
    }

    public ObjectNode execute(String sql, String json) throws SqlParseException, JsonProcessingException {
        SqlNode sqlNode = parseSql(sql);
        return executeSql(sqlNode, mapper.readTree(json));
    }

    public ObjectNode executeSql(SqlNode sqlNode, JsonNode json) {
        if (sqlNode instanceof SqlSelect) {
            return executeSqlSelect((SqlSelect) sqlNode, json);
        }
        throw new IllegalStateException(String.format("not support sql type %s", sqlNode));
    }

    public ObjectNode executeSqlSelect(SqlSelect sql, JsonNode jsonNode) {
        ObjectNode objectNode = mapper.createObjectNode();
        Boolean result = (Boolean) calculate(sql.getWhere(), jsonNode);
        if (result == null || !result) {
            return objectNode;
        }
        for (SqlNode sqlNode : sql.getSelectList()) {
            String name = sqlNode.toString();
            this.putObject(objectNode, name, calculate(sqlNode, jsonNode));
        }
        return objectNode;
    }

    private Object calculate(SqlNode sqlNode, JsonNode jsonNode) {
        if (sqlNode instanceof SqlNumericLiteral sqlNumericLiteral) {
            BigDecimal bigDecimal = sqlNumericLiteral.bigDecimalValue();
            if (bigDecimal == null) {
                throw new IllegalStateException(String.format("invalid sql number %s", sqlNumericLiteral));
            }
            if (sqlNumericLiteral.isInteger()) {
                return bigDecimal.intValue();
            } else {
                return bigDecimal.doubleValue();
            }
        }
        if (sqlNode instanceof SqlCharStringLiteral sqlCharStringLiteral) {
            return sqlCharStringLiteral.getValueAs(NlsString.class).getValue();
        }
        if (sqlNode instanceof SqlIdentifier sqlIdentifier) {
            return calculate(sqlIdentifier, jsonNode);
        }
        if (sqlNode instanceof SqlBasicCall sqlBasicCall) {
            return calculate(sqlBasicCall, jsonNode);
        }
        throw new IllegalStateException(String.format("not support sqlNode %s", sqlNode));
    }

    private Object calculate(SqlIdentifier sqlIdentifier, JsonNode jsonNode) {
        JsonNode node = jsonNode.at("/" + sqlIdentifier.toString());
        if (node instanceof IntNode) {
            return node.asInt();
        }
        if (node instanceof TextNode) {
            return node.asText();
        }
        return node;
    }

    private Object calculate(SqlBasicCall sqlBasicCall, JsonNode jsonNode) {
        SqlOperator operator = sqlBasicCall.getOperator();
        if (operator instanceof SqlBinaryOperator) {
            return calculateBinaryOperator((SqlBinaryOperator) operator, sqlBasicCall, jsonNode);
        }
        return null;
    }

    private Boolean calculateBinaryOperator(SqlBinaryOperator operator, SqlBasicCall sqlBasicCall, JsonNode jsonNode) {
        List<SqlNode> operandList = sqlBasicCall.getOperandList();
        if (operandList.size() != 2) {
            throw new IllegalStateException(String.format("wrong sql operandSize %d", operandList.size()));
        }
        Object left = calculate(operandList.get(0), jsonNode);
        Object right = calculate(operandList.get(1), jsonNode);
        if (operator == SqlStdOperatorTable.EQUALS) {
            return calculateEqual(left, right);
        } else if (operator == SqlStdOperatorTable.LESS_THAN) {
            return calculateLess(left, right);
        }
        throw new IllegalStateException(String.format("not support sql operator %s", operator));
    }

    private Boolean calculateEqual(Object left, Object right) {
        if (left instanceof Integer) {
            return calculateEqualInt((Integer) left, (Integer) right);
        }
        if (left instanceof Long) {
            return calculateEqualLong((Long) left, (Long) right);
        }
        if (left instanceof String) {
            return calculateEqualString((String) left, (String) right);
        }
        throw new IllegalStateException(String.format("not support sql operator %s", left.getClass()));
    }

    private Boolean calculateEqualInt(Integer left, Integer right) {
        return left.equals(right);
    }

    private Boolean calculateEqualLong(Long left, Long right) {
        return left.equals(right);
    }

    private Boolean calculateEqualString(String left, String right) {
        return left.equals(right);
    }

    private Boolean calculateLess(Object left, Object right) {
        if (left instanceof Integer) {
            return calculateLessInt((Integer) left, (Integer) right);
        }
        if (left instanceof Long) {
            return calculateLessLong((Long) left, (Long) right);
        }
        if (left instanceof String) {
            return calculateEqualString((String) left, (String) right);
        }
        throw new IllegalStateException(String.format("not support sql operator %s", left.getClass()));
    }

    private Boolean calculateLessInt(Integer left, Integer right) {
        return left < right;
    }

    private Boolean calculateLessLong(Long left, Long right) {
        return left < right;
    }

    private void putObject(ObjectNode objectNode, String key, Object value) {
        if (value instanceof MissingNode) {
            return;
        }
        if (value instanceof Boolean aux) {
            objectNode.put(key, aux);
            return;
        }
        if (value instanceof Integer aux) {
            objectNode.put(key, aux);
            return;
        }
        if (value instanceof IntNode aux) {
            objectNode.put(key, aux.asInt());
            return;
        }
        if (value instanceof JsonNode aux) {
            objectNode.set(key, aux);
            return;
        }
        throw new IllegalStateException(String.format("not support sql type %s", value));
    }

}
