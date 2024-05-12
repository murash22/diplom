package widgets;

import utils.Conversions;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;


public class HexTextArea extends JTextArea implements DocumentListener {
    private boolean filtering = false;

    public HexTextArea() {
        super();
        postConstructor();
    }

    public HexTextArea(String text, int rows, int columns) {
        super(text, rows, columns);
        postConstructor();
    }

    private void postConstructor() {
        getDocument().addDocumentListener(this);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        filterText();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        filterText();
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        filterText();
    }

    private void filterText() {
        if (filtering) return;
        filtering = true;

        EventQueue.invokeLater(() -> {
            String input = getText().toUpperCase();
            StringBuilder filtered = new StringBuilder();
            int index = 0;

            for (int i = 0; i < input.length(); i++) {
                char c = Character.toUpperCase(input.charAt(i));
                if ("0123456789ABCDEF".indexOf(c) >= 0) {
                    filtered.append(c);
                    if (index++ % 2 == 1 && i != input.length() - 1) {
                        filtered.append(" ");
                    }
                }
            }

            setText(filtered.toString());
            filtering = false;
        });
    }

    public byte[] getBytes() {
        return Conversions.hex(getText());
    }
}
