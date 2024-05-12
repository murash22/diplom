package widgets;

import utils.Conversions;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


public class HexTextField extends JTextField implements DocumentListener {
    private boolean filtering = false;
    private boolean supportsStars = false;
    private int bytesLength = 0;

    public HexTextField(boolean supportsStars) {
        super();
        postConstructor(0, supportsStars);
    }

    public HexTextField(int maxLength, boolean supportsStars) {
        super();
        postConstructor(maxLength, supportsStars);
    }

    public HexTextField(String text, int maxLength, boolean supportsStars) {
        super(text, maxLength);
        postConstructor(maxLength, supportsStars);
    }

    private void postConstructor(int maxLength, boolean supportsStars) {
        this.supportsStars = supportsStars;
        setBytesLength(maxLength);
        getDocument().addDocumentListener(this);
        setFont(new Font(Font.MONOSPACED, Font.PLAIN, getFont().getSize()));
    }

    public void setBytesLength(int bytes) {
        bytesLength = bytes;
        setColumns(bytes * 3);
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

    @Override
    public void setCaretPosition(int position) {
        super.setCaretPosition(Math.min(position, getColumns()));
    }

    private boolean isHexDigit(char c) {
        return "0123456789ABCDEF".indexOf(c) >= 0;
    }

    private String filter(String str) {
        return str.chars().filter(i -> isHexDigit((char) i) || ((char) i == '*' && supportsStars)).mapToObj(i -> "" + (char) i).collect(Collectors.joining());
    }

    private void filterText() {
        if (filtering) return;
        filtering = true;

        EventQueue.invokeLater(() -> {
            int caret = getCaretPosition();

            String input = filter(getText().toUpperCase());
            List<String> filtered = new ArrayList<>(bytesLength);

            for (int i = 0; i < input.length(); i++) {
                if (isHexDigit(input.charAt(i))) {
                    if (i == input.length() - 1) filtered.add(input.charAt(i) + "0");
                    else if (!isHexDigit(input.charAt(i + 1))) filtered.add("0" + input.charAt(i));
                    else filtered.add(input.substring(i, ++i + 1));
                } else if (supportsStars && input.charAt(i) == '*') filtered.add("*");
                if (filtered.size() == bytesLength) {
                    break;
                }
            }
            while (filtered.size() < bytesLength) filtered.add("00");

            setText(String.join(" ", filtered));
            setCaretPosition(Math.min(caret, getColumns() - 1));
            filtering = false;
        });
    }

    public byte[] getBytes() {
        return Conversions.hex(getText());
    }
}
