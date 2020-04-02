import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class Main {
	private ArrayList<Directory> dirs;
	private ArrayList<ROM> roms;
	private ArrayList<Path> childs;
	private ArrayList<Path> internals;
	private Path root;
	private HashMap<Integer, Character> letters;

	public Main(String[] args) {
		dirs = new ArrayList<Directory>();
		roms = new ArrayList<ROM>();
		childs = new ArrayList<Path>();
		internals = new ArrayList<Path>();
		letters = new HashMap<Integer, Character>(26);
		setLetters();

		for (String path : args) {
			try {
				Directory dir = new Directory(path);
				dirs.add(dir);
			} catch (IOException | DirectoryIteratorException x) {
				x.printStackTrace();
				System.err.println(x);
			}
		}
		createDirectories(Directory.getX());

		assert childs.size() == dirs
				.size() : "Error: The number of child directories is not equal to the number of scanned directories";

		getRidInternalRepetions();

		for (Directory dir : dirs) {
			roms.addAll(dir.getROMs());
		}
		Collections.sort(roms);

		manageRepetions();
		//moveRepeated();

		System.out.println("Operation Complete");
	}

	private void setLetters() {
		char c = 'a';
		int i = 0;
		for (; c <= 'z'; c++, i++) {
			letters.put(i, c);
		}
	}

	private void createDirectories(int num) {
		Path root = Paths.get("Results");
		this.root = root;
		try {
			Files.createDirectories(root);
		} catch (IOException e) {
			System.err.println("Error trying to create Results directory");
			e.printStackTrace();
			System.exit(-1);
		}
		for (int i = 0; i < num; i++) {
			Path child = Paths.get("Results" + File.separatorChar + dirs.get(i).getPath().getFileName());
			this.childs.add(child);
			try {
				Files.createDirectories(child);
			} catch (IOException e) {
				System.err.println("Error trying to create directory for " + dirs.get(i).getPath().getFileName());
				e.printStackTrace();
			}
			/*
			Path internal = child.resolve("Internal Repetions");
			internals.add(internal);
			try {
				Files.createDirectories(internal);
			} catch (IOException e) {
				System.err.println("Error trying to create \"Internal Repetions\" directory for "
						+ dirs.get(i).getPath().getFileName());
				e.printStackTrace();
			}
			*/
		}
	}

	// TODO: getRidInternalRepetions
	private void getRidInternalRepetions() {
		boolean repetion = false;
		for (int dir = 0; dir < dirs.size(); dir++) {
			ArrayList<ROM> roms = dirs.get(dir).getROMs();
			for (int i = 0; i < roms.size() - 1; i++) {
				ROM rom1 = roms.get(i);
				ROM rom2 = roms.get(i + 1);
				if (rom1.compareTo(rom2) == 0) {
					dirs.get(dir).increaseNumOfInternalRepetions();
					/*
					Path source = rom2.getPath();
					Path target = internals.get(dir).resolve(
							rom1.getNum() + letters.get(dirs.get(dir).getNum()).toString() + "- " + rom2.getName());
					try {
						Files.move(source, target);
					} catch (IOException e) {
						System.err.println("Failed to move " + rom2.getName() + " from "
								+ dirs.get(dir).getPath().toString() + " to " + target.toString());
						e.printStackTrace();
					}

					roms.remove(rom2);
					i--;
					rom1.setRepeated(true);
					*/					
					repetion = true;
				}

			}
		}

		if (repetion) {
			System.out.println("Internal repetions found:");
			for (Directory dir : dirs) {
				System.out.println(
						dir.getNumOfInternalRepetions() + " internal repetions in " + dir.getPath().toString());
			}
			System.out.println("Internal repetions aren't handled in this version, so they're togheter with the other repetions");
		} else
			System.out.println("No internal repetions found");
		System.out.println("");
	}

	private void manageRepetions() {
		int numOfRepetions = 0;
		for (int i = 0; i < roms.size() - 1; i++) {
			ROM rom1 = roms.get(i);
			ROM rom2 = roms.get(i + 1);
			if (rom1.compareTo(rom2) == 0) {
				numOfRepetions++;
				rom1.getDir().increaseNumOfRepetions();
				Path source1 = rom1.getPath();
				Path target1 = childs.get(rom1.getDir().getNum()).resolve(
						rom1.getNum() + letters.get(rom1.getDir().getNum()).toString() + "- " + rom1.getName());
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

					roms.remove(rom2);
					try {
						rom2 = roms.get(i + 1);
					} catch (IndexOutOfBoundsException e) {
						break;
					}
				}

				try {
					Files.move(source1, target1);
				} catch (IOException e) {
					System.err.println("Failed to move " + rom1.getName() + " from "
							+ rom1.getDir().getPath().toString() + " to " + target1.toString());
					e.printStackTrace();
				}
				roms.remove(rom1);
				i--;
			}
		}
		System.out.println(numOfRepetions + " repetions found:");
		for (Directory dir : dirs) {
			System.out.println(dir.getNumOfRepetions() + " files in " + dir.getPath().toString());
		}
		System.out.println("");

	}

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