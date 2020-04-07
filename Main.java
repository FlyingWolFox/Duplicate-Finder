import java.io.File;
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
 * @version 0.9.0-1
 * @author FlyingWolFox / lips.pissaia@gmail.com
 */
public class Main {
	private ArrayList<Directory> dirs; // where all directories are stored
	private ArrayList<ROM> roms; // where all files will be stored for comparasion
	private ArrayList<Path> childs; // stores the subfolders of the Results folder
	private ArrayList<Path> internals; // used in internal repetion handling, this
										// still in the works
	private Path root; // stores the Reuslts folder path
	private HashMap<Integer, Character> letters; // used to translate the directory number in a letter to use in the
													// filenames

	/**
	 * main contructor, here the script is executed
	 * 
	 * @param args String collction of paths to folders to compare
	 */
	public Main(String[] args) {
		dirs = new ArrayList<Directory>();
		roms = new ArrayList<ROM>();
		childs = new ArrayList<Path>();
		internals = new ArrayList<Path>(); // not used yet
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
		createDirectories(Directory.getX());

		// this should always be true
		assert childs.size() == dirs
				.size() : "Error: The number of child directories is not equal to the number of scanned directories";

		// process internal repetions first. At this version this just logs that
		// repetions have been found
		getRidInternalRepetions();

		// will get all files of all directories to make comparisons
		for (Directory dir : dirs) {
			roms.addAll(dir.getROMs());
		}
		Collections.sort(roms); // sort the arraylist accordingly to MD5 hash in alphabetical order

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
	private void createDirectories(int num) {
		// TODO: use root to get child paths
		// TODO: check ig param num is necessary
		Path root = Paths.get("Results"); // holds the Results directory as root for subdirectories
		this.root = root;
		try {
			Files.createDirectories(root); // creates the Results directory
		} catch (IOException e) {
			// not being able to create the Results directory is terminal and the script
			// mustn't proceed
			System.err.println("Error trying to create Results directory");
			e.printStackTrace();
			System.exit(-1);
		}
		// will create all the subfolders
		for (int i = 0; i < num; i++) {
			Path child = Paths.get("Results" + File.separatorChar + dirs.get(i).getPath().getFileName());
			this.childs.add(child); // add the subdirectory to the collection
			try {
				Files.createDirectories(child);
			} catch (IOException e) {
				// a fail to ccreate a sbudirectory is terminal, the script mustn't proceed
				System.err.println("Error trying to create directory for " + dirs.get(i).getPath().getFileName());
				e.printStackTrace();
				System.exit(-1);
			}

			// TODO: better internal repetion handling
			/*
			 * Path internal = child.resolve("Internal Repetions"); internals.add(internal);
			 * try { Files.createDirectories(internal); } catch (IOException e) {
			 * System.err.
			 * println("Error trying to create \"Internal Repetions\" directory for " +
			 * dirs.get(i).getPath().getFileName()); e.printStackTrace(); }
			 */
		}
	}

	/**
	 * Gets rid of internal repetions of each directory, making organization better
	 * and comparing among folders faster. Deactived for now
	 */
	private void getRidInternalRepetions() {
		// TODO: getRidInternalRepetions
		// this flag will change to show internal repetions on the terminal
		boolean repetion = false;
		for (int dir = 0; dir < dirs.size(); dir++) {
			ArrayList<ROM> roms = dirs.get(dir).getROMs();
			// will search for internal repetions
			for (int i = 0; i < roms.size() - 1; i++) {
				ROM rom1 = roms.get(i);
				ROM rom2 = roms.get(i + 1);
				if (rom1.compareTo(rom2) == 0) {
					dirs.get(dir).increaseNumOfInternalRepetions();
					/*
					 * Path source = rom2.getPath(); Path target = internals.get(dir).resolve(
					 * rom1.getNum() + letters.get(dirs.get(dir).getNum()).toString() + "- " +
					 * rom2.getName()); try { Files.move(source, target); } catch (IOException e) {
					 * System.err.println("Failed to move " + rom2.getName() + " from " +
					 * dirs.get(dir).getPath().toString() + " to " + target.toString());
					 * e.printStackTrace(); }
					 * 
					 * roms.remove(rom2); i--; rom1.setRepeated(true);
					 */
					repetion = true; // if a repetion is found, set the flag
				}

			}
		}

		// print internal repetion stats and the non handling warning
		if (repetion) {
			System.out.println("Internal repetions found:");
			for (Directory dir : dirs) {
				System.out.println(
						dir.getNumOfInternalRepetions() + " internal repetions in " + dir.getPath().toString());
			}
			System.out.println(
					"Internal repetions aren't handled in this version, so they're togheter with the other repetions");
		} else
			System.out.println("No internal repetions found");
		System.out.println("");
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
		for (int i = 0; i < roms.size() - 1; i++) {
			ROM rom1 = roms.get(i); // get a first file to compare
			ROM rom2 = roms.get(i + 1); // get a second file to compare
			if (rom1.compareTo(rom2) == 0) { // compares the files
				numOfRepetions++;
				rom1.getDir().increaseNumOfRepetions(); // increases the number of repetion on the folder of the file
				Path source1 = rom1.getPath(); // gets directory info to move the file
				Path target1 = childs.get(rom1.getDir().getNum()).resolve(
						rom1.getNum() + letters.get(rom1.getDir().getNum()).toString() + "- " + rom1.getName()); // gets
																													// the
																													// respective
																													// subfolder
																													// to
																													// move
																													// the
																													// file
																													// with
																													// the
																													// new
																													// name
				// continue to look for repetions, this will garant that not just repetion
				// doubles get spotted
				while (rom1.compareTo(rom2) == 0) {
					rom2.getDir().increaseNumOfRepetions();
					Path source2 = rom2.getPath();
					Path target2 = childs.get(rom2.getDir().getNum()).resolve(
							rom1.getNum() + letters.get(rom2.getDir().getNum()).toString() + "- " + rom2.getName());
					try {
						Files.move(source2, target2);
					} catch (IOException e) {
						System.err.println("Failed to move " + rom2.getName() + " from "
								+ rom2.getDir().getPath().toString() + " to " + target2.toString());
						e.printStackTrace();
					}

					roms.remove(rom2); // remove the file of the collection, so comparasion can proceed without getting
										// a integer iterator
					try {
						rom2 = roms.get(i + 1);
					} catch (IndexOutOfBoundsException e) {
						// if this happens, the end of the collection have been reached
						break;
					}
				}

				try {
					// finally moves the file used to compare with proper naming
					Files.move(source1, target1);
				} catch (IOException e) {
					System.err.println("Failed to move " + rom1.getName() + " from "
							+ rom1.getDir().getPath().toString() + " to " + target1.toString());
					e.printStackTrace();
				}
				// removes from the collection, this means that the iteration have to be
				// subtract one so a start of a group of repetions isn't missed, this may be
				// unnecessary
				// TODO: see if it's necesary to remove and move the iteration back
				roms.remove(rom1);
				i--;
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
	 * Used to move internal repetions that weren't moved. Not used by now because
	 * internal repetions aren't handled seperetedly
	 */
	public void moveRepeated() {
		for (ROM rom : roms) {
			if (rom.getRepeated()
					&& rom.getFile().getAbsolutePath().equals(rom.getPath().toAbsolutePath().toString())) {
				Path source = rom.getPath();
				Path target = internals.get(rom.getDir().getNum())
						.resolve(rom.getNum() + letters.get(rom.getDir().getNum()).toString() + "- " + rom.getName());
				try {
					Files.move(source, target);
				} catch (IOException e) {
					System.err.println("Failed to move " + rom.getName() + " from " + rom.getDir().getPath().toString()
							+ " to " + target.toString());
					e.printStackTrace();
				}
			}
			if (rom.getRepeated()
					&& !rom.getFile().getAbsolutePath().equals(rom.getPath().toAbsolutePath().toString())) {
				System.out.println(rom.getName() + " from " + rom.getDir().getPath().toString()
						+ " was among internal repetions, but it was found among repetions with other directories, "
						+ "so it isn't with the internal repetions of its directory, but in the "
						+ rom.getDir().getPath().toFile().getName() + " directory in the results directory");
			}
		}
	}

	/**
	 * main method. The script will execute the code in the Main class constructor
	 * and then sleep 2 seconds (this is done so its possible to read the fail
	 * maessages and statistics)
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
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.exit(0);
	}
}