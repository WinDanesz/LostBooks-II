package toast.lostBooks.helper;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import toast.lostBooks.LostBooks;
import toast.lostBooks.config.ConfigPropertyHelper;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class InitConfig {

	public static void init() {
		if (ConfigPropertyHelper.getBoolean(ConfigPropertyHelper.GENERAL, "generateDefaultBookPackAtStart")) {
			try {
				LostBooks.console("Initializing config directory with the default books. Set 'generateDefaultBookPackAtStart' to False in lostbooks.cfg to disable this behaviour.");
				URL url = FileHelper.class.getClassLoader().getResource("assets/lostbooks/LostBooks");
				url = formatFileUrl2JarUrl(url);
				JarURLConnection jarURLConnection = (JarURLConnection) url.openConnection();
				InitConfig.copyJarResourceToFolder(jarURLConnection, new File(LostBooks.CONFIG_DIRECTORY, "/LostBooks"));
				LostBooks.console("Completed initializing config directory with the default books.");
			}
			catch (Exception e) {
				LostBooks.console("Failed to initialize the ConfigFileHelper. Please report the below error at the issue tracker (https://github.com/WinDanesz/LostBooks-II/issues)");
				e.printStackTrace();
			}
		} else {
			LostBooks.console("Skipping initialization of the default book pack as 'generateDefaultBookPackAtStart' was set to False.");
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

	/* @param url
	 * @return
	 * @throws MalformedURLException
	 */
	public static URL formatFileUrl2JarUrl(URL url) throws MalformedURLException {
		StringBuilder urlStr = new StringBuilder();
		urlStr.append(url.toString());
		System.out.println(urlStr.toString());
		return new URL((urlStr.toString()));
	}
}
