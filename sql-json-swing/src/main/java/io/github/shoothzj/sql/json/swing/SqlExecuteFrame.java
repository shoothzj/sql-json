package io.github.shoothzj.sql.json.swing;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.github.shoothzj.sql.json.core.SqlJsonParser;
import org.apache.calcite.sql.parser.SqlParseException;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import java.awt.GridBagConstraints;
import java.awt.HeadlessException;

public class SqlExecuteFrame extends JFrame {

    private final SqlJsonParser parser;

    private final JTextArea jsonArea;

    private final JTextArea sqlArea;

    private final JTextArea resultArea;

    public SqlExecuteFrame() throws HeadlessException {
        parser = new SqlJsonParser();

        this.setTitle("Sql Json Execute");
        this.setSize(1200, 800);

        // init
        jsonArea = new JTextArea(40, 20);
        jsonArea.setText(ExampleConst.EXAMPLE_JSON_STR);
        sqlArea = new JTextArea(10, 30);
        sqlArea.setText(ExampleConst.EXAMPLE_SQL);
        resultArea = new JTextArea(5, 40);
        resultArea.setEditable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.add(inputPanel());
        mainPanel.add(resultPanel());

        this.add(mainPanel);
    }

    private JPanel inputPanel() {
        GridBagConstraints jsonConstraints = new GridBagConstraints();
        jsonConstraints.weightx = 2.0;
        jsonConstraints.fill = GridBagConstraints.HORIZONTAL;

        GridBagConstraints sqlConstraints = new GridBagConstraints();
        sqlConstraints.weightx = 1.0;
        sqlConstraints.gridwidth = GridBagConstraints.REMAINDER;

        JPanel panel = new JPanel();
        panel.add(new JLabel("JSON:"));
        panel.add(jsonArea, jsonConstraints);
        panel.add(new JLabel("SQL:"));
        panel.add(sqlArea, sqlConstraints);

        JButton executeButton = new JButton("Execute");
        executeButton.addActionListener(e -> {
            JsonNode jsonNode;
            try {
                jsonNode = parser.execute(sqlArea.getText(), jsonArea.getText());
                if (jsonNode != null) {
                    resultArea.setText(jsonNode.toPrettyString());
                } else {
                    resultArea.setText("No Content");
                }
            } catch (SqlParseException | JsonProcessingException ex) {
                resultArea.setText(ex.toString());
            }
        });
        panel.add(executeButton);
        return panel;
    }

    private JPanel resultPanel() {
        JPanel jPanel = new JPanel();
        jPanel.add(new JLabel("Result:"));
        jPanel.add(resultArea);
        return jPanel;
    }

}
