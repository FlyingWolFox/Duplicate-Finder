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
 * @version 0.9.0-3
 * @author FlyingWolFox / lips.pissaia@gmail.com
 */
public class Main {
	private ArrayList<Directory> dirs; // where all directories are stored
	private ArrayList<FileInfo> files; // where all files will be stored for comparasion
	private ArrayList<Path> subfolders; // stores the subfolders of the Results folder
	private ArrayList<Path> internalRepetions; // used in internal repetion handling, this
	// still in the works
	private HashMap<Integer, Character> letters; // used to translate the directory number in a letter to use in the
													// filenames

	/**
	 * main contructor, here the script is executed
	 * 
	 * @param args String collction of paths to folders to compare
	 */
	public Main(String[] args) {
		dirs = new ArrayList<Directory>();
		files = new ArrayList<FileInfo>();
		subfolders = new ArrayList<Path>();
		internalRepetions = new ArrayList<Path>(); // not used yet
		letters = new HashMap<Integer, Character>(26);
		setLetters(); // fills the HashMap

		// creates Directory objects to hold directory information
		for (String path : args) {
			try {
				Directory dir = new Directory(path); // hrows exception if failed to process the directory and the files
				dirs.add(dir); // adds the newly directory to the dir collection
			} catch (IOException | DirectoryIteratorException x) {
				System.out.println("Failed to add " + path + " for analyzing");
				x.printStackTrace();
			}
		}

		// creates the Results directory and its subfolders
		createDirectories();

		// this should always be true
		assert subfolders.size() == dirs
				.size() : "Error: The number of subdirectories is not equal to the number of scanned directories";

		// process internal repetions first. At this version this just logs that
		// repetions have been found
		if (manageInternalRepetions()) {
			System.out.println("Internal repetions found:");
			for (Directory dir : dirs) {
				System.out.println(
						dir.getNumOfInternalRepetions() + " internal repetions in " + dir.getPath().toString());
			}
			System.out.println(
					"Internal are in their respective directories. Clean then first then run the tool again");
			return;
		} else
			System.out.println("No internal repetions found");
		System.out.println("");

		// will get all files of all directories to make comparisons
		for (

		Directory dir : dirs) {
			files.addAll(dir.getFiles());
		}
		Collections.sort(files); // sort the arraylist accordingly to MD5 hash in alphabetical order

		// will be looking for repetions and will organize everything
		manageRepetions();
		// moveRepeated(); deactivated, useless for now

		System.out.println("Operation Complete");

	}

	/**
	 * will fill the HashMap to usage in conversion of the directory number to a
	 * letter, here lies the max limit of folders to be analyzed. Fills with
	 * integers and it respectevely letter, for example 0-a, 1-b, etc
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
	 * names of the folders to analyze
	 * 
	 * @param num number of subdirectories to create (maybe not really needed)
	 */
	private void createDirectories() {
		Path results = Paths.get("Results"); // holds the Results directory as root for subdirectories
		try {
			Files.createDirectories(results); // creates the Results directory
		} catch (IOException e) {
			// not being able to create the Results directory is terminal and the script
			// mustn't proceed
			System.err.println("Error trying to create Results directory");
			e.printStackTrace();
			System.exit(-1);
		}
		// will create all the subfolders
		for (int i = 0; i < Directory.getX(); i++) {
			// Path child = Paths.get("Results" + File.separatorChar +
			// dirs.get(i).getPath().getFileName());
			Path subfolder = results.resolve(dirs.get(i).getPath().getFileName());
			this.subfolders.add(subfolder); // add the subdirectory to the collection
			try {
				Files.createDirectories(subfolder);
			} catch (IOException e) {
				// a fail to ccreate a sbudirectory is terminal, the script mustn't proceed
				System.err.println("Error trying to create directory for " + dirs.get(i).getPath().getFileName());
				e.printStackTrace();
				System.exit(-1);
			}
		}
	}

	/**
	 * Will find internal repetions in the directories in th same style of
	 * manageRepetions
	 * 
	 * @return if a internal repetion was found
	 */
	private boolean manageInternalRepetions() {
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
	 * Will find repetions among files and will organize them. The way to find
	 * repetions is simple: the collection is sorted based in the hash, so if two or
	 * more files have the same hash, they'll be togheter, using this, organization
	 * is also easier
	 */
	private void manageRepetions() {
		int numOfRepetions = 0; // stores the number of repetions of all directories, every repetion found will
								// increase this
		for (int i = 0; i < files.size() - 1; i++) {
			FileInfo file1 = files.get(i); // get a first file to compare
			FileInfo file2 = files.get(i + 1); // get a second file to compare
			if (file1.compareTo(file2) == 0) { // compares the files
				numOfRepetions++;
				file1.getDir().increaseNumOfRepetions(); // increases the number of repetion on the folder of the file
				Path source1 = file1.getPath(); // gets directory info to move the file
				file1.setName(
						file1.getNum() + letters.get(file1.getDir().getNum()).toString() + "- " + file1.getName());
				Path target1 = subfolders.get(file1.getDir().getNum()).resolve(file1.getName()); // gets the respective
																									// subfolder to move
																									// the
																									// file with the new
																									// name
				// continue to look for repetions, this will garant that not just repetion
				// doubles get spotted
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

					files.remove(file2); // remove the file of the collection, so comparasion can proceed without
											// getting
											// a integer iterator
					try {
						file2 = files.get(i + 1);
					} catch (IndexOutOfBoundsException e) {
						// if this happens, the end of the collection have been reached
						break;
					}
				}

				try {
					// finally moves the file used to compare with proper naming
					Files.move(source1, target1);
				} catch (IOException e) {
					System.err.println("Failed to move " + file1.getName() + " from "
							+ file1.getDir().getPath().toString() + " to " + target1.toString());
					e.printStackTrace();
				}
				// removes from the collection, this means that the iteration have to be
				// subtract one so a start of a group of repetions isn't missed, this may be
				// unnecessary
			}
		}
		// prints the statics of the reptions found
		System.out.println(numOfRepetions + " repetions found:");
		for (Directory dir : dirs) {
			System.out.println(dir.getNumOfRepetions() + " files in " + dir.getPath().toString());
		}
		System.out.println("");

	}

	/**
	 * main method. The script will execute the code in the Main class constructor
	 * and then sleep 3,5 seconds (this is done so its possible to read the fail
	 * messages and statistics)
	 * 
	 * @param args The directories to be analyzed
	 */
	public static void main(String[] args) {
		if (args.length < 1) {
			System.out.println("	usage: <dir> ...");
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