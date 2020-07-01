import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Main class where the script is executed. This will look for repetions in
 * folders passed for the constructor and will organize then in the Results
 * folder. To know more info: https://github.com/FlyingWolFox/Duplicate-Finder
 * 
 * @version 1.3-beta
 * @author FlyingWolFox
 */
public class Main {
    private ArrayList<Directory> dirs;
    private ArrayList<Path> subfolders; // subdir of the Results dir
    private HashMap<Integer, Character> letters; // number -> letter, ex: 0 -> a

    /**
     * Script
     * 
     * @param args String collction of paths to folders to compare
     */
    public Main(String[] args) {
        dirs = new ArrayList<Directory>();
        subfolders = new ArrayList<Path>();
        letters = new HashMap<Integer, Character>(26);
        boolean internalRepetion = false;
        setLetters(); // fills the HashMap

        // creates Directory objects
        for (String path : args) {
            try {
                Directory dir = new Directory(path); // exception if failed to process the directory and/or the files
                dirs.add(dir);
            } catch (IOException | DirectoryIteratorException x) {
                System.out.println("Failed to add " + path + " for analyzing");
                x.printStackTrace();
            }
        }

        // creates the Results directory and its subfolders
        createDirectories();

        // this should always be true
        assert subfolders.size() == dirs
                .size() : "The number of Results subdirectories isn't equal to the number of scanned directories";

        // process internal repetions
        internalRepetion |= manageInternalFileRepetions();
        internalRepetion |= manageIntenalArchiveRepetions();
        System.out.println("");
        if (internalRepetion) {
            System.out.println("Internal repetions found:");
            for (Directory dir : dirs) {
                System.out.println(":.. " + dir.getNumOfInternalRepetions() + " internal repetions in "
                        + dir.getPath().toString());
            }
            System.out.println(
                    ": > Internal are in their respective directories. Clean then first then run the tool again");
            return;
        } else
            System.out.println("No internal repetions found");
        System.out.println("");

        // find repetions and move files
        int numOfRepetions = 0;
        numOfRepetions += manageFileRepetions();
        numOfRepetions += manageArchiveRepetions();

        // prints statics
        System.out.println(numOfRepetions + " repetions found:");
        for (Directory dir : dirs) {
            System.out.println(":.. " + dir.getNumOfRepetions() + " files in " + dir.getPath().toString());
        }
        System.out.println("");

        System.out.println("Operation Complete");

    }

    /**
     * fills the HashMap with letter and their position on alphabet (0 = a)
     */
    private void setLetters() {
        char c = 'a';
        int i = 0;
        for (; c <= 'z'; c++, i++) {
            letters.put(i, c);
        }
    }

    /**
     * creates the Results directory and its subdirectories, which have the same
     * names of the directories to analyze
     */
    private void createDirectories() {
        Path results = Paths.get("Results");
        try {
            Files.createDirectories(results);
        } catch (IOException e) {
            // not being able to create the Results directory is terminal
            System.err.println("Error trying to create Results directory");
            e.printStackTrace();
            System.exit(-1);
        }
        // create all the subfolders
        for (int i = 0; i < Directory.getX(); i++) {
            Path subfolder = results.resolve(dirs.get(i).getPath().getFileName());
            this.subfolders.add(subfolder);
            try {
                Files.createDirectories(subfolder);
            } catch (IOException e) {
                // a fail to ccreate a sbudirectory is terminal
                System.err.println("Error trying to create directory for " + dirs.get(i).getPath().getFileName());
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    /**
     * finds internal repetions among files of the directories
     * 
     * @return if exists internal repetion
     */
    private boolean manageInternalFileRepetions() {
        boolean internalRepetionFound = false;
        for (Directory dir : dirs) {
            ArrayList<FileInfo> files = dir.getFiles();
            String letter = letters.get(dir.getNum()).toString();
            for (int i = 0; i < files.size() - 1; i++) {
                FileInfo file1 = files.get(i);
                FileInfo file2 = files.get(i + 1);
                if (file1.compareTo(file2) == 0) {
                    internalRepetionFound = true;
                    Path source1 = file1.getPath();
                    file1.setName(file1.getNum() + letter + "- " + file1.getName());
                    Path target1 = subfolders.get(dirs.indexOf(dir)).resolve(file1.getName());

                    while (file1.compareTo(file2) == 0) {
                        dir.increaseNumOfInternalRepetions();
                        Path source2 = file2.getPath();
                        file2.setName(file1.getNum() + letter + "- " + file2.getName());
                        Path target2 = subfolders.get(dirs.indexOf(dir)).resolve(file2.getName());

                        try {
                            Files.move(source2, target2);
                        } catch (IOException e) {
                            System.out.println(
                                    "Error moving " + source2.toString() + " to " + target2.toString() + ": " + e);
                        } finally {
                            files.remove(file2);
                        }

                        try {
                            file2 = files.get(i + 1);
                        } catch (IndexOutOfBoundsException e) {
                            // end of ArrayList reached
                            break;
                        }
                    }

                    try {
                        Files.move(source1, target1);
                    } catch (IOException e) {
                        System.out
                                .println("Error moving " + source1.toString() + " to " + target1.toString() + ": " + e);
                    }
                }
            }
        }

        return internalRepetionFound;
    }

    /**
     * finds repetions among files and will organize them. Since the collection is
     * sorted, files with the same hash are togheter
     * 
     * @return how many repetions were found
     */
    private int manageFileRepetions() {
        int numOfRepetions = 0;
        ArrayList<FileInfo> files = new ArrayList<FileInfo>();
        for (Directory dir : dirs) {
            files.addAll(dir.getFiles());
        }
        if (files.size() == 0)
            return 0;
        Collections.sort(files);
        for (int i = 0; i < files.size() - 1; i++) {
            FileInfo file1 = files.get(i);
            FileInfo file2 = files.get(i + 1);
            if (file1.compareTo(file2) == 0) {
                numOfRepetions++;
                file1.getDir().increaseNumOfRepetions();
                Path source1 = file1.getPath();
                // put file id and letter in the filename
                file1.setName(
                        file1.getNum() + letters.get(file1.getDir().getNum()).toString() + "- " + file1.getName());
                Path target1 = subfolders.get(file1.getDir().getNum()).resolve(file1.getName());
                // looks for more repetions
                while (file1.compareTo(file2) == 0) {
                    file2.getDir().increaseNumOfRepetions();
                    Path source2 = file2.getPath();
                    file2.setName(
                            file1.getNum() + letters.get(file2.getDir().getNum()).toString() + "- " + file2.getName());
                    Path target2 = subfolders.get(file2.getDir().getNum()).resolve(file2.getName());
                    try {
                        Files.move(source2, target2);
                    } catch (IOException e) {
                        System.err.println("Failed to move " + file2.getName() + " from "
                                + file2.getDir().getPath().toString() + " to " + target2.toString());
                        e.printStackTrace();
                    }

                    files.remove(file2);
                    try {
                        file2 = files.get(i + 1);
                    } catch (IndexOutOfBoundsException e) {
                        // end of ArrayList reached
                        break;
                    }
                }

                try {
                    Files.move(source1, target1);
                } catch (IOException e) {
                    System.err.println("Failed to move " + file1.getName() + " from "
                            + file1.getDir().getPath().toString() + " to " + target1.toString());
                    e.printStackTrace();
                }
            }
        }
        return numOfRepetions;
    }

    /**
     * same thing of manageInternalFileRepetions, but with the archives
     * 
     * @return if exists internal repetion
     */
    public boolean manageIntenalArchiveRepetions() {
        boolean internalRepetionFound = false;
        for (Directory dir : dirs) {
            ArrayList<Archive> archives = dir.getArchives();
            if (archives.size() == 0)
                break;
            Archive.ArchiveComparator c = archives.get(0).new ArchiveComparator();
            String letter = letters.get(dir.getNum()).toString();
            for (int i = 0; i < archives.size() - 1; i++) {
                Archive archive1 = archives.get(i);
                Archive archive2 = archives.get(i + 1);
                if (c.compare(archive1, archive2) == 0) {
                    internalRepetionFound = true;
                    Path source1 = archive1.getPath();
                    archive1.setName(archive1.getNum() + letter + "- " + archive1.getName());
                    Path target1 = subfolders.get(dirs.indexOf(dir)).resolve(archive1.getName());

                    while (c.compare(archive1, archive2) == 0) {
                        dir.increaseNumOfInternalRepetions();
                        Path source2 = archive2.getPath();
                        archive2.setName(archive1.getNum() + letter + "- " + archive2.getName());
                        Path target2 = subfolders.get(dirs.indexOf(dir)).resolve(archive2.getName());

                        try {
                            Files.move(source2, target2);
                        } catch (IOException e) {
                            System.out.println(
                                    "Error moving " + source2.toString() + " to " + target2.toString() + ": " + e);
                        } finally {
                            archives.remove(archive2);
                        }

                        try {
                            archive2 = archives.get(i + 1);
                        } catch (IndexOutOfBoundsException e) {
                            // if this happens, the end of the collection have been reached
                            break;
                        }
                    }

                    try {
                        Files.move(source1, target1);
                    } catch (IOException e) {
                        System.out
                                .println("Error moving " + source1.toString() + " to " + target1.toString() + ": " + e);
                    }
                }
            }

        }

        return internalRepetionFound;
    }

    /**
     * same thing of manageFileRepetions, but with archives
     * 
     * @return how many repetions were found
     */
    private int manageArchiveRepetions() {
        int numOfRepetions = 0;
        ArrayList<Archive> archives = new ArrayList<Archive>();
        for (Directory dir : dirs) {
            archives.addAll(dir.getArchives());
        }
        if (archives.size() == 0)
            return 0;
        Archive.ArchiveComparator c = archives.get(0).new ArchiveComparator();
        Collections.sort(archives, c);

        for (int i = 0; i < archives.size() - 1; i++) {
            Archive archive1 = archives.get(i);
            Archive archive2 = archives.get(i + 1);
            if (c.compare(archive1, archive2) == 0) {
                numOfRepetions++;
                archive1.getDir().increaseNumOfRepetions();
                archive2.getDir().increaseNumOfRepetions();
                Path source1 = archive1.getPath();
                archive1.setName(archive1.getNum() + letters.get(archive1.getDir().getNum()).toString() + "- "
                        + archive1.getName());
                Path target1 = subfolders.get(archive1.getDir().getNum()).resolve(archive1.getName());

                while (c.compare(archive1, archive2) == 0) {
                    archive2.getDir().increaseNumOfInternalRepetions();
                    Path source2 = archive2.getPath();
                    archive2.setName(archive1.getNum() + letters.get(archive2.getDir().getNum()).toString() + "- "
                            + archive2.getName());
                    Path target2 = subfolders.get(archive2.getDir().getNum()).resolve(archive2.getName());
                    try {
                        Files.move(source2, target2);
                    } catch (IOException e) {
                        System.err.println("Failed to move " + archive2.getName() + " from "
                                + archive2.getDir().getPath().toString() + " to " + target2.toString());
                        e.printStackTrace();
                    }

                    archives.remove(archive2);
                    try {
                        archive2 = archives.get(i + 1);
                    } catch (IndexOutOfBoundsException e) {
                        // end of ArrayList reached
                        break;
                    }
                }

                try {
                    Files.move(source1, target1);
                } catch (IOException e) {
                    System.err.println("Failed to move " + archive1.getName() + " from "
                            + archive1.getDir().getPath().toString() + " to " + target1.toString());
                    e.printStackTrace();
                }
            }
        }
        return numOfRepetions;
    }

    /**
     * the constructor script is executed and then sleep 3,5 seconds (this is done
     * so its possible to read the fail messages and statistics)
     * 
     * @param args The directories to be analyzed
     */
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("usage: <dir> ...");
            System.exit(0);
        }
        new Main(args);
        try {
            Thread.sleep(3500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }
}