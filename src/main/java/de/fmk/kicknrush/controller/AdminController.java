package de.fmk.kicknrush.controller;

import de.fmk.kicknrush.models.AdminAccount;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminController {
	@RequestMapping("/getInitialAccount")
	public AdminAccount getInitialAdminAccount() {
		return new AdminAccount("admin", "admin123");
	}
}
