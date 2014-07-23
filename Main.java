import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class Main {

    static JTextPane area = new JTextPane();

    static LineRefs rr;

    private static void newSearch(String query) throws BadLocationException, IOException {
        final AbstractDocument d = new DefaultStyledDocument();

        area.setDocument(d);

        rr = LineRefs.scan(new File("."), query);

        rr.dump(d);

        d.setDocumentFilter(new DocumentFilter() {

            int readonly = rr.getNameLength() + 2;

            boolean isReadOnly(FilterBypass fb, int offset) throws BadLocationException {
                if (offset < readonly)
                    return true;

                final Document d = fb.getDocument();
                final String text = d.getText(0, d.getLength());

                for (int p = offset-1; p >= (offset - readonly); p--) {
                    if (text.charAt(p) == '\n') {
                        return true;
                    }
                }

                return false;
            }

            @Override
            public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
                if (!isReadOnly(fb, offset))
                    super.remove(fb, offset, length);
            }

            @Override
            public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                if (!isReadOnly(fb, offset))
                    super.insertString(fb, offset, string, attr);
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs) throws BadLocationException {
                if (!isReadOnly(fb, offset))
                    super.replace(fb, offset, length, text, attrs);
            }
        });
    }

    public static void main(String[] args) throws BadLocationException, IOException {
        JFrame mainFrame = new JFrame("Text");
        mainFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        area.setPreferredSize(new Dimension(300, 300));

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));



        JPanel searchPanel = new JPanel(new FlowLayout());
        final JTextField query = new JTextField(80);
        final AbstractAction searcher = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    newSearch(query.getText());
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        };
        query.addActionListener(searcher);

        searchPanel.add(query);

        JButton search = new JButton("Search");

        search.addActionListener(searcher);
        searchPanel.add(search);

        newSearch("asm.syslog.port");

        p.add(searchPanel);

        p.add(area);

        final JButton save = new JButton("Save");

        save.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    final List<LineRef> refs = rr.read(area.getDocument());
                    for (LineRef r : refs) {
                        System.out.println(r.getFilename() + " >> " + r.getContent());
                    }

                    LineRefs.save(refs);
                } catch (BadLocationException e1) {
                    e1.printStackTrace();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        });
        p.add(save);


        mainFrame.setContentPane(p);
        mainFrame.setVisible(true);
        mainFrame.pack();

    }
}
