import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;
import java.nio.file.Paths;
import java.nio.file.Files;

class Zipping {
   // Wrapper for string file exists
   def exists(String path) {
      Files.exists(Paths.get(path))
   }

   // Given a path to a directory, makes all requisite directories
   def mkdirs(String outputPath) {
      if (outputPath != null) {
         outputPath.tokenize("/").inject("") { partialPath, pathPart ->
            def path = Paths.get(partialPath, pathPart)
            if (!Files.exists(path)) {
               Files.createDirectory(path)
            }
            path.toString()
         }
      }
   }

   // Unzips a file to the specified destination, returns a list of all files that were unzipped
   def unzip(zipfile, outputPath) {
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
               Files.newOutputStream(filePath) << zipStream
            }
            files << filePath.toString()
         }
         zipStream.close()
         files
      } else {
         throw new FileNotFoundException(zipfile + " was not found")
      }
   }

   // Zips a list of files to a specified output file
   def zip(zipfile, files) {

      mkdirs(new File(zipfile).getParent())
      def zipStream = new ZipOutputStream(Files.newOutputStream(Paths.get(zipfile)))
      files.inject([]) { zipped, file ->
         if (!Files.exists(Paths.get(file))) throw new FileNotFoundException(file + " was not found")
         file.tokenize("/").inject([zipped, ""]) { args, part ->
            def cache = args[0]
            def partialPath = args[1]
            def path = Paths.get(partialPath, part)
            if (!cache.contains(path.toString())) {
               if (Files.isDirectory(path)) {
                  zipStream.putNextEntry(new ZipEntry(path.toString() + "/"))
               } else {
                  zipStream.putNextEntry(new ZipEntry(path.toString()))
                  zipStream << Files.newInputStream(path)
               }
               zipStream.closeEntry()
               cache << path.toString()
            }
            [cache, path.toString()]
         }
         zipped
      }
      zipStream.close()
   }
}
