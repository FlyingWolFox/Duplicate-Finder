# Duplicate Finder

Duplicate finder is a tool (like a script) that will serach for repetions on directories given by argument using MD5 hash.
This tool uses the terminal to operate and the windows executable is wrapped by JSmooth.

## How to use

### Calling the tool

* Using the .class or .jar:

  - Call the Main class and pass the directories that you want to analyze as arguments. The tool, for now, ignores subdirectories. The tool will print in the terminal what files it's analizing and, after done, will print the results:
    - 1. Internal repetions: Internal repetions are files that have a equal copy in the same directory that they're in. The tool will show if it found at least a repetion and, if it does, will print how many are in each and end execution. You'll have to handle them yourself and then re-run the script.
    - 2. Repetions: Here enters repetions among directories. The tool will print how many repetions were found on each directory.
    - 3. The tool will print `Operation Complete` after the execution. If any exceptions are raised while handling, the description of the error will be printed with the stack trace.

* Using the executable:

  - Call it by terminal with the directories to analyze as arguments or drag and drop the directory to analyze over the executable. The results are printed like the terminal and the windows will remain open by two seconds. You can add the `Press Enter to continue` message using a option on the wrapper. On JSmooth, the opition is in Skeleton Properties.

### Finiding the results directory

If you ran the tool in the terminal, the `Results` directory is in the local that the terminal is. On windows, for example, if you open the CMD without using admin rights in the start menu, the directory is your user directory.

If you dragged and dropped in the executable, the directory is where you selected the directories. For example, you selected the directories /Parent/Dir1/ and /Parent/Dir2/ , the `Results` directory is in the parent directory, /Parent/Results .

### Using the results directory

The `Results` directory will have a number of subdriectories in it, the same number of directories you selected to analyse. Each subdirectory contains the repetions of each folder analysed, having the same name of it. The files are renamed to facilitate user interaction. Each file is renamed following these rules:
  - 1. The file start with a number, this number is a identification of which group of repetions it's part of. For example, all files that start with the number 48 have the same content
  - 2. After the number comes a letter, which range from `a` to `z` and represent the directory that they came. For example, if you passed ./Dir1/ and ./Dir2/, in this order, the files in ./Results/Dir1/ will all have the letter `a` after the number, and ./Results/Dir2 will all have the letter `b` after the number. This is for identification purposes, if you want to merge all directories to compare files. Here lies the limit of this tool, only support a max of 26 directories, after that, or there will be no letters, or a exception will raise with the HashMap
  - 3. After the lettter comes a dash (`-`) and a blank space. This is to preserve the file name and make easy to rename the files after (that you can use my [renamer tool for this script](https://github.com/FlyingWolFox/Duplicate-Finder-Renamer/) or something like Bulk Renamer Utility)
  - 4. Then comes the file name and extension, preserving the original name, but I'm not sure about symbolic links

PS: Intenal repetions are handle first. If the tool finds, it won't look for repetions among directories. Internal repetions are put in the subfolder which they're found. Manage them first, them re-run the tool to look for repetions among directories

### How it works

First the tool get all files in the folders passed by argument, creating Directory objects and storing the files in a ArrayList in each object. Files are stored in ROM objects (name subject to change), which in its creation already calculates the MD5 hash. After that it'll look for internal repetions in the folders, which, for now, doesn't do anything, except print in the terminal. After that, it'll get each ArrayList of files of each directory and store in another ArrayList in the Main class and will sort it, using the MD5 hash as reference, and will search for repetion loking at subsequent itens in the ArrayList. When it finds a repetion, it'll move all repeated files, renaming them in the process. The tool try to be as verbose as possible to give an idea of what it's doing, this may hit perfomance.

## About the code

This tool uses MD5 hash to verify if files are equal and uses `java.nio` and `java.security`. Not all of these are available in all Java versions, like Java ME or old version of Java. PS: The hash calculations were optimized after Java 7, so they run much faster

## About FastMD5

Theres a libray in the internet called FastMD5. It's a fast and super optimizaded to run as quickly as possible. It wasn't implanted in this tool however, since after Java 7, the speed is comparable with the java library, with Java running faster with samller files and FastMD5 with bigger files. The difference isn't that big, so I didn't bother using it. This tool uses MessageDigest to calculate MD5 hash

## FAQ

### Users

1. **This will delete repeated files?**

   No, it'll organize it. You choose if you want to delete

2. **So how I get rid of duplicates?**

   You can just delete all the subdirectories in `Results` except one (since there's no internal repetion) and rename the files yourself

3. **There's a way to rename them quickly?**

   You can use my [renamer tool here](https://github.com/FlyingWolFox/Duplicate-Finder-Renamer/) that is made to work with this tool, use another tool like Bulk Renaming Utility or a shell script/batch file

4. **The tools rise an exception/error, what should I do?**

   Read the exception message to see if you can fix yourself. Things like passing something that is not a directory as arguments will raise an exception because there's no verification to see if the argument is a valid directory (yet)

5. **I get acess denied, how I fix it?**

   The tool is probably trying to acess a protected directory (like a system directory or a directory that just a administrator can acess). Run the tool as admin and probably it'll fix it. This exception may appear too if you pass like a file as argument

6. **My folder aren't togheter in the same parent, can I use a shortcut in one of the parents?**

   No, because the shortcut acts like a file, so it won't work. This may be implemented in the future tho. While, this doesn't happen, you can use the terminal like this: `DuplicateFinder <directory path> <directory path> ...`

7. **There's a min and a max number of folders to analyse?**

   The min is one, in which it'll just look for internal repetions, the max is designed to be 26, after that the tool will raise an exception (error) or will stop to put letters in the filenames in the 27th and beyond folders

8. **I've found taht something is going wrong, how can I help/get a solution**

   You can always create an Issue to help improve this tool. I just ask you to look at all issues to see if your questions/problem hasn't been answered/solved

### Other programers

1. **Can I use your code in my project?**

Yes! This code is under the MIT license, so you're free to do anything with the code. You can also fork the repository.

2. **How can I help?**

You can help createing an Issue or a Pull Request, I'll look into it, I promise!

3. **I want to put FastMD5 in the code, where I change?**

The method `calculateHash()` in the FileInfo class is responsible to calculate the hash so any modifications in the hash calculation goes there. The method changes the `String hash` variable to contain the hash.

4. **Why not use other hashing algorithm or use multiple ones?**

I choosed MD5 because is relatively colision safe when looking for repeated files and it's fast. There was other alternatives, like SHA-1, that's fast too, but MD5 was good enough. Other hashes like SHA-2 or SHA-3 weren't considered because they're really slow. Other non-security algorithms weren't considered because I didn't know they existed until sometime ago :D You can put any hash you want in the code and it'll work.

5. **Everything is in the Main class in a weird way, how the code is designed?**

This is because this tool is more like a script, so things went to the Main classes. The code works basically in the constructors, that does almost everything. This is just to be pratic, feel free to change it (and even submmit a Pull Request!)

## Future plans

This tool isn't finished, not for me at least, so I'm planning to do:

- [ ] Implement a compressed archived comparer
- [ ] Better verbosity

Also I'm planning to make a GUI version of this tool (when I learn how to make GUIs) to be even better! It'll be in another repository tho. I'll update here when I finish it
