import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ROM implements Comparable<ROM> {
	private File file;
	private String name;
	private Path path;
	private String md5;
	private Directory dir;
	private int num;
	private boolean repeated;
	private static int y;

	static {
		y = 0;
	}

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

	public File getFile() {
		return this.file;
	}

	public Path getPath() {
		return this.path;
	}

	public String getMD5() {
		return this.md5;
	}

	public Directory getDir() {
		return dir;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNum() {
		return num;
	}

	public boolean getRepeated() {
		return repeated;
	}

	public void setRepeated(boolean repeated) {
		this.repeated = repeated;
	}

	@Override
	public int compareTo(ROM rom) {
		return md5.compareTo(rom.getMD5());
	}

}