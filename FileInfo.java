import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class holds file information. To know more info:
 * https://github.com/FlyingWolFox/Duplicate-Finder
 * 
 * @version 0.9.0-3
 * @author FlyingWolFox / lips.pissaia@gmail.com
 */
public class FileInfo implements Comparable<FileInfo> {
	private File file; // file information
	private String name; // filename
	private Path path; // file path
	private String hash;
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
		try {
			calculateHash();
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
		y++;
		System.out.println("    MD5: " + hash);
	}

	// calculates the md5 hash of this file
	public void calculateHash() throws NoSuchAlgorithmException, IOException {
		InputStream fis = new FileInputStream(path.toString());

		byte[] buffer = new byte[1024];
		MessageDigest complete = MessageDigest.getInstance("MD5");

		int numRead;

		do {
			numRead = fis.read(buffer);
			if (numRead > 0) {
				complete.update(buffer, 0, numRead);
			}
		} while (numRead != -1);

		fis.close();

		byte[] digest = complete.digest();

		// This bytes[] has bytes in decimal format;
		// Convert it to hexadecimal format
		hash = getStringHash(digest);
	}

	public static String getStringHash(byte[] digest) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digest.length; i++) {
			sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();
	}

	/**
	 * @return this file
	 */
	public File getFile() {
		return this.file;
	}

	/**
	 * @return the path of this file
	 */
	public Path getPath() {
		return this.path;
	}

	/**
	 * @return the md5 hash of this file
	 */
	public String getHash() {
		return this.hash;
	}

	
	protected void setHash(String hash) {
		if (this.hash.length() == hash.length()) 
			this.hash = hash;
	}

	/**
	 * @return the directory of this file
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
	 * Set a new name for th file. To be used in renaming the file
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

	@Override
	/**
	 * Used to compare files to find repetions
	 */
	public int compareTo(FileInfo file) {
		return hash.compareTo(file.getHash());
	}

}
