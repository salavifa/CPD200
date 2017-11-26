package com.nuggets.google.gae.cheevedit;

import javax.servlet.http.*;
import java.io.IOException;
import java.util.logging.Logger;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

@SuppressWarnings("serial")
public class AchievementServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(AchievementServlet.class.getName()); 
	private UserService userService = UserServiceFactory.getUserService();  		
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

		// POST from profile add form to /achievements/save
		if (req.getParameter("save") != null) {
			log.info("saving achievement");
			
			AchievementOps.saveAchievement(userService.getCurrentUser().getEmail(), 
					req.getParameter("achievement-name"), 
					req.getParameter("achievement-category"), 
					req.getParameter("achievement-description"),
					Integer.parseInt(req.getParameter("achievement-score")));			
			
			resp.sendRedirect("/profile");
		}
		
	}
}