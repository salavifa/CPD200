package com.nuggets.google.gae.cheevedit;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import java.util.logging.*;
import java.util.Date;

public class AchievementOps {
	
	private static final Logger log = Logger.getLogger(AchievementOps.class.getName()); 
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	public static void saveAchievement(String userEmail, String title, String category, String description, int score)
	{
		
		// retrieve cheever entity
		Entity cheever = null;
		try {
			cheever = datastore.get(KeyFactory.createKey("Cheever", userEmail));
		} catch (EntityNotFoundException e) {
			e.printStackTrace();
		}
		
		log.info("saving achievement");
		
		// create a new achievement entity
		Entity achievement = new Entity("Achievement");
			
		// set properties
		achievement.setProperty("title", title);
		achievement.setProperty("description", description);
		achievement.setProperty("category", category);
		achievement.setProperty("score", score);
		achievement.setProperty("contributor", (String) cheever.getProperty("username"));
		achievement.setProperty("numLiked", 0);
		achievement.setProperty("numCheeved", 0);
		achievement.setProperty("created", new Date());
		achievement.setProperty("verified", true);
		
		cheever.setProperty("numContribs", ((long) cheever.getProperty("numContribs")) + 1);
		
		// save to datastore via put operation
		datastore.put(achievement);
		datastore.put(cheever);

	}
	
}