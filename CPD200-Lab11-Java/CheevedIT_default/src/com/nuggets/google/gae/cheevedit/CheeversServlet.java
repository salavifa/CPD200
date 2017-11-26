package com.nuggets.google.gae.cheevedit;

import javax.servlet.http.*;
import java.io.IOException;
import java.util.logging.Logger;

import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

import com.nuggets.google.gae.cheevedit.CheeverOps;

@SuppressWarnings("serial")
public class CheeversServlet extends HttpServlet {
	private static final Logger log = Logger.getLogger(CheeversServlet.class.getName()); 
	private UserService userService = UserServiceFactory.getUserService(); 
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {	
		// POST from profile save form to /cheevers/save
		if (req.getParameter("save") != null) {
			log.info("saving cheever");
			
			CheeverOps.saveCheever(userService.getCurrentUser().getEmail(), 
					req.getParameter("username"), 
					req.getParameter("email"), 
					req.getParameter("bio"));
			
			resp.sendRedirect("/profile");
		}
	}
}