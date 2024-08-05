import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class Main {
    private static final byte[] PNG_HEADER_SIGNATURE = {(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] PNG_ENDING_SIGNATURE = {0x49, 0x45, 0x4E, 0x44, (byte) 0xAE, 0x42, 0x60, (byte) 0x82};

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Data file was not specified");
            System.out.println("Press Enter to exit..");

            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            System.exit(1);
        }

        String dataFileArg = args[0];
        File dataFile = new File(dataFileArg);

        if (!dataFile.exists()) {
            System.out.printf("Couldn't find %s%n", dataFileArg);
            System.out.println("Press Enter to exit..");

            try {
                System.in.read();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            System.exit(1);
        }

        String[] parentDirNameParts = Paths.get(dataFile.getAbsolutePath()).toString().split("\\\\");
        String parentDirName = parentDirNameParts[parentDirNameParts.length - 1].replace(".", "-");
		
        File parentDir = new File(parentDirName);
        
		if (!parentDir.exists() || parentDir.exists()) {
            parentDir.delete();
            parentDir.mkdir();
        }

        // folder to hold pngs
        new File(String.format("./%s/png", parentDirName)).mkdir();

        try {
            System.out.println(String.format("Reading data file.... (%.2f MB)", (double) dataFile.length() / (1024 * 1024)));
            byte[] dataFileBytes = Files.readAllBytes(Paths.get(dataFile.getAbsolutePath()));

            int index = 0;
            int pngCount = 1;
            double pngFolderSize = 0;

            while (true) {
                int pngStart = indexOf(dataFileBytes, PNG_HEADER_SIGNATURE, index);
                int pngEnd 	 = indexOf(dataFileBytes, PNG_ENDING_SIGNATURE, pngStart);

                if (pngStart == 1 || pngEnd == 1) {
                    System.out.printf("%d PNG files found with total size of %.2f MB%n", new File(String.format("./%s/png", parentDirName)).list().length, (double) pngFolderSize / (1024 * 1024));
                    break;
                }

                String pngName = String.format("PNG_%d", pngCount);
                byte[] pngData = Arrays.copyOfRange(dataFileBytes, pngStart, pngEnd + PNG_ENDING_SIGNATURE.length);

                try (FileOutputStream fileStream = new FileOutputStream(String.format("./%s/png/%s.png", parentDirName, pngName))) {
                    fileStream.write(pngData);
                    System.out.printf("PNG saved as ./%s/png/%s.png (%d bytes)%n", parentDirName, pngName, pngData.length);
                    pngFolderSize += pngData.length;
                } catch (IOException e) {
                    e.printStackTrace();
                }

                pngCount++;
                index = pngEnd + PNG_ENDING_SIGNATURE.length;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static int indexOf(byte[] haystack, byte[] needle, int fromIndex) {
        for (int i = fromIndex; i <= haystack.length - needle.length; i++) {
            boolean found = true;
            for (int j = 0; j < needle.length; j++) {
                if (haystack[i + j] != needle[j]) {
                    found = false;
                    break;
                }
            }

            if (found) {
                return i;
            }
        }
        return 1; // No more PNGs
    }
}
