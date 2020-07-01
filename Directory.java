import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Directory info. Hold the files that it has and tracks statiscs. To know more
 * info: https://github.com/FlyingWolFox/Duplicate-Finder
 * 
 * @version 1.3-beta
 * @author FlyingWolFox
 */
public class Directory {
    private ArrayList<FileInfo> files;
    private ArrayList<Archive> archives;
    private Path path;
    private int num; // dir id
    private static int x; // used to generate dir id
    private int numOfInternalRepetions;
    private int numOfRepetions;
    static {
        x = 0;
    }

    /**
     * Main contructor, takes the path to the directory and process all files
     * 
     * @param path path to the directory
     * @throws IOException if there was an error opening the directory
     */
    public Directory(String path, String no) throws IOException {
        num = x; // gets dir id
        this.path = Paths.get(path);
        files = new ArrayList<FileInfo>();
        archives = new ArrayList<Archive>();
        System.out.println("");
        System.out.println("Opening " + path);
        // open the DirectoryStream to get files
        DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path));
        for (Path file : stream) {
            if (!file.toFile().isDirectory()) {
                if (Archive.isArchive(file.getFileName().toString())) {
                    archives.add(new Archive(file.toFile(), this));
                } else
                    files.add(new FileInfo(file.toFile(), this));
            }
        }
        trasnformSingleFileArchives();
        Collections.sort(files); // sorts based on hash
        if (archives.size() != 0)
            Collections.sort(archives, archives.get(0).new ArchiveComparator());
        x++;
        stream.close();
    }

    public Directory(String path) throws IOException {
        num = x; // gets dir id
        this.path = Paths.get(path);
        files = new ArrayList<FileInfo>();
        archives = new ArrayList<Archive>();
        FileInfo[][] cache = Cache.getCache(this);
        List<FileInfo> fileCache = Arrays.asList(cache[0]);
        List<Archive> archiveCache = Arrays.asList((Archive[]) cache[1]);
        ArrayList<FileInfo> fileDeletions = new ArrayList<FileInfo>();
        ArrayList<FileInfo> fileAditions = new ArrayList<FileInfo>();
        ArrayList<Archive> archiveDeletions = new ArrayList<Archive>();
        ArrayList<Archive> archiveAditions = new ArrayList<Archive>();

        // Even if the cache file doesn't exists, nothing should break
        for (FileInfo file : fileCache) {
            if (!file.getFile().exists())
                fileDeletions.add(file);

            if (String.valueOf(file.getFile().lastModified()).equals(file.getLastModified())) {
                fileDeletions.add(file);
                fileAditions.add(file);
            }
        }
        fileCache.removeAll(fileDeletions);
        for (int i = 0; i < fileAditions.size(); i++)
            fileAditions.set(i, new FileInfo(fileAditions.get(i).getFile(), this));
        fileCache.addAll(fileAditions);

        for (Archive archive : archiveCache) {
            if (!archive.getFile().exists())
                archiveDeletions.add(archive);

            if (String.valueOf(archive.getFile().lastModified()).equals(archive.getLastModified())) {
                archiveDeletions.add(archive);
                archiveAditions.add(archive);
            }
        }
        archiveCache.removeAll(archiveDeletions);
        for (int i = 0; i < archiveAditions.size(); i++)
            archiveAditions.set(i, new Archive(archives.get(i).getFile(), this));
        fileCache.addAll(archiveAditions);

        ArrayList<Path> filesPaths = new ArrayList<Path>();
        for (FileInfo file : fileCache)
            filesPaths.add(file.getPath());
        ArrayList<Path> archivesPaths = new ArrayList<Path>();
        for (Archive archive : archiveCache)
            archivesPaths.add(archive.getPath());

        // open the DirectoryStream to get files
        DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(path));
        for (Path file : stream) {
            if (!file.toFile().isDirectory()) {
                if (Archive.isArchive(file.getFileName().toString())) {
                    archivesPaths.add(file);
                } else
                    filesPaths.add(file);
            }
        }
        stream.close();

        Collections.sort(filesPaths);
        Collections.sort(archivesPaths);
        for (int i = 0; i < filesPaths.size() - 1; i++) {
            String name1 = filesPaths.get(i).getFileName().toString();
            String name2 = filesPaths.get(i + 1).getFileName().toString();
            if (name1.equals(name2)) {
                filesPaths.remove(i);
                filesPaths.remove(i + 1);
                i--;
            }
        }

        for (int i = 0; i < archivesPaths.size() - 1; i++) {
            String name1 = archivesPaths.get(i).getFileName().toString();
            String name2 = archivesPaths.get(i + 1).getFileName().toString();
            if (name1.equals(name2)) {
                archivesPaths.remove(i);
                archivesPaths.remove(i + 1);
                i--;
            }
        }

        System.out.println("");
        System.out.println("Opening " + path);
        ProgressBar bar = new ProgressBar("Hashing Files", filesPaths.size());
        for (Path file : filesPaths) {
            fileAditions.add(new FileInfo(file.toFile(), this));
            bar.update();
        }

        bar = new ProgressBar("Hashing Archives", archivesPaths.size());
        for (Path archive : archivesPaths) {
            archiveAditions.add(new Archive(archive.toFile(), this));
            bar.update();
        }

        files.addAll(fileCache);
        files.addAll(fileAditions);
        archives.addAll(archiveCache);
        archives.addAll(archiveAditions);

        FileInfo[][] filesUpdate = { fileAditions.toArray(new FileInfo[fileAditions.size()]), fileDeletions.toArray(new FileInfo[fileAditions.size()]) };
        Archive[][] archivesUpdate = { archiveAditions.toArray(new Archive[archiveAditions.size()]), archiveDeletions.toArray(new Archive[archiveAditions.size()]) };

        Cache.updateCache(this, filesUpdate, archivesUpdate);

        System.out.println("");
        System.out.println("Opening " + path);
    }

    /**
     * looks for archives that contains a single file. Transforms archive into file
     * by using the compressed file hash instead of the archive file hash and puts
     * it with in files ArrayList
     */
    public void trasnformSingleFileArchives() {
        // Uses iterator to avoid java.util.ConcurrentModificationException
        for (Iterator<Archive> i = archives.iterator(); i.hasNext();) {
            Archive archive = i.next();
            if (archive.hasSingleFile()) {
                archive.getHashFromFile();
                files.add(archive);
                i.remove();
            }
        }
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
     * @return ArrayList of archives of this dir
     */
    public ArrayList<Archive> getArchives() {
        return archives;
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