import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;

public class MFS {

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please provide a command.");
            return;
        }

        try{
            MFS mfs = new MFS();
            mfs.executeCommand(args[0], Arrays.copyOfRange(args, 1, args.length));
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public MFS() throws IOException{
        initMFS();
    }

    private void initMFS() throws IOException{
        File root = new File("root");
        if(!root.exists() || !root.isDirectory()) {
            Files.createDirectory(Paths.get("root"));
        }
        File rootMfs = new File("root/root.mfs");
        if (!rootMfs.exists() || rootMfs.isDirectory()) {
            Files.createFile(Paths.get("root/root.mfs"));
        }
    }


    public void executeCommand(String command, String... args) throws IOException {
        switch (command) {
            case "ls" -> listDirectory(args[0]);
            case "mkdir" -> createDirectory(args[0]);
            case "rmdir" -> removeDirectory(args[0]);
            case "mvdir" -> moveDirectory(args[0], args[1]);
            case "touch" -> createFile(args[0]);
            case "echo" -> appendToFile(args[0], args[1]);
            case "cat" -> displayFileContent(args[0]);
            case "delete" -> deleteFile(args[0]);
            case "copy" -> copyFile(args[0], args[1]);
            case "mv" -> moveFile(args[0], args[1]);
            case "help" -> help();
            default -> System.out.println("Unknown command. Type 'help' for a list of commands.");
        }
    }

    private void listDirectory(String dirName) throws IOException{
        File mfsFile = new File("root\\" + dirName + ".mfs");
        if (!mfsFile.exists() || mfsFile.isDirectory()) {
            System.out.println("Error: Directory '" + dirName + "' not found.");
            return;
        }

        List<String> lines = Files.readAllLines(Paths.get("root\\" + dirName + ".mfs"));

        List<String> sortedLines = lines.stream()
                .sorted()
                .toList();

        printSortedContents(sortedLines);
    }
    private static void printSortedContents(List<String> sortedLines) {
        // Print the sorted lines
        for (String line : sortedLines) {

            final int marginBeforeType = 20;

            String spaces = " ";
            if(marginBeforeType > line.length())
                spaces = " ".repeat(marginBeforeType - line.length());
            else
                line = line.substring(0, marginBeforeType - 4) + "...";

            System.out.print(line.substring(1));

            if(line.charAt(0) == 'D'){
                System.out.println(spaces + "File folder");
            } else {
                System.out.println(spaces + "File");
            }
        }
    }
    private void createDirectory(String dirName) throws IOException {
        File newDirectory = new File("root\\" + dirName + ".mfs");
        if (newDirectory.exists()) {
            System.out.println("Error: Directory '" + dirName + "' already exists.");
        } else {
            Files.createFile(Paths.get("root\\" + dirName + ".mfs"));
            updateMfsInfoAfterAddition(dirName, "D");
        }
    }

    private void removeDirectory(String dirName) throws IOException{
        File mfsFile = new File("root\\" + dirName + ".mfs");
        if (mfsFile.exists()) {

            List<String> lines = Files.readAllLines(Paths.get("root\\" + dirName + ".mfs"));

            lines.stream()
                    .forEach((l) -> {
                        if(l.charAt(0) == 'D'){
                            try{
                                removeDirectory(dirName + "-" + l.substring(1));
                            } catch (IOException e){e.printStackTrace();}
                        } else if(l.charAt(0) == 'F'){
                            try{
                                deleteFile(dirName + "-" + l.substring(1));
                            } catch (IOException e){e.printStackTrace();}
                        }
                    });
            Files.delete(mfsFile.toPath());
            updateMfsInfoAfterDelete(dirName, "D");

        } else {
            System.out.println("Error: Directory '" + dirName + "' not found.");
        }
    }

    private void moveDirectory(String srcDir, String targetDir) throws IOException {
        File src = new File("root\\" + srcDir + ".mfs");
        File target = new File("root\\" + targetDir + ".mfs");

        if (!src.exists()) {
            System.out.println("Error: Source directory '" + srcDir + "' not found.");
        } else if (!target.exists()) {
            System.out.println("Error: Target directory '" + targetDir + "' not found");
        } else if (srcDir.startsWith(targetDir) && srcDir.length() < targetDir.length()) {
            String[] srcSplit = srcDir.split("-");
            String[] targetSplit = targetDir.split("-");
            if(srcSplit.length < targetSplit.length){
                System.out.println("Error: Moving directory would create a cycle.");
            }
        } else {
            createDirectory(targetDir + "-" + getName(srcDir));
            List<String> lines = Files.readAllLines(Paths.get("root\\" + srcDir + ".mfs"));

            lines.stream().forEach((l) -> {
                try{
                    moveFile(srcDir + "-" + l.substring(1), targetDir + "-" + getName(srcDir));
                } catch (IOException e ){e.printStackTrace();}
            });

            removeDirectory(srcDir);
        }
    }
    private void createFile(String fileName) throws IOException {
        String parent = getParent(fileName);
        File directory = new File("root\\" + parent + ".mfs");
        if(!directory.exists()){
            System.out.println("Error: Directory '" + parent + "' not found.");
            return;
        }

        File newFile = new File("root\\" + fileName);
        if (newFile.exists()) {
            System.out.println("Error: File '" + fileName + "' already exists.");
        } else {
            Files.createFile(Paths.get("root\\" + fileName));
            if(!fileName.endsWith(".mfs"))
                updateMfsInfoAfterAddition(fileName, "F");
        }
    }

    private void appendToFile(String fileName, String content) throws IOException {
        String filePath = "root\\" + fileName;
        File file = new File(filePath);
        if (file.exists()) {
            Files.writeString(Path.of(filePath), content, StandardOpenOption.APPEND);
        } else {
            System.out.println("Error: File '" + fileName + "' not found.");
        }
    }

    private void displayFileContent(String fileName) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get("root\\" + fileName));

        for (String line : lines) {
            System.out.println(line);
        }
    }

    private void deleteFile(String fileName) throws IOException {
        File file = new File("root\\" + fileName);
        if (file.exists()) {
            updateMfsInfoAfterDelete(fileName, "F");
            Files.delete(Paths.get("root\\" + fileName));
        } else {
            System.out.println("Error: File '" + fileName + "' not found.");
        }
    }
    private void copyFile(String srcFileName, String targetDirPath) throws IOException {

        File targetDir = new File("root\\" + targetDirPath + ".mfs");

        File fileToCopy = new File("root\\" + srcFileName);

        if (!fileToCopy.exists()) {
            System.out.println("Error: Source file '" + srcFileName + "' not found.");
        } else if (!targetDir.exists()) {
            System.out.println("Error: Target directory '" + targetDir + "' not found.");
        } else {
            Path sourcePath = Paths.get("root\\" + srcFileName);
            String destinationFileName = targetDirPath + "-" + getName(srcFileName);
            Path destinationPath = Paths.get("root\\" + destinationFileName);

            createFile(destinationFileName);

            try {
                Files.copy(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                System.out.println("Failed to copy the file.");
            }
        }
    }
    private void moveFile(String srcFile, String targetDir) throws IOException{
        copyFile(srcFile, targetDir);
        deleteFile(srcFile);
    }

    private void help() throws UnsupportedEncodingException {
        System.out.println("Choose language. Enter 1 for english, 2 for polish.");
        Scanner sc = new Scanner(System.in);
        int mode = sc.nextInt();
        displayHelp(mode);
    }
    private void displayHelp(int mode) throws UnsupportedEncodingException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(System.out, "ISO-8859-2"), true);
        switch(mode){
            case 1:
            default: System.out.println(""" 
                Commands:
                ls [dir_name] - list of files in a directory.
                mkdir [dir_name] - make a new directory.
                rmdir [dir_name] - remove an old directory.
                mvdir [src_dir] [target_dir] - move a directory to a target directory.
                touch [file_name] - create a new file in current directory.
                echo [file_name] [content] - write down content to a file.
                cat [file_name] - display the content of a file.
                delete [file_name] - delete a file.
                copy [src_file] [target_dir] - copy a file to a directory to a target directory.
                mv [src_file] [target_dir] - move a file to a target directory.
                help - Display available commands.
                """);
                break;
            case 2:
                writer.println("""
                Komendy:
                ls [dir_name] - wyświetla zawartość katalogu o podanej nazwie. Wyświetlanie musi jasno rozróżniać pliki od katalogów. Sortowanie po nazwach malejąco, w kolejności kodów ASCII. Jeśli taki katalog nie istnieje, wyświetla błąd.
                mkdir [dir_name] - tworzy katalog o podanej nazwie. Jeśli taki katalog już istnieje, wyświetla błąd.
                rmdir [dir_name] - usuwa katalog o podanej nazwie z całą zawartością. Jeśli taki katalog nie istnieje, wyświetla błąd.
                mvdir [src_dir] [target_dir] - przesuwa katalog src_dir do katalogu target_dir. Jeśli któryś z katalogów nie istnieje, wyświetla błąd. Jeśli wprowadziłoby to cykl w drzewie katalogów, wyświetla błąd.
                touch [file_name] - tworzy pusty plik o podanej nazwie. Jeśli taki plik już istnieje, nie dzieje się nic.
                echo [file_name] [content] - dopisuje do pliku zawartość parametru content.
                cat [file_name] - wyświetla zawartość pliku. Jeśli taki plik nie istnieje, wyświetla błąd.
                delete [file_name] - usuwa plik o podanej nazwie. Jeśli taki plik nie istnieje, nic się nie dzieje.
                copy [src_file] [target_dir] - kopiuje plik src_file do katalogu target_dir. Jeśli któreś z nich nie istnieje, wyświetla błąd. Jeśli w katalogu docelowym plik o takiej nazwie już istnieje, wyświetla błąd.
                mv [src_file] [target_dir] - przesuwa plik src_file do katalogu target_dir. Jeśli któreś z nich nie istnieje, wyświetla błąd. Jeśli w katalogu docelowym plik o takiej nazwie już istnieje, wyświetla błąd.
                help - wyświetla możliwe komendy z krótkim opisem sposobu ich użycia.
                """);
                break;
        }
        writer.close();
    }
    private static String getName(String fileName){
        String[] components = fileName.split("-");
        return components[components.length - 1];
    }
    private String getParent(String fileName){
        String[] components = fileName.split("-");
        String parent = "";
        for(int i = 0; i < components.length - 2; i++){
            parent += components[i] + "-";
        }
        return parent + components[components.length - 2];
    }
    private void updateMfsInfoAfterAddition(String fileName, String mode) throws IOException{
        String parent = getParent(fileName);
        String mfs = "root\\" + parent + ".mfs";
        Files.writeString(Paths.get(mfs), mode + getName(fileName) + "\n", StandardOpenOption.APPEND);
    }
    private void updateMfsInfoAfterDelete(String fileName, String mode) throws IOException{
        String parent = getParent(fileName);
        String mfs = "root\\" + parent + ".mfs";
        deleteStringFromFile(Paths.get(mfs), mode + getName(fileName) + "\n");
    }
    public static void deleteStringFromFile(Path filePath, String stringToDelete) throws IOException {
        String fileContent = Files.readString(filePath);

        String modifiedContent = fileContent.replaceAll(stringToDelete, "");

        Files.write(filePath, modifiedContent.getBytes());
    }
    public static void removeDirectoryFromSourceFolder(){
        Path folder = Paths.get("root");
        try {
            Files.walkFileTree(folder, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    System.out.println("Deleted file: " + file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    System.out.println("Failed to delete file: " + file);
                    return FileVisitResult.CONTINUE;
                }
            });
            Files.delete(folder);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
