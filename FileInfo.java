import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A file. Responsible to calculate hash. To know more info:
 * https://github.com/FlyingWolFox/Duplicate-Finder
 * 
 * @version 1.3-beta
 * @author FlyingWolFox
 */
public class FileInfo implements Comparable<FileInfo> {
    private File file;
    private String name;
    private Path path;
    private String hash;
    private String lastModified;
    private Directory dir; // directory that the file is in
    private int num; // the file id in this directory
    private static int y; // used to generate file ids

    static {
        y = 0;
    }

    /**
     * main constructor. Initialize all variable and calculates the hash
     * 
     * @param file // the file
     * @param dir  // the directory it's in
     */
    public FileInfo(File file, Directory dir) {
        this.file = file;
        this.path = file.toPath();
        this.num = y;
        this.dir = dir;
        this.name = file.getName();
        this.lastModified = String.valueOf(file.lastModified());
        // System.out.print(String.format(":.. %s", name));
        try {
            calculateHash();
            // System.out.println(" [OK]");
        } catch (IOException e) {
            System.out.print(String.format(":.. %s", name));
            System.out.println(" [ERROR] " + e);
        }
        y++;
    }

    public FileInfo(Path path, String hash, String lastModified, Directory dir) {
        this.file = new File(path.toString());
        this.path = path;
        this.num = y;
        this.dir = dir;
        this.name = path.getFileName().toString();
        this.lastModified = lastModified;
        this.hash = hash;
        y++;
    }

    /**
     * calculates the hash of the file
     * 
     * @throws IOException if file read fails
     */
    public void calculateHash() throws IOException {
        InputStream fis = new FileInputStream(path.toString());

        byte[] buffer = new byte[1024];
        MessageDigest complete = null;
        try {
            complete = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.print("Failed to get MD5 hash algorithm");
            e.printStackTrace();
            System.exit(10);
        }

        int numRead;

        do {
            numRead = fis.read(buffer);
            if (numRead > 0) {
                complete.update(buffer, 0, numRead);
            }
        } while (numRead != -1);

        fis.close();

        byte[] digest = complete.digest();

        // This bytes[] has bytes in numeric format;
        // Convert it to hexadecimal string
        hash = getStringHash(digest);
    }

    /**
     * Coverts byte array to an hexadecimal string
     * 
     * @param digest byte array with the hash
     * @return the hexadecimal string
     */
    public static String getStringHash(byte[] digest) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    /**
     * @return this pathname
     */
    public File getFile() {
        return this.file;
    }

    /**
     * @return the path of the file
     */
    public Path getPath() {
        return this.path;
    }

    /**
     * @return the hash of the file
     */
    public String getHash() {
        return this.hash;
    }

    /**
     * Sets the hash of this file. Used to convert archives with single file
     * 
     * @param hash new hash of the file
     */
    protected void setHash(String hash) {
        if (this.hash.length() == hash.length())
            this.hash = hash;
    }

    /**
     * @return the directory of the file
     */
    public Directory getDir() {
        return dir;
    }

    /**
     * @return the name of the file
     */
    public String getName() {
        return name;
    }

    /**
     * Set a new name for the file.
     * 
     * @param name new filename
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the file number
     */
    public int getNum() {
        return num;
    }

    /**
     * @return file's last modified date
     */
    public String getLastModified() {
        return lastModified;
    }

    /**
     * @param lastModified new date
     */
    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }

    @Override
    public int compareTo(FileInfo file) {
        // compare based on hash
        return hash.compareTo(file.getHash());
    }

}
