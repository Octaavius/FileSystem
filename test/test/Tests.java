import org.junit.jupiter.api.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

public class Tests {
    private final MFS mfs = new MFS();
    private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;

    public Tests() throws IOException {
    }

    @BeforeEach
    public void setUpStreams() {
        System.setOut(new PrintStream(outputStream));
    }
    @AfterEach
    public void restoreStreams() {
        System.setOut(originalOut);
    }
    @AfterEach
    public void cleanUp() {
        MFS.removeDirectoryFromSourceFolder();
    }
    @Test
    public void addFile() throws IOException {
        mfs.executeCommand("touch", "root-firstFile!");
        mfs.executeCommand("ls", "root");
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "firstFile!         File";
        Assertions.assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    public void addFolder() throws IOException {

        mfs.executeCommand("mkdir", "root-1");
        mfs.executeCommand("mkdir", "root-2");
        mfs.executeCommand("mkdir", "root-3");
        mfs.executeCommand("ls", "root");

        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = """
                1                  File folder\r
                2                  File folder\r
                3                  File folder""";
        Assertions.assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    public void deleteDirectory() throws IOException {
        mfs.executeCommand("mkdir", "root-1");
        mfs.executeCommand("rmdir", "root-1");
        mfs.executeCommand("ls", "root");

        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "";
        Assertions.assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    public void moveFolder() throws IOException {
        mfs.executeCommand("mkdir", "root-1");
        mfs.executeCommand("mkdir", "root-2");
        mfs.executeCommand("mvdir", "root-2", "root-1");
        mfs.executeCommand("ls", "root-1-2");
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "";
        Assertions.assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    public void printFolderContent() throws IOException {
        mfs.executeCommand("mkdir", "root-1");
        mfs.executeCommand("touch", "root-1.txt");
        mfs.executeCommand("ls", "root");
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = """
            1                  File folder\r
            1.txt              File""";
        Assertions.assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    public void writeToFileAndReadFromFile() throws IOException {
        mfs.executeCommand("touch", "root-1.txt");
        mfs.executeCommand("echo", "root-1.txt", "hello world!");
        mfs.executeCommand("cat", "root-1.txt");
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "hello world!";
        Assertions.assertEquals(expectedOutput, consoleOutput);
    }

    @Test
    //Copy file
    public void copyFile() throws IOException {
        mfs.executeCommand("touch", "root-a.txt");
        mfs.executeCommand("mkdir", "root-1");
        mfs.executeCommand("copy", "root-a.txt", "root-1");
        mfs.executeCommand("ls", "root-1");
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "a.txt              File";
        Assertions.assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    public void deleteFile() throws IOException {
        mfs.executeCommand("touch", "root-1.txt");
        mfs.executeCommand("delete", "root-1.txt");
        mfs.executeCommand("ls", "root");
        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "";
        Assertions.assertEquals(expectedOutput, consoleOutput);
    }
    @Test
    public void errorTest() throws IOException {
        mfs.executeCommand("touch", "root-root-1.txt");

        mfs.executeCommand("touch", "root-1.txt");
        mfs.executeCommand("touch", "root-1.txt");

        mfs.executeCommand("ls", "root-1");

        String consoleOutput = outputStream.toString().trim();
        System.out.println(consoleOutput);
        String expectedOutput = "Error: Directory 'root-root' not found.\r\n" +
                "Error: File 'root-1.txt' already exists.\r\n" +
                "Error: Directory 'root-1' not found.";
        Assertions.assertEquals(expectedOutput, consoleOutput);
    }
}
