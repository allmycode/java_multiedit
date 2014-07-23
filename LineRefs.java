import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class LineRefs {
    final List<LineRef> list;
    final int nameLength;

    public LineRefs(List<LineRef> list) {
        this.list = list;
        int max = 0;
        for (LineRef r : list) {
            if (r.getFilename().length() > max)
                max = r.getFilename().length();
        }
        nameLength = max;
    }

    public static LineRefs scan(File dir, String propName) throws IOException {
        List<LineRef> refs = new ArrayList<LineRef>();
        for (File f : dir.listFiles()) {
            if (f.getName().endsWith("properties")) {
                BufferedReader rd = new BufferedReader(new FileReader(f));
                int i = 0;
                String line;
                while ((line = rd.readLine()) != null) {
                    if (line.contains(propName)) {
                        refs.add(new LineRef(f.getAbsolutePath(), f.getName(), i, line));
                    }
                    i++;
                }
            }
        }
        return new LineRefs(refs);
    }

    public List<LineRef> getList() {
        return list;
    }

    public int getNameLength() {
        return nameLength;
    }

    public void dump(Document d) throws BadLocationException {
        final AttributeSet black = colored(Color.black);
        final AttributeSet blue = colored(Color.blue);

        int o = 0;
        for (LineRef r: list) {
            d.insertString(o, r.getFilename() + ":", blue);
            o += r.getFilename().length() + 1;
            d.insertString(o, " " + r.getContent() + "\n", black);
            o += r.getContent().length() + 2;

        }
    }

    public List<LineRef> read(Document d) throws BadLocationException {
        List<LineRef> res = new ArrayList<LineRef>(list.size());

        final String text = d.getText(0, d.getLength());
        final String[] lines = text.split("\n");
        for (int i = 0; i < lines.length; i++) {
            final LineRef orig = list.get(i);
            String line = lines[i].substring(orig.getFilename().length()+2);

            res.add(orig.newContent(line));
        }
        return res;
    }

    public static void save(List<LineRef> refs) throws IOException {
        for (LineRef r : refs) {
            // Open a temporary file to write to.
            final StringWriter sw = new StringWriter();
            PrintWriter writer = new PrintWriter(sw);

            BufferedReader br = new BufferedReader(new FileReader(r.getFilepath()));
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (i == r.getLine())
                    writer.println(r.getContent());
                else
                    writer.println(line);
                i++;
            }
            br.close();
            writer.close();

            final String str = sw.toString();
            System.out.println(str);

            final FileWriter fileWriter = new FileWriter(r.getFilepath());
            fileWriter.write(str);
            fileWriter.close();

            System.out.println("Saved " + r.getFilename() + " >> " + r.getContent());


        }
    }

    private static AttributeSet colored(Color c) {
        MutableAttributeSet attributes = new SimpleAttributeSet();
        StyleConstants.setForeground(attributes, c);
        return attributes;
    }
}
