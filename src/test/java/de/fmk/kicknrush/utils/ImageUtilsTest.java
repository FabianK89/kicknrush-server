package de.fmk.kicknrush.utils;

import org.junit.After;
import org.junit.Test;
import org.springframework.util.FileSystemUtils;

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
	private Path imgDir;


	@After
	public void tearDown() throws Exception {
		if (imgDir != null && Files.isDirectory(imgDir)) {
			FileSystemUtils.deleteRecursively(imgDir);
			assertFalse(Files.isDirectory(imgDir));

			imgDir = null;
		}
	}


	@Test
	public void storeTeamLogo() throws Exception {
		final String result;
		final String team;
		final String url;

		Optional<String> logo;

		imgDir = Paths.get(System.getProperty("user.home"), ".kicknrush", "images");
		result = Paths.get(imgDir.toString(), "fc_bayern_muenchen_small.png").toString();
		team   = "FC Bayern München";
		url    = "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1f/Logo_FC_Bayern_M%C3%BCnchen_" +
		         "%282002%E2%80%932017%29.svg/600px-Logo_FC_Bayern_M%C3%BCnchen_%282002%E2%80%932017%29.svg.png";

		assertFalse(ImageUtils.storeTeamLogo(null, team, false).isPresent());
		assertFalse(ImageUtils.storeTeamLogo(url, null, false).isPresent());

		logo = ImageUtils.storeTeamLogo(url, team, true);

		assertEquals(result, logo.orElse("NOT_FOUND"));
		assertTrue(Files.isDirectory(imgDir));
		assertTrue(Files.isRegularFile(Paths.get(result)));
	}
}