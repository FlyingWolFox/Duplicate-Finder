import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

public class Archive extends FileInfo {
    private ArrayList<String> hashes;

    public Archive(File file, Directory dir) {
        super(file, dir);
        hashes = new ArrayList<String>();
        try {
            calculateHashes();
        } catch (IOException e) {
            System.out.println(
                    "Error trying to get hashes for the archive " + super.getFile().getAbsolutePath() + ": " + e);
            e.printStackTrace();     
        }
    }

    public void calculateHashes() throws IOException {
        uncompress();

        for (byte[] digest : MyExtractCallback.getDigests()) {
            // This bytes[] has bytes in decimal format;
            // Convert it to hexadecimal format
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                sb.append(Integer.toString((digest[i] & 0xff) + 0x100, 16).substring(1));
            }

            hashes.add(sb.toString());
        }
        Collections.sort(hashes);
    }

    public void uncompress() throws IOException {
        RandomAccessFile randomAccessFile = null;
        IInArchive inArchive = null;
        try {
            randomAccessFile = new RandomAccessFile(super.getFile().getAbsolutePath(), "r");
            inArchive = SevenZip.openInArchive(null, // autodetect archive type
                    new RandomAccessFileInStream(randomAccessFile));

            System.out.println("                   Hash              | Filename");
            System.out.println("    ---------------------------------+---------");

            int[] in = new int[inArchive.getNumberOfItems()];
            for (int i = 0; i < in.length; i++) {
                in[i] = i;
            }
            inArchive.extract(in, false, // Non-test mode
                    new MyExtractCallback(inArchive));
        } catch (Exception e) {
            System.err.println("Error occurs: " + e);
            e.printStackTrace();
        } finally {
            if (inArchive != null) {
                try {
                    inArchive.close();
                } catch (SevenZipException e) {
                    System.err.println("Error closing archive: " + e);
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    System.err.println("Error closing file: " + e);
                }
            }
        }
    }

    public static class MyExtractCallback implements IArchiveExtractCallback {
        private int index;
        private boolean skipExtraction;
        private IInArchive inArchive;
        private static byte[] hash;
        private static ArrayList<byte[]> digests;

        public MyExtractCallback(IInArchive inArchive) {
            this.inArchive = inArchive;
            hash = null;
            digests = new ArrayList<byte[]>();
        }

        public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
            this.index = index;
            skipExtraction = (Boolean) inArchive.getProperty(index, PropID.IS_FOLDER);
            if (skipExtraction || extractAskMode != ExtractAskMode.EXTRACT) {
                return null;
            }
            return new ISequentialOutStream() {
                public int write(byte[] data) throws SevenZipException {
                    MessageDigest complete;
                    try {
                        complete = MessageDigest.getInstance("MD5");
                        hash = complete.digest(data);
                        digests.add(hash);
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    return data.length; // Return amount of proceed data
                }
            };
        }

        public void prepareOperation(ExtractAskMode extractAskMode) throws SevenZipException {
        }

        public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
            if (skipExtraction) {
                return;
            }
            if (extractOperationResult != ExtractOperationResult.OK) {
                System.err.println("Extraction error");
            } else {
                System.out.println(String.format("    %32s | %s", FileInfo.getStringHash(hash),
                        inArchive.getProperty(index, PropID.PATH)));
                hash = null;
            }
        }

        public void setCompleted(long completeValue) throws SevenZipException {
        }

        public void setTotal(long total) throws SevenZipException {
        }

        public static ArrayList<byte[]> getDigests() {
            return digests;
        }
    }

    @Override
    public boolean equals(Object anObject) {        
        if (anObject == null)
            return false;

        if(!(anObject instanceof Archive)){
            return false;
        }

        Archive archive = (Archive) anObject;

        if (this == archive)
            return true;
        
        // Collections.sort(this.hashes);
        // Collections.sort(archive.hashes);
        return this.hashes.equals(archive.hashes);
    }

    class ArchiveComparator implements Comparator<Archive> {
        @Override
        public int compare(Archive archive1, Archive archive2) {
            int comparationTotal = 0;
            comparationTotal += 60 * Integer.compare(archive1.hashes.size(), archive2.hashes.size());
            for (int i = 0; i < Math.min(archive1.hashes.size(), archive2.hashes.size()); i++) {
                int comp = archive1.hashes.get(i).compareTo(archive2.hashes.get(i));
                if (comp != 0) {
                    comparationTotal += comp;
                    break;
                }
            }
            return comparationTotal;
        }
    }

    // public static CompressedFile getCompressedFile(FileInfo file, CompressedType
    // type) { }

    public ArrayList<String> getHashes() {
        return this.hashes;
    }

    public static boolean isArchive(String filename) {
        String[] extensions = {".cpio", ".7z", ".zip", ".zipx", ".gz", ".bz2", ".tar", ".rar", ".rev", ".iso", ".arj", ".Z", ".udf"};
        int i  = filename.length() - 1;
        while (filename.charAt(i) != '.') {
            i--;
        };

        String fileExtension = filename.substring(i);

        for (String extension : extensions) {
            if (extension.equalsIgnoreCase(fileExtension))
                return true;
        }

        return false;
    }
}
