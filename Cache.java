import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import java.util.ArrayList;
import java.util.Collections;

import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Cache {
    /**
     * Creates cache for a directory
     * @param dir Directory to be cached
     */
    public static void CreateCache(Directory dir) {
        // sort arrays
        Collections.sort(dir.getArchives());
        Collections.sort(dir.getFiles());

        // root element
        Element dirElement = new Element("dir");
        dirElement.setAttribute(new Attribute("path", dir.getPath().toString()));
        Document doc = new Document(dirElement);

        // file subelements
        for(FileInfo file : dir.getFiles()) {
            Element fileElement = new Element("file");
            Element nameElement = new Element("name");
            Element hashElement = new Element("hash");
            Element lastModifiedElement = new Element("last_modified");
            nameElement.setText(file.getName());
            hashElement.setText(file.getHash());
            lastModifiedElement.setText(String.valueOf(file.getFile().lastModified()));
            fileElement.addContent(nameElement);
            fileElement.addContent(hashElement);
            fileElement.addContent(lastModifiedElement);
            doc.getRootElement().addContent(fileElement);
        }

        // archive subelement
        for(Archive archive : dir.getArchives()) {
            Element archiveElement = new Element("archive");            
            Element nameElement = new Element("name");
            Element hashesElement = new Element("hashes");            
            Element lastModifiedElement = new Element("last_modified");
            nameElement.setText(archive.getName());
            for(String hash : archive.getHashes()) {
                Element hashElement = new Element("hash");
                hashElement.setText(hash);
                hashesElement.addContent(hashElement);
            }            
            archiveElement.addContent(lastModifiedElement);
            doc.getRootElement().addContent(archiveElement);
        }

        // write cache
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            byte[] hashBytes = messageDigest.digest(dir.getPath().getParent().toString().getBytes());
            String hash = FileInfo.getStringHash(hashBytes);
            String name = dir.getPath().getFileName().toString();
            Path cacheDir = Paths.get("Results").resolve(".cache");
            Files.createDirectories(cacheDir);
            XMLOutputter xmlOutputter = new XMLOutputter();
            xmlOutputter.output(doc, new FileOutputStream(cacheDir.resolve(name + "_" + hash).toFile()));
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
    }

    public static ArrayList<FileInfo> getCache(Path dir) {
        ArrayList<FileInfo> files = new ArrayList<FileInfo>();
        // TODO: write method
        return files;
    }

    public static void UpdateCache(ArrayList<FileInfo> files) {

    }
}