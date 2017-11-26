package com.nuggets.google.gae.tasks;

import com.nuggets.google.gae.tasks.TaskOps;

import java.io.IOException;
import javax.servlet.http.*;
import java.util.logging.Logger;

@SuppressWarnings("serial")
public class TasksServlet extends HttpServlet {
	
	private static final Logger log = Logger.getLogger("Tasks");

	public void doGet(HttpServletRequest req, HttpServletResponse resp)	throws IOException {
		
		log.info("TasksServlet Get");
		
		// GET to handle /tasks?action=generateleaderboardstats
		if (req.getParameter("action").equals("generateleaderboardstats")) {
			log.info("generating leaderboard stats");
			
			TaskOps.generateLeaderboardStats();
			
			resp.setContentType("text/plain");
			resp.getWriter().println("Generated Leaderboard stats");
		}

		// GET to handle /tasks?action=generatesystemstats
		if (req.getParameter("action").equals("generatesystemstats")) {
			log.info("generating system stats");
			
			TaskOps.generateSystemStats();
			
			resp.setContentType("text/plain");
			resp.getWriter().println("Generated System stats");
		}	
		
		// GET to handle /tasks?action=buildtestachievements
		if (req.getParameter("action").equals("buildtestachievements")) {
			log.info("Building test achievements");
			
			TaskOps.buildTestAchievements();
			
			resp.setContentType("text/plain");
			resp.getWriter().println("Test Achievements Built");
		}		
	

	}
}