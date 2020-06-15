package toast.lostBooks.helper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import toast.lostBooks.LostBooks;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ConfigFileHelper {

	public static void init() {
		try {
			LostBooks.console("Initializing config directory with the default books");
			URL url = FileHelper.class.getClassLoader().getResource("assets/lostbooks/LostBooks");
			JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
			ConfigFileHelper.copyJarResourceToFolder(jarURLConnection, new File(LostBooks.CONFIG_DIRECTORY, "/LostBooks"));
		}
		catch (Exception e) {
			LostBooks.console("Failed to initialize the ConfigFileHelper");
			e.printStackTrace();
		}
	}

	/**
	 * This method will copy resources from the jar file of the current thread and extract it to the destination folder.
	 *
	 * @param jarConnection
	 * @param destDir
	 * @throws IOException
	 */
	public static void copyJarResourceToFolder(JarURLConnection jarConnection, File destDir) {

		try {
			JarFile jarFile = jarConnection.getJarFile();

			/**
			 * Iterate all entries in the jar file.
			 */
			for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {

				JarEntry jarEntry = e.nextElement();
				String jarEntryName = jarEntry.getName();
				String jarConnectionEntryName = jarConnection.getEntryName();

				/**
				 * Extract files only if they match the path.
				 */
				if (jarEntryName.startsWith(jarConnectionEntryName)) {

					String filename = jarEntryName.startsWith(jarConnectionEntryName) ? jarEntryName.substring(jarConnectionEntryName.length()) : jarEntryName;
					File currentFile = new File(destDir, filename);

					if (jarEntry.isDirectory()) {
						currentFile.mkdirs();
					} else {
						InputStream is = jarFile.getInputStream(jarEntry);
						OutputStream out = FileUtils.openOutputStream(currentFile);
						IOUtils.copy(is, out);
						is.close();
						out.close();
					}
				}
			}
		}
		catch (IOException e) {
			LostBooks.console("Failed to initialize the /config/LostBooks config directory with the default book pack!");
			e.printStackTrace();
		}

	}

}
