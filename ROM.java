import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class holds file information. The name is subject to change. To know
 * more info: https://github.com/FlyingWolFox/Duplicate-Finder
 * 
 * @version 0.9.0-1
 * @author FlyingWolFox / lips.pissaia@gmail.com
 */
public class ROM implements Comparable<ROM> {
	private File file; // file information
	private String name; // filename
	private Path path; // file path
	private String md5; // md5 hash
	private Directory dir; // directory that the file is in
	private int num; // the file id in this directory
	private boolean repeated; // flag to be used in internal repetion handling
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
	public ROM(File file, Directory dir) {
		this.file = file;
		this.path = file.toPath();
		this.num = y;
		this.dir = dir;
		this.name = file.getName();
		try {
			calculateMD5();
		} catch (NoSuchAlgorithmException | IOException e) {
			e.printStackTrace();
		}
		y++;
		System.out.println("	MD5: " + md5);
	}

	// calculates the md5 hash of this file
	public void calculateMD5() throws NoSuchAlgorithmException, IOException {
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
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < digest.length; i++) {
			sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
		}

		md5 = sb.toString();
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
	public String getMD5() {
		return this.md5;
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
		// TODO: use this to set the new filename
		this.name = name;
	}

	/**
	 * @return the file number
	 */
	public int getNum() {
		return num;
	}

	/**
	 * @return if the file is part of internal repetions
	 */
	public boolean getRepeated() {
		return repeated;
	}

	/**
	 * Set that this file is part of internal repetions
	 */
	public void setRepeated() {
		this.repeated = true;
	}

	@Override
	/**
	 * Used to compare files to find repetions
	 */
	public int compareTo(ROM rom) {
		return md5.compareTo(rom.getMD5());
	}

}