import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompilerGUI extends JFrame {

    private JTextArea codeTextArea;
    private JTextArea lexicalResultTextArea;
    private JTextArea syntaxResultTextArea;
    private JTextArea semanticResultTextArea;
    private JButton lexicalBtn;
    private JButton syntaxBtn;
    private JButton semanticBtn;

    private boolean lexicalAnalysisSuccessful = false;
    private boolean syntaxAnalysisSuccessful = false;
    private static final Map<String, String> tokenMap = new HashMap<>();

    static
    {
        String[] keywords = {"int", "double", "char", "String", "if", "else", "for", "while", "return"};
        String[] operators = {"+", "-", "*", "/", "%", "==", "!=", "(", ")", "{", "}", "[", "]", ",", ".", "++", "--", "->"};
        String[] booleanValues = {"true", "false"};
        for (String keyword : keywords)
        {
            tokenMap.put(keyword, "<keyword>");
        }

        for (String operator : operators)
        {
            tokenMap.put(operator, "<operator>");
        }

        for (String booleanValue : booleanValues)
        {
            tokenMap.put(booleanValue, "<boolean>");
        }

        tokenMap.put("=", "<assignment_operator>");
        tokenMap.put(";", "<delimiter>");
    }

    public CompilerGUI() {
        setTitle("Compiler GUI");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();

        JPanel openFilePanel = new JPanel();
        JButton openFileBtn = new JButton("Open File");
        codeTextArea = new JTextArea(20, 40);
        openFileBtn.addActionListener(new OpenFileListener());
        openFilePanel.add(openFileBtn);
        openFilePanel.add(new JScrollPane(codeTextArea));
        tabbedPane.addTab("Open File", openFilePanel);

        JPanel lexicalPanel = new JPanel();
        lexicalBtn = new JButton("Lexical Analysis");
        lexicalResultTextArea = new JTextArea(20, 40);
        lexicalBtn.setEnabled(false);
        lexicalBtn.addActionListener(new LexicalAnalysisListener());
        lexicalPanel.add(lexicalBtn);
        lexicalPanel.add(new JScrollPane(lexicalResultTextArea));
        tabbedPane.addTab("Lexical Analysis", lexicalPanel);

        JPanel syntaxPanel = new JPanel();
        syntaxBtn = new JButton("Syntax Analysis");
        syntaxResultTextArea = new JTextArea(20, 40);
        syntaxBtn.setEnabled(false);
        syntaxBtn.addActionListener(new SyntaxAnalysisListener());
        syntaxPanel.add(syntaxBtn);
        syntaxPanel.add(new JScrollPane(syntaxResultTextArea));
        tabbedPane.addTab("Syntax Analysis", syntaxPanel);

        JPanel semanticPanel = new JPanel();
        semanticBtn = new JButton("Semantic Analysis");
        semanticResultTextArea = new JTextArea(20, 40);
        semanticBtn.setEnabled(false);
        semanticBtn.addActionListener(new SemanticAnalysisListener());
        semanticPanel.add(semanticBtn);
        semanticPanel.add(new JScrollPane(semanticResultTextArea));
        tabbedPane.addTab("Semantic Analysis", semanticPanel);

        JPanel clearPanel = new JPanel();
        JButton clearBtn = new JButton("Clear");
        clearBtn.addActionListener(new ClearListener());
        clearPanel.add(clearBtn);
        tabbedPane.addTab("Clear", clearPanel);

        add(tabbedPane);

        setVisible(true);
    }

    private class OpenFileListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            lexicalAnalysisSuccessful = false;
            syntaxAnalysisSuccessful = false;
            JFileChooser fileChooser = new JFileChooser();
            int result = fileChooser.showOpenDialog(null);

            if (result == JFileChooser.APPROVE_OPTION)
            {
                try (BufferedReader reader = new BufferedReader(new FileReader(fileChooser.getSelectedFile()))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null)
                    {
                        content.append(line).append("\n");
                    }
                    codeTextArea.setText(content.toString());
                    lexicalBtn.setEnabled(true);
                }
                catch (IOException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
    }

    private class LexicalAnalysisListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            lexicalBtn.setEnabled(false);
            syntaxBtn.setEnabled(false);
            SwingUtilities.invokeLater(() -> {
                performLexicalAnalysis(codeTextArea.getText());
                lexicalBtn.setEnabled(true);
                syntaxBtn.setEnabled(lexicalAnalysisSuccessful);
            });
        }

        private void performLexicalAnalysis(String input) {
            String pattern = "\\s*(\\w+|\\S)\\s*";
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(input);

            StringBuilder result = new StringBuilder();
            while (matcher.find())
            {
                String token = matcher.group(1);
                if (tokenMap.containsKey(token))
                {
                    result.append(token).append(": ").append(tokenMap.get(token)).append("\n");
                } else {
                    if (isIdentifier(token)) {
                        result.append(token).append(": <identifier>\n");
                    } else if (isNumber(token)) {
                        result.append(token).append(": <number>\n");
                    } else {
                        result.append(token).append(": <unexpected>\n");
                        break;
                    }
                }
                try
                {
                    Thread.sleep(50);
                } catch (InterruptedException ex)
                {
                    ex.printStackTrace();
                }
            }
            lexicalAnalysisSuccessful = true;
            SwingUtilities.invokeLater(() ->
            {
                lexicalResultTextArea.setText(result.toString());
                lexicalResultTextArea.append("Lexical Analysis Successful.\n");
            });
        }

        private boolean isIdentifier(String token)
        {
            return token.matches("[a-zA-Z][a-zA-Z0-9_]*");
        }

        private boolean isNumber(String token)
        {
            return token.matches("-?\\d+(\\.\\d+)?");
        }
    }

    private class SyntaxAnalysisListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            syntaxBtn.setEnabled(false);
            new Thread(() ->
            {
                performSyntaxAnalysis(codeTextArea.getText());
                SwingUtilities.invokeLater(() ->
                {
                    if (syntaxAnalysisSuccessful)
                    {
                        syntaxResultTextArea.append("\nSyntax Analysis Successful.\n");
                        semanticBtn.setEnabled(true);
                    }
                    else
                    {
                        syntaxResultTextArea.append("\nSyntax Analysis Failed.\n");
                    }
                });
            }).start();
        }

        private void performSyntaxAnalysis(String code) {
            String[] lines = code.split("\n");

            for (int i = 0; i < lines.length; i++)
            {
                String line = lines[i];
                if (!isValidAssignmentSyntax(line.trim()))
                {
                    int finalI = i;
                    SwingUtilities.invokeLater(() ->
                    {
                        syntaxResultTextArea.append("\nSyntax Analysis Failed at Line " + (finalI + 1) + ":\n" + line + "\n");
                    });
                    syntaxAnalysisSuccessful = false;
                    return;
                }}
            syntaxAnalysisSuccessful = true;
        }

        private boolean isValidAssignmentSyntax(String line) {
            boolean isValid = line.matches("\\s*(\\w+)\\s+(\\w+)\\s*=\\s*(.+);\\s*") ||
                    line.matches("\\s*return\\s+\\w+;\\s*");
            SwingUtilities.invokeLater(() -> {
                syntaxResultTextArea.append(line + "\n");
                if (isValid)
                {
                    syntaxResultTextArea.append("  Syntax is valid.\n");
                }
                else
                {
                    syntaxResultTextArea.append("  Syntax is invalid.\n");
                }
            });
            return isValid;
        }}
    private class SemanticAnalysisListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            if (lexicalAnalysisSuccessful && syntaxAnalysisSuccessful)
            {
                String code = codeTextArea.getText();
                boolean semanticAnalysisSuccessful = performSemanticAnalysis(code);
                SwingUtilities.invokeLater(() ->
                {
                    if (semanticAnalysisSuccessful)
                    {
                        semanticResultTextArea.append("\nSemantic Analysis Result: Semantic is correct.\n");
                    }
                    else
                    {
                        semanticResultTextArea.append("\nSemantic Analysis Result: Type mismatch in some assignment statements.\n");
                    }
                });
                semanticBtn.setEnabled(false);
                disableAllTabs();
            }
            else
            {
                semanticResultTextArea.append("\nSemantic Analysis Result: Semantic analysis cannot proceed" +
                                                " without successful lexical and syntax analysis.\n");
            }}
        private boolean performSemanticAnalysis(String code) {
            String[] lines = code.split("\n");
            boolean anyTypeMismatch = false;

            for (String line : lines)
            {
                boolean isVariableCorrect = isValidAssignmentSemantics(line.trim());
                if (!isVariableCorrect)
                {
                    anyTypeMismatch = true;
                }}
            return !anyTypeMismatch;
        }
        private boolean isValidAssignmentSemantics(String line) {
            // Extract variable type, name, and assigned value from the line
            Matcher matcher = Pattern.compile("\\s*(\\w+)\\s+(\\w+)\\s*=\\s*(.+);\\s*").matcher(line);

            if (matcher.matches())
            {
                String variableType = matcher.group(1);
                String variableName = matcher.group(2);
                String assignedValue = matcher.group(3);
                boolean isValid = false;
                switch (variableType)
                {
                    case "int":
                        isValid = isInteger(assignedValue);
                        break;
                    case "double":
                        isValid = isDouble(assignedValue);
                        break;
                    case "String":
                        isValid = isString(assignedValue);
                        break;
                    default:
                        isValid = false;
                }
                boolean finalIsValid = isValid;
                SwingUtilities.invokeLater(() ->
                {
                    semanticResultTextArea.append("Variable: " + variableName + ", Type: " + variableType + ", Assigned Value: " + assignedValue + "\n");
                    semanticResultTextArea.append("Semantic Result: " + (finalIsValid ? "Correct" : "Type Mismatch") + "\n\n");
                });
                return isValid;
            }
            else if (line.matches("\\s*return\\s+\\w+;\\s*"))
            {
                SwingUtilities.invokeLater(() -> {
                    semanticResultTextArea.append("Return Statement: " + line + "\n");
                    semanticResultTextArea.append("Semantic Result: Correct\n\n");
                });
                return true;
            }return false;}
        private boolean isString(String value)
        {
            return value.startsWith("\"") && value.endsWith("\"");
        }

        private boolean isDouble(String value)
        {
            try
            {
                Double.parseDouble(value);
                return true;
            }
            catch (NumberFormatException e)
            {
                return false;
            }}
        private boolean isInteger(String value)
        {
            try
            {
                Integer.parseInt(value);
                return true;
            }
            catch (NumberFormatException e)
            {
                return false;
            }
        }
    }
    private class ClearListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e)
        {
            codeTextArea.setText("");
            lexicalResultTextArea.setText("");
            syntaxResultTextArea.setText("");
            semanticResultTextArea.setText("");
            lexicalBtn.setEnabled(false);
            syntaxBtn.setEnabled(false);
            semanticBtn.setEnabled(false);
            lexicalAnalysisSuccessful = false;
            syntaxAnalysisSuccessful = false;
        }
    }
    private void disableAllTabs()
    {
        JTabbedPane tabbedPane = (JTabbedPane) getContentPane().getComponent(0);

        for (int i = 0; i < tabbedPane.getTabCount(); i++)
        {
            Component component = tabbedPane.getComponentAt(i);
            disableComponents(component);
        }}
    private void disableComponents(Component component) {
        if (component instanceof Container)
        {
            Container container = (Container) component;
            for (Component c : container.getComponents())
            {
                disableComponents(c);
            }
        }
        else
        {
            component.setEnabled(false);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CompilerGUI());
    }
}
