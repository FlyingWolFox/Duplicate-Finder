# Duplicate Finder

Duplicate finder is a tool (like a script) that will serach for repetions on directories given by argument using MD5 hash. It'll scan files and archives (but not subdirectories). Archives with more than one file are compared with others of the same kind. Other files and archives with one file are compared toghter. In this case the archive will be compared based on the file inside. The filename, its hash(es) and last modified date is cached for better speed. Needs [7-Zip-JBinding](http://sevenzipjbind.sourceforge.net/index.html) and [JDOM 2.0.6](http://www.jdom.org/)
This tool uses the terminal to operate and the windows executable is wrapped by JSmooth.

## How to use

### Calling the tool

* Using the .class or .jar:

  - Call the Main class and pass the directories that you want to analyze as arguments. The tool ignores subdirectories. The tool will print a progress bar showing the progress, after done, will print the results:
    - 1. Internal repetions: Internal repetions are files that have a equal copy in the same directory that they're in. The tool will show if it found at least a repetion and, if it does, will print how many are in each and end execution. You'll have to handle them yourself and then re-run the script.
    - 2. Repetions: Here enters repetions among directories. The tool will print how many repetions were found on each directory.
    - 3. The tool will print `Operation Complete` after the execution. If any exceptions are raised while handling, the description of the error will be printed with the stack trace.

* Using the executable:

  - Call it by terminal with the directories to analyze as arguments or drag and drop the directory to analyze over the executable. The results are printed like the terminal and the windows will remain open by two seconds. You can add the `Press Enter to continue` message using a option on the wrapper. On JSmooth, the option is in Skeleton Properties.

### Finiding the results directory

If you ran the tool in the terminal, the `Results` directory is in the local that the terminal is. On windows, for example, if you open the CMD without using admin rights in the start menu, the directory is your user directory.

If you dragged and dropped in the executable, the directory is where you selected the directories. For example, you selected the directories /Parent/Dir1/ and /Parent/Dir2/ , the `Results` directory is in the parent directory, /Parent/Results .

### Using the results directory

The `Results` directory will have a number of subdriectories in it, the same number of directories you selected to analyse. Each subdirectory contains the repetions of each directory analysed, having the same name of it. The files are renamed to facilitate user interaction. Each file is renamed following these rules:
  - 1. The file start with a number, this number is a identification of which group of repetions it's part of. For example, all files that start with the number 48 have the same content
  - 2. After the number comes a letter, which range from `a` to `z` and represent the directory that they came. For example, if you passed ./Dir1/ and ./Dir2/, in this order, the files in ./Results/Dir1/ will all have the letter `a` after the number, and ./Results/Dir2 will all have the letter `b` after the number. This is for identification purposes, if you want to merge all directories to compare files. Here lies the limit of this tool, only support a max of 26 directories, after that, or there will be no letters, or a exception will raise with the HashMap
  - 3. After the lettter comes a dash (`-`) and a blank space. This is to preserve the file name and make easy to rename the files after (that you can use my [renamer tool for this script](https://github.com/FlyingWolFox/Duplicate-Finder-Renamer/) or something like Bulk Renamer Utility)
  - 4. Then comes the file name and extension, preserving the original name, but I'm not sure about symbolic links

PS: Intenal repetions are handle first. If the tool finds, it won't look for repetions among directories. Internal repetions are put in the subdirectories which they're found. Manage them first, them re-run the tool to look for repetions among directories

### The cache and .cache directory

The tool will cache all directories that are analyzed, storing name, hash (also hashes for archive) and last modified date. It helps speedup the analysis process. All cache entries are validatied on run. If a file was modified, it'll be rehashed and have its cache entry updated.The `Results` directory will have a `.cache` directory that will have inside `.xml` files. Those are the cache for the directories scanned.

### How it works

First the tool get all files and archives in the directories passed by argument. The directories files will have their hashes calculated and archives are decompressed and their files' hash calculated. Archives with only one file are treated as a normal file. After that it'll look for internal repetions in the directories, comparing archives separately from the files. Archives with a single file are treated like a common file. After that it'll search for repetion among directories, with archives being treated separately. To do both of things, files and archives are sorted by their hashes, with this, files and archives with the same hash will be togheter in the list. When it finds a repetion, it'll move all repeated files, renaming them in the process.

## About the code

This tool uses MD5 hash to verify if files are equal and uses `java.nio` and `java.security`. Not all of these are available in all Java versions, like Java ME or old version of Java. PS: The hash calculations were optimized after Java 7, so they run much faster. Also, this tool needs 7-Zip-JBinding library to handle archives, you can get it [here](http://sevenzipjbind.sourceforge.net/index.html), and JDOM to generate and read cache, you can get it [here](http://www.jdom.org/).

## FAQ

### Users

1. **This will delete repeated files?**

   No, it'll organize it. You choose if you want to delete

2. **So how I get rid of duplicates?**

   You can just delete all the subdirectories in `Results` except one (since there's no internal repetion) and rename the files yourself

3. **There's a way to rename them quickly?**

   You can use my [renamer tool here](https://github.com/FlyingWolFox/Duplicate-Finder-Renamer/) that is made to work with this tool, use another tool like Bulk Renaming Utility or a shell script/batch file

4. **How archives are compared?**

   Archives are compared between themselves. An archive is a duplicate of other if they have the same files. Archives with just one file are compared with other files

5. **The tools rise an exception/error, what should I do?**

   Read the exception message to see if you can fix yourself. Things like passing something that is not a directory as arguments will raise an exception because there's no verification to see if the argument is a valid directory (yet)

6. **I get acess denied, how I fix it?**

   The tool is probably trying to acess a protected directory (like a system directory or a directory that just a administrator can acess). Run the tool as admin and probably it'll fix it. This exception may appear too if you pass like a file as argument

7. **My directory aren't togheter in the same parent, can I use a shortcut in one of the parents?**

   No, because the shortcut acts like a file, so it won't work. This may be implemented in the future tho. While, this doesn't happen, you can use the terminal like this: `DuplicateFinder <directory path> <directory path> ...`

8. **There's a min and a max number of directories to analyse?**

   The min is one, in which it'll just look for internal repetions, the max is designed to be 26, after that the tool will raise an exception (error) or will stop to put letters in the filenames in the 27th and beyond directories

9. **The cache will be update if the files are?**

   Yes, the tool will check if the cache is valid, using the last modified date

10. **Can I delete the cache?**

   Yes. The cache will be regenerated on the next run

11. **I've found that something is going wrong, how can I help/get a solution?**

   You can always create an Issue to help improve this tool. I just ask you to look at all issues to see if your questions/problem hasn't been answered/solved

### Other programers

1. **Can I use your code in my project?**

Yes! This code is under the MIT license, so you're free to do anything with the code. You can also fork the repository.

2. **How can I help?**

You can help createing an Issue or a Pull Request. I'll look into it, I promise!

3. **I want to put FastMD5/other hashing algorithm in the code, where I change?**

The method `calculateHash()` in the FileInfo class is responsible to calculate the hash so any modifications in the hash calculation goes there. The method changes the `String hash` variable to contain the hash.

4. **Why not use other hashing algorithm or use multiple ones?**

I choosed MD5 because is relatively colision safe when looking for repeated files and it's fast. There was other alternatives, like SHA-1, that's fast too, but MD5 was good enough. Other hashes like SHA-2 or SHA-3 weren't considered because they're really slow. Other non-security algorithms weren't considered because I didn't know they existed until sometime ago :D You can put any hash algorithm you want in the code and it'll work.

5. **Everything is in the Main class in a weird way, how the code is designed?**

This is because this tool is more like a script, so things went to the Main classes. The code works basically in the constructors, that does almost everything. This is just to be pratic, feel free to change it (and even submmit a Pull Request!)

## Future plans

I'm planning to make a GUI version of this tool (when I learn how to make GUIs) to be even better! It'll be in another repository tho. I'll update here when I finish it
