import java.util.Scanner;
import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;

public class Main {
    static String prochitatFayl(Path path) throws IOException {
        return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
    }
    static JPanel sozdatPanel(String example) {
        JPanel root = new JPanel(new BorderLayout(12,12));
        root.setBorder(BorderFactory.createEmptyBorder(18,20,18,20));
        JLabel title = new JLabel("Слова из слова");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 34f));
        root.add(title, BorderLayout.NORTH);
        JTextArea input = new JTextArea(example), output = new JTextArea();
        output.setEditable(false);
        JPanel editors = new JPanel(new GridLayout(1,2,12,0));
        for (JTextArea area : new JTextArea[]{input,output}) {
            area.setColumns(24);
            area.setFont(new Font("Monospaced", Font.PLAIN, 14));
            JScrollPane scroll = new JScrollPane(area);
            scroll.setBorder(BorderFactory.createTitledBorder(area == input ? "Входные данные" : "Уровни JSON"));
            editors.add(scroll);
        }
        root.add(editors);
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton run = new JButton("Сгенерировать"), save = new JButton("Сохранить JSON");
        JLabel status = new JLabel();
        buttons.add(run); buttons.add(save); buttons.add(status);
        root.add(buttons, BorderLayout.SOUTH);
        Runnable refresh = () -> {
            try {
                output.setText(Generator.sozdatUrovni(input.getText()));
                output.setCaretPosition(0);
                save.setEnabled(true);
                status.setText("Готово");
            } catch (IllegalArgumentException e) {
                output.setText("");
                save.setEnabled(false);
                status.setText(e.getMessage());
            }
        };
        input.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { sbrositRezultat(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { sbrositRezultat(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { sbrositRezultat(); }
            private void sbrositRezultat() {
                output.setText(""); save.setEnabled(false); status.setText("Данные изменены");
            }
        });
        run.addActionListener(e -> refresh.run());
        save.addActionListener(e -> {
            JFileChooser dialog = new JFileChooser();
            dialog.setSelectedFile(new File("levels.json"));
            if (dialog.showSaveDialog(root) != JFileChooser.APPROVE_OPTION) return;
            File file = dialog.getSelectedFile();
            if (file.exists() && JOptionPane.showConfirmDialog(root,"Заменить файл?",
                    "Сохранение",JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;
            try {
                Files.write(file.toPath(), output.getText().getBytes(StandardCharsets.UTF_8));
                status.setText("Файл сохранён");
            } catch (IOException ex) { JOptionPane.showMessageDialog(root, ex.getMessage()); }
        });
        refresh.run();
        return root;
    }
    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            String output = Generator.sozdatUrovni(prochitatFayl(Paths.get(args[0])));
            Files.write(Paths.get(args[1]), output.getBytes(StandardCharsets.UTF_8));
            return;
        }
        if (args.length != 0) throw new IllegalArgumentException("java -jar Words.jar [input.json output.json]");
        String example;
        try (InputStream in = Main.class.getResourceAsStream("/example.json");
             Scanner scanner = new Scanner(in, "UTF-8").useDelimiter("\\A")) {
            example = scanner.next();
        }
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Слова из слова");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(sozdatPanel(example));
            frame.setSize(1060,700);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
