public class LineRef {
    private final String filepath;
    private final String filename;
    private final int line;
    private String content;

    public LineRef(String filepath, String filename, int line, String content) {
        this.filepath = filepath;
        this.filename = filename;
        this.line = line;
        this.content = content;
    }

    public String getFilename() {
        return filename;
    }

    public int getLine() {
        return line;
    }

    public String getFilepath() {
        return filepath;
    }

    public String getContent() {
        return content;
    }

    public LineRef newContent(String newContent) {
        return new LineRef(filepath, filename, line, newContent);
    }



}
