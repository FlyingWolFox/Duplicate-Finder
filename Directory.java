import java.util.ArrayList;
import java.util.Collections;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Directory {
    private ArrayList<ROM> roms;
    private Path path;
    private int num;
    private static int x;
    private int numOfInternalRepetions;
    private int numOfRepetions;
    static {
        x = 0;
    }

    public Directory(String path) throws IOException, DirectoryIteratorException {
        num = x;
        this.path = Paths.get(path);
        roms = new ArrayList<ROM>();
        DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path));
        for (Path file : stream) {
            if (!file.toFile().isDirectory()) {
                System.out.println("Opening: " + file.getFileName());
                roms.add(new ROM(file.toFile(), this));
            }
        }
        Collections.sort(roms);
        x++;
    }

    public Path getPath() {
        return path;
    }

    public ArrayList<ROM> getROMs() {
        return roms;
    }

    public int getNum() {
        return num;
    }

    public static int getX() {
        return x;
    }


    /**
     * @return int return the numOfDuplicates
     */
    public int getNumOfInternalRepetions() {
        return numOfInternalRepetions;
    }

    /**
     * @param numOfInternalRepetions the numOfDuplicates to set
     */
    public void increaseNumOfInternalRepetions() {
        this.numOfInternalRepetions++;
    }

    /**
     * @return int return the numOfRepetions
     */
    public int getNumOfRepetions() {
        return numOfRepetions;
    }

    /**
     * @param numOfRepetions the numOfRepetions to set
     */
    public void increaseNumOfRepetions() {
        this.numOfRepetions++;
    }

}