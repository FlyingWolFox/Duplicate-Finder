import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class holds Directory info and the files that it have. Also tracks
 * internal and external repetions. To know more info:
 * https://github.com/FlyingWolFox/Duplicate-Finder
 * 
 * @version 0.9.0-2
 * @author FlyingWolFox / lips.pissaia@gmail.com
 */
public class Directory {
    private ArrayList<FileInfo> files; // files that are in the folder
    private Path path; // path to the directory
    private int num; // dir id
    private static int x; // used to generate dir id
    private int numOfInternalRepetions; // tracks how many internal repetions this dir has
    private int numOfRepetions; // tracks how many external repetions this dir was involved
    static {
        x = 0;
    }

    /**
     * Main contructor, takes the path to the directory and will process all files
     * 
     * @param path // path to the directory
     * @throws IOException // if there was an error opening the directory
     */
    public Directory(String path) throws IOException {
        num = x; // gets dir id
        this.path = Paths.get(path);
        files = new ArrayList<FileInfo>();
        // open the DirectoryStream to get files
        DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path));
        for (Path file : stream) {
            if (!file.toFile().isDirectory()) {
                System.out.println("Opening: " + file.getFileName());
                files.add(new FileInfo(file.toFile(), this));
            }
        }
        Collections.sort(files); // this sorts the collection based on the hash of each file
        x++;
        stream.close(); // closes the stream
    }

    /**
     * @return the path to this dir
     */
    public Path getPath() {
        return path;
    }

    /**
     * @return the ArrayList of files of this dir
     */
    public ArrayList<FileInfo> getFiles() {
        return files;
    }

    /**
     * @return the id of this dir
     */
    public int getNum() {
        return num;
    }

    /**
     * @return the number of directories already created
     */
    public static int getX() {
        return x;
    }

    /**
     * @return the number of internal repetions
     */
    public int getNumOfInternalRepetions() {
        return numOfInternalRepetions;
    }

    /**
     * Increases the number of internal repetions by one
     */
    public void increaseNumOfInternalRepetions() {
        this.numOfInternalRepetions++;
    }

    /**
     * @return the number of external repetions
     */
    public int getNumOfRepetions() {
        return numOfRepetions;
    }

    /**
     * Increases the number of external repetions by one
     */
    public void increaseNumOfRepetions() {
        this.numOfRepetions++;
    }

}