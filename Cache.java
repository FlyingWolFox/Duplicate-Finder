import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * creates, gets and updates xml cache files for the tool. To know more info:
 * https://github.com/FlyingWolFox/Duplicate-Finder
 * 
 * @author FlyingWolFox
 * @version 1.0-beta
 */
public class Cache {
    /**
     * Creates cache for a directory
     * 
     * @param dir Directory to be cached
     */
    public static void createCache(Directory dir) {
        System.out.println("Creating cache...");

        // sort arrays
        Collections.sort(dir.getArchives());
        Collections.sort(dir.getFiles());

        // root element
        Element dirElement = new Element("dir");
        dirElement.setAttribute(new Attribute("path", dir.getPath().toString()));
        Document doc = new Document(dirElement);

        // file subelements
        ProgressBar bar = new ProgressBar("Caching files", dir.getFiles().size());
        for (FileInfo file : dir.getFiles()) {
            Element fileElement = convertToFileElement(file);
            doc.getRootElement().addContent(fileElement);
            bar.update();
        }

        // archive subelement
        bar = new ProgressBar("Caching archives", dir.getArchives().size());
        for (Archive archive : dir.getArchives()) {
            Element archiveElement = convertToArchiveElement(archive);
            doc.getRootElement().addContent(archiveElement);
            bar.update();
        }

        dirElement.sortChildren(new FileElementComparator());

        // write cache
        try {
            // get the file name
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] hashBytes = messageDigest.digest(dir.getPath().getParent().toString().getBytes());
            String hash = FileInfo.getStringHash(hashBytes);
            String name = dir.getPath().getFileName().toString();

            Path cacheDir = Paths.get("Results").resolve(".cache");
            Files.createDirectories(cacheDir);
            XMLOutputter xmlOutput = new XMLOutputter();
            FileOutputStream outuputFile = new FileOutputStream(cacheDir.resolve(name + "_" + hash).toFile());
            xmlOutput.setFormat(Format.getPrettyFormat());
            xmlOutput.output(doc, outuputFile);
            outuputFile.close();
        } catch (NoSuchAlgorithmException e) {
            System.out.print(e);
            System.out.print("Failed to get MD5 hash algorithm");
            System.out.print("Cache creation failed");
            System.exit(0xC0);
        } catch (IOException e) {
            System.out.print(e);
            System.out.print("Cache creation failed");
            System.exit(0xC1);
        }
        System.out.println("Cache creation complete");
    }

    /**
     * Creates a ELement based on a archive
     * 
     * @param archive archive to be converted
     * @return Element based on the archive
     */
    private static Element convertToArchiveElement(Archive archive) {
        Element archiveElement = new Element("archive");
        Element nameElement = new Element("name");
        Element hashesElement = new Element("hashes");
        Element lastModifiedElement = new Element("last_modified");
        nameElement.setText(archive.getName());
        for (String hash : archive.getHashes()) {
            Element hashElement = new Element("hash");
            hashElement.setText(hash);
            hashesElement.addContent(hashElement);
        }
        Element hashElement = new Element("hash");
        hashElement.setText(archive.getHash());
        lastModifiedElement.setText(String.valueOf(archive.getFile().lastModified()));
        archiveElement.addContent(nameElement);
        archiveElement.addContent(hashElement);
        archiveElement.addContent(hashesElement);
        archiveElement.addContent(lastModifiedElement);
        return archiveElement;
    }

    /**
     * Creates a ELement based on a file
     * 
     * @param file file to be converted
     * @return Element based on the file
     */
    private static Element convertToFileElement(FileInfo file) {
        Element fileElement = new Element("file");
        Element nameElement = new Element("name");
        Element hashElement = new Element("hash");
        Element lastModifiedElement = new Element("last_modified");
        nameElement.setText(file.getName());
        hashElement.setText(file.getHash());
        lastModifiedElement.setText(file.getLastModified());
        fileElement.addContent(nameElement);
        fileElement.addContent(hashElement);
        fileElement.addContent(lastModifiedElement);
        return fileElement;
    }

    /**
     * Gets cache for the specified dir
     * 
     * @param dir dir cache to be retrieved
     * @return FileInfo[][], FileInfo[0][] is files and FileInfo[1][] is archives
     */
    public static FileInfo[][] getCache(Directory dir) {
        System.out.println("Retrieving cache...");
        ArrayList<FileInfo> files = new ArrayList<FileInfo>();
        ArrayList<Archive> archives = new ArrayList<Archive>();
        MessageDigest messageDigest;
        try {
            // get filename
            messageDigest = MessageDigest.getInstance("MD5");
            byte[] hashBytes = messageDigest.digest(dir.getPath().getParent().toString().getBytes());
            String hash = FileInfo.getStringHash(hashBytes);
            String name = dir.getPath().getFileName().toString();

            File inputFile = Paths.get("Results").resolve(".cache").resolve(name + "_" + hash).toFile();
            if (!inputFile.exists()) {
                FileInfo[][] ret = { files.toArray(new FileInfo[files.size()]),
                        archives.toArray(new Archive[archives.size()]) };
                return ret;
            }

            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(inputFile);
            Element dirElement = document.getRootElement();
            if (!dirElement.getAttribute("path").getValue().equals(dir.getPath().toString())) {
                System.out.println(inputFile.getName() + " isn't " + dirElement.getAttribute("path").getValue() + " cache file!");
            }
            List<Element> filesList = dirElement.getChildren("file");
            ProgressBar bar = new ProgressBar("Retrieving files", filesList.size());
            for (Element fileElement : filesList) {
                String fileName = fileElement.getChild("name").getText();
                String fileHash = fileElement.getChild("hash").getText();
                String fileLastModified = fileElement.getChild("last_modified").getText();
                files.add(new FileInfo(dir.getPath().resolve(fileName), fileHash, fileLastModified, dir));
                bar.update();
            }
            List<Element> archiveList = dirElement.getChildren("archive");
            bar = new ProgressBar("Retrieving archives", archiveList.size());
            for (Element archiveElement : archiveList) {
                String fileName = archiveElement.getChild("name").getText();
                String fileHash = archiveElement.getChild("hash").getText();
                ArrayList<String> archiveHashes = new ArrayList<String>();
                for (Element hashElement : archiveElement.getChild("hash").getChildren("hash")) {
                    archiveHashes.add(hashElement.getText());
                }
                String fileLastModified = archiveElement.getChild("last_modified").getText();
                archives.add(
                        new Archive(dir.getPath().resolve(fileName), fileHash, archiveHashes, fileLastModified, dir));
                bar.update();
            }
        } catch (NoSuchAlgorithmException e) {
            System.out.print(e);
            System.out.print("Failed to get MD5 hash algorithm");
            System.out.print("Cache load failed");
            System.exit(0xC3);
        } catch (JDOMException e) {
            System.out.print(e);
            System.out.print("Failed to parse cache file");
            System.out.print("Cache load failed");
            System.exit(0xC4);
        } catch (IOException e) {
            System.out.print(e);
            System.out.print("Failed to get cache file");
            System.out.print("Cache load failed");
            System.exit(0xC5);
        }
        System.out.println("Cache retrieved");

        Collections.sort(files);
        Collections.sort(archives);
        FileInfo[][] ret = { files.toArray(new FileInfo[files.size()]),
                archives.toArray(new Archive[archives.size()]) };
        return ret;
    }

    /**
     * Updates dir cahe file, adding and removing files
     * 
     * @param dir            dir to have its cache updated
     * @param filesUpdate    filesUpdate[0][] is file additions and filesUpdate[1][]
     *                       is file removals
     * @param archivesUpdate archivesUpdate[0][] is archive additions and
     *                       archivesUpdate[1][] is archive removals
     */
    public static void updateCache(Directory dir, FileInfo[][] filesUpdate, Archive[][] archivesUpdate) {
        MessageDigest messageDigest;
        try {
            // Even if the cache file doesn't exists, nothing should break
            messageDigest = MessageDigest.getInstance("MD5");
            byte[] hashBytes = messageDigest.digest(dir.getPath().getParent().toString().getBytes());
            String hash = FileInfo.getStringHash(hashBytes);
            String name = dir.getPath().getFileName().toString();
            File inputFile = Paths.get("Results").resolve(".cache").resolve(name + "_" + hash).toFile();
            if (!inputFile.exists()) {
                createCache(dir);
                // TODO: return;
            }
            ProgressBar bar = new ProgressBar("Updating cache", 3);
            SAXBuilder saxBuilder = new SAXBuilder();
            Document document = saxBuilder.build(inputFile);
            Element dirElement = document.getRootElement();
            if (!dirElement.getAttribute("path").getValue().equals(dir.getPath().toString())) {
                System.out.println(inputFile.getName() + " isn't " + dirElement.getAttribute("path").getValue() + " cache file!");
            }
            for (FileInfo file : filesUpdate[1]) {
                Element fileElement = convertToFileElement(file);
                dirElement.addContent(fileElement);
            }
            for (Archive archive : archivesUpdate[1]) {
                Element archiveElement = convertToArchiveElement(archive);
                dirElement.addContent(archiveElement);
            }
            dirElement.sortChildren(new FileElementComparator());
            List<Element> children = dirElement.getChildren();
            for (int i = 0; i < children.size() - 1; i++) {
                String name1 = children.get(i).getChild("name").toString();
                String name2 = children.get(i + 1).getChild("name").toString();
                if (name1.equals(name2)) {
                    children.remove(i + 1);
                    children.remove(i);
                    i--;
                }
            }
            bar.update();
            for (FileInfo file : filesUpdate[0]) {
                Element fileElement = convertToFileElement(file);
                dirElement.addContent(fileElement);
            }
            for (Archive archive : archivesUpdate[0]) {
                Element archiveElement = convertToArchiveElement(archive);
                dirElement.addContent(archiveElement);
            }
            dirElement.sortChildren(new FileElementComparator());

            bar.update();

            XMLOutputter xmlOutput = new XMLOutputter();
            xmlOutput.setFormat(Format.getPrettyFormat());
            FileOutputStream output = new FileOutputStream(inputFile);
            xmlOutput.output(document, output);
            output.close();
            bar.update();
        } catch (NoSuchAlgorithmException e) {
            System.out.print(e);
            System.out.print("Failed to get MD5 hash algorithm");
            System.out.print("Cache update failed");
            System.exit(0xC6);
        } catch (JDOMException e) {
            System.out.print(e);
            System.out.print("Failed to parse cache file");
            System.out.print("Cache update failed");
            System.exit(0xC7);
        } catch (IOException e) {
            System.out.print(e);
            System.out.print("Cache update failed");
            System.exit(0xC8);
        }
    }
}

class FileElementComparator implements Comparator<Element> {
    public int compare(Element e1, Element e2) {
        // TODO: comparing by hash?
        String name1 = e1.getChild("name").getText();
        String name2 = e2.getChild("name").getText();

        return name1.compareTo(name2);
    }
}