package de.fmk.kicknrush.utils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.Base64Utils;
import org.springframework.util.FileSystemUtils;
import org.springframework.util.StreamUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * @author FabianK
 */
public class ImageUtilsTest {
	private Path   imgDir;
	private String rootDir;


	@Before
	public void setUp() throws Exception {
		rootDir = System.getProperty("user.home").concat("\\test");
		imgDir  = Paths.get(rootDir, ".kicknrush", "images");
	}


	@After
	public void tearDown() throws Exception {
		final Path path = Paths.get(rootDir);

		if (Files.isDirectory(path)) {
			FileSystemUtils.deleteRecursively(path);
			assertFalse(Files.isDirectory(path));
		}

		rootDir = null;
		imgDir  = null;
	}


	@Test
	public void encodeAndDecodeBase64AndWriteToFS() throws Exception {
		final Path   path;
		final String fileType;
		final String result;
		final String team;

		byte[] bytes;
		String encoded;

		fileType = ".png";
		path     = Paths.get(getClass().getResource("sc_freiburg.png").toURI());
		result   = imgDir.resolve("sc_freiburg.png").toString();
		team     = "SC Freiburg";

		try (InputStream is = getClass().getResourceAsStream("sc_freiburg.png")) {
			bytes   = StreamUtils.copyToByteArray(is);
			encoded = Base64Utils.encodeToUrlSafeString(bytes);
		}

		assertFalse(ImageUtils.encodeBase64(null).isPresent());
		assertFalse(ImageUtils.encodeBase64("Test").isPresent());
		assertEquals(encoded, ImageUtils.encodeBase64(path.toString()).orElse("UNKNOWN"));

		assertFalse(ImageUtils.decodeBase64AndWriteToFS(null, rootDir, team, fileType, false).isPresent());
		assertEquals(result, ImageUtils.decodeBase64AndWriteToFS(encoded, rootDir, team, fileType, false).orElse("UNKNOWN"));
	}


	@Test
	public void storeTeamLogo() throws Exception {
		final String result;
		final String team;
		final String url;

		Optional<String> logo;

		result  = Paths.get(imgDir.toString(), "fc_bayern_muenchen_small.png").toString();
		team    = "FC Bayern München";
		url     = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1f/Logo_FC_Bayern_M%C3%BCnchen_" +
		          "%282002%E2%80%932017%29.svg/600px-Logo_FC_Bayern_M%C3%BCnchen_%282002%E2%80%932017%29.svg.png";

		assertFalse(ImageUtils.storeTeamLogo(null, url, team, false).isPresent());
		assertFalse(ImageUtils.storeTeamLogo(rootDir, null, team, false).isPresent());
		assertFalse(ImageUtils.storeTeamLogo(rootDir, url, null, false).isPresent());

		logo = ImageUtils.storeTeamLogo(rootDir, url, team, true);

		assertEquals(result, logo.orElse("NOT_FOUND"));
		assertTrue(Files.isDirectory(imgDir));
		assertTrue(Files.isRegularFile(Paths.get(result)));
	}
}