package com.nuggets.google.gae.tasks;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;

import java.util.logging.Logger;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class TaskOps {

	private static final Logger log = Logger.getLogger(TaskOps.class.getName()); 
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();	
	
	public static void generateLeaderboardStats() {
		log.info("aggregating leaderboards");
		
		// scores leaderboard
		Query scoresQuery = new Query("Cheever").addSort("numScore", SortDirection.DESCENDING);
		List<Entity> scoreCheevers = datastore.prepare(scoresQuery).asList(FetchOptions.Builder.withLimit(5));
		
		log.info(scoreCheevers.toString());
		
		for (Integer pos = 1; pos <= scoreCheevers.size(); pos += 1) {
			Entity cheeverStat;
			try {
				cheeverStat = datastore.get(KeyFactory.createKey("LeaderboardStats", pos + "s"));
			} catch (EntityNotFoundException e) {
				cheeverStat = new Entity("LeaderboardStats", pos + "s");
			}
			
			cheeverStat.setProperty("username", scoreCheevers.get(pos - 1).getProperty("username"));
			cheeverStat.setProperty("type", "score");
			cheeverStat.setProperty("value", scoreCheevers.get(pos - 1).getProperty("numScore"));
			
			datastore.put(cheeverStat);	
		}
		
		
		// contributors leaderboard
		Query contribQuery = new Query("Cheever").addSort("numContribs", SortDirection.DESCENDING);
		List<Entity> contribCheevers = datastore.prepare(contribQuery).asList(FetchOptions.Builder.withLimit(5));
		
		for (Integer pos = 1; pos <= contribCheevers.size(); pos += 1) {
			Entity contribStat;
			try {
				contribStat = datastore.get(KeyFactory.createKey("LeaderboardStats", pos + "c"));
			} catch (EntityNotFoundException e) {
				contribStat = new Entity("LeaderboardStats", pos + "c");
			}
			
			contribStat.setProperty("username", contribCheevers.get(pos - 1).getProperty("username"));
			contribStat.setProperty("type", "contribution");
			contribStat.setProperty("value", contribCheevers.get(pos - 1).getProperty("numContribs"));
			
			datastore.put(contribStat);	
		}
		
	}	

	public static void generateSystemStats() {
		log.info("aggregating sys stats");
		
		Query achievementQuery = new Query("Achievement").setFilter(new FilterPredicate("verified", FilterOperator.EQUAL, true));
		Query cheeverQuery = new Query("Cheever");
		Query contributorQuery = new Query("Achievement");

		contributorQuery.addProjection(new PropertyProjection("contributor", String.class)).setDistinct(true);
		
		List<Entity> achievements = datastore.prepare(achievementQuery).asList(FetchOptions.Builder.withDefaults());
		List<Entity> cheevers = datastore.prepare(cheeverQuery).asList(FetchOptions.Builder.withDefaults());
		List<Entity> contributors = datastore.prepare(contributorQuery).asList(FetchOptions.Builder.withDefaults());		
		
		Integer pointStat = 0;
		for (Entity achievement : achievements) {
			pointStat += Integer.parseInt(achievement.getProperty("score").toString());
		}
		
		Entity sysStats = new Entity("SystemStats");
		sysStats.setProperty("cheevers", cheevers.size());
		sysStats.setProperty("contributors", contributors.size());
		sysStats.setProperty("achievements", achievements.size());
		sysStats.setProperty("maxpoints", pointStat);
		sysStats.setProperty("created", new Date());
		
		// save to datastore
		datastore.put(sysStats);		
	}
	
	public static void buildTestAchievements() {
		log.info("Building Test Achievements");
		
		for (Integer x = 1; x <= 50; x+=1) {
			
			Entity achievement = new Entity("Achievement");
			
			// set properties
			achievement.setProperty("title", "Test Achievement " + x.toString());
			achievement.setProperty("description", "Desc for Test Achievement " + x.toString());
			achievement.setProperty("category", "Test");
			Random random = new Random();
			achievement.setProperty("score", random.nextInt(200)+50);
			achievement.setProperty("contributor", "Test" + Integer.toString(x % 3));
			achievement.setProperty("numLiked", 0);
			achievement.setProperty("numCheeved", 0);
			achievement.setProperty("created", new Date());
			achievement.setProperty("verified", true);
			
			datastore.put(achievement);
		}
		
	}		

}
