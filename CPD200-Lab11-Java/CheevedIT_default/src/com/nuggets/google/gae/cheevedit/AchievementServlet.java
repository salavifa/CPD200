package com.nuggets.google.gae.cheevedit;

import javax.servlet.http.*;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.List;
import java.util.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;


import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import javax.servlet.ServletException;

import com.google.appengine.api.datastore.Entity;

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
		

		// POST from achievements search form to /achievements/search
		if (req.getParameter("search") != null) {
			log.info("searching achievements");		

			DateFormat df = new SimpleDateFormat("MM.d.yyyy");
			Date beginDate;
			try {
				beginDate = df.parse(req.getParameter("achievement-begindate"));
			} catch (ParseException e1) {
				beginDate = new Date(0);
			}
			
			Date endDate;
			try {
				endDate = df.parse(req.getParameter("achievement-enddate"));
			} catch (ParseException e1) {
				endDate = new Date();
			}
			
		   List<Entity> achievements = AchievementOps.getAchievements(req.getParameter("achievement-title"), 
				req.getParameter("achievement-contributor"), 
				beginDate, 
				endDate);
		 
		    try {
		    	req.setAttribute("achievements", achievements);
				getServletContext().getRequestDispatcher("/achievements").forward(req, resp);
		  	} catch (ServletException e) {
		  		e.printStackTrace();
		  	}    
			
		}					
		
	}
}