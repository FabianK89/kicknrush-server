package de.fmk.kicknrush.controller;

import de.fmk.kicknrush.db.DatabaseHandler;
import de.fmk.kicknrush.models.Update;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


/**
 * @author FabianK
 */
@RestController
@RequestMapping("/api/updates")
public class UpdatesController {
	private static final Logger LOGGER = LoggerFactory.getLogger(UpdatesController.class);

	@Autowired
	private DatabaseHandler dbHandler;


	@RequestMapping("/getAll")
	public List<Update> getAll() {
		return dbHandler.getUpdates();
	}
}
