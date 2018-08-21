package de.fmk.kicknrush.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;


/**
 * @author FabianK
 */
public class ImageUtils {
	public static Optional<String> storeTeamLogo(final String url, final String team, final boolean small)
			throws IOException {
		final Path   path;
		final String fileName;
		final String userHomeDir;

		if (url == null || team == null)
			return Optional.empty();

		fileName    = createFileName(team, url.substring(url.lastIndexOf(".")), small);
		userHomeDir = System.getProperty("user.home");
		path        = Paths.get(userHomeDir, ".kicknrush", "images");

		if (!Files.isDirectory(path))
			Files.createDirectories(path);

		System.out.println("Save image from " + url + " to " + path.resolve(fileName).toString());

		return Optional.of(storeImage(url, path.resolve(fileName)).toString());
	}


	private static Path storeImage(final String url, final Path destination) throws IOException {
		final URLConnection connection;

		byte[] buf;
		int    length;

		buf = new byte[1024];

		connection = new URL(url).openConnection();

		try (InputStream  is = connection.getInputStream();
		     OutputStream os = new FileOutputStream(destination.toFile())) {
			while ((length = is.read(buf)) > 0)
				os.write(buf, 0, length);
		}

		return destination;
	}


	private static String createFileName(final String name, final String fileType, final boolean small) {
		String fileName = name.toLowerCase();

		fileName = fileName.replace(" ", "_");
		fileName = fileName.replace("ä", "ae");
		fileName = fileName.replace("ö", "oe");
		fileName = fileName.replace("ü", "ue");
		fileName = fileName.replace("ß", "ss");
		fileName = fileName.replace(".", "");

		if (small)
			fileName = fileName.concat("_small");

		return fileName.concat(fileType);
	}
}
