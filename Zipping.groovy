import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.nio.file.Paths;
import java.nio.file.Files;

class Zipping {
   // Wrapper for string file exists
   def exists(String path) {
      Files.exists(Paths.get(path))
   }

   // Given a path to a directory, makes all requisite directories
   def mkdirs(String outputPath) {
      outputPath.tokenize("/").inject("") { partialPath, pathPart ->
         def path = Paths.get(partialPath, pathPart)
         if (!Files.exists(path)) {
            Files.createDirectory(path)
         }
         path.toString()
      }
   }

   // Unzips a file to the specified destination, returns a list of all files that were unzipped
   def unzip(String zipfile, String outputPath) {
      if (exists(zipfile)) {
         def zipStream = new ZipInputStream(Files.newInputStream(Paths.get(zipfile)))
         def entry = null
         def files = []
         while((entry = zipStream.getNextEntry()) != null) {
            def filePath = Paths.get(outputPath, entry.getName())
            if (entry.isDirectory()) {
               mkdirs(filePath.toString())
            } else {
               mkdirs(new File(filePath.toString()).getParent())
               def outputStream = Files.newOutputStream(filePath)
               def buff = new byte[1024]
               def length = 0
               while((length = zipStream.read(buff)) > 0) {
                  outputStream.write(buff, 0, length)
               }
               outputStream.close()
            }
            files << filePath.toString()
         }
         zipStream.close()
         files
      } else {
         throw new FileNotFoundException(zipfile + " was not found")
      }
   }
}
