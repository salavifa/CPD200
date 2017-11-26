package com.nuggets.google.gae.cheevedit;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.TransactionOptions;

import com.google.appengine.api.memcache.Expiration;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.taskqueue.TaskOptions;

import java.util.logging.*;
import java.io.IOException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

public class AchievementOps {
	
	private static final Logger log = Logger.getLogger(AchievementOps.class.getName()); 
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	private static MemcacheService memcache = MemcacheServiceFactory.getMemcacheService();	
	
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
	
	public static List<Entity> getPopularAchievements() throws EntityNotFoundException {

		// get from memcache
		List<Entity> popularAchievements = (List<Entity>) memcache.get("popularAchievements");
		
		// return memcached entities if they exist
		if (popularAchievements != null)
			return popularAchievements;		
		
		// create a query targeting achievement entities
		Query q = new Query("Achievement");	
		
		// sort by liked descending, then score descending
		q.addSort("numLiked", SortDirection.DESCENDING);
		q.addSort("score", SortDirection.DESCENDING);

		q.setFilter(new FilterPredicate("numLiked", FilterOperator.GREATER_THAN, 0));		
						
		// execute and return the first 5 results (will be top 5 with sort)
		popularAchievements = datastore.prepare(q).asList(FetchOptions.Builder.withLimit(5));
		
		// add results to memcache with an expiration of 60 seconds
		memcache.put("popularAchievements", popularAchievements, Expiration.byDeltaSeconds(60), SetPolicy.ADD_ONLY_IF_NOT_PRESENT);
		
		return popularAchievements;
	}	
	
	public static List<Entity> getAchievements(String title, String contributor, Date beginDate, Date endDate) throws IOException
	{  
		
		// create a query targeting achievement entities
		Query q = new Query("Achievement");	
		
		// create equality filters on title, contributor, verified. inequality filters on created
		FilterPredicate titleFilter = new FilterPredicate("title", FilterOperator.EQUAL, title);
		FilterPredicate contributorFilter = new FilterPredicate("contributor", FilterOperator.EQUAL, contributor);
		FilterPredicate beginDateFilter = new FilterPredicate("created", FilterOperator.GREATER_THAN_OR_EQUAL, beginDate);
		FilterPredicate endDateFilter = new FilterPredicate("created", FilterOperator.LESS_THAN_OR_EQUAL, endDate);
		FilterPredicate verifiedFilter = new FilterPredicate("verified", FilterOperator.EQUAL, true);
		
		ArrayList<Filter> filters = new ArrayList<Filter>();
		
		// add filters to an array, omit title & contributor if not supplied
		if (!title.trim().equals(""))
			filters.add(titleFilter);
		if (!contributor.trim().equals(""))
			filters.add(contributorFilter);
		filters.add(beginDateFilter);
		filters.add(endDateFilter);
		filters.add(verifiedFilter);
		
		// combine all filters with an AND operator
		CompositeFilter searchFilters = new CompositeFilter(CompositeFilterOperator.AND, filters);
		q.setFilter(searchFilters);
		
		// sort by created first (inequality), then sort
		q.addSort("created", SortDirection.DESCENDING);
				
		log.info(q.toString());
		
		return datastore.prepare(q).asList(FetchOptions.Builder.withDefaults());
	}	

	public static void likeAchievement(String achievementKey, String userEmail)
	{
		Entity achievement = null, cheever = null;
		
		// get cheever entity from datastore, constructing key from email
		try{
			cheever = datastore.get(KeyFactory.createKey("Cheever", userEmail));
		}
		catch (EntityNotFoundException enf){
			enf.printStackTrace();
		}

		// get achievement entity from datastore, converting encoded string back to key
		try{
			achievement = datastore.get(KeyFactory.stringToKey(achievementKey));
		}
		catch (EntityNotFoundException enf){
			enf.printStackTrace();
		}

		// get liked key multi-valued property
		List<Key> liked = (List<Key>) cheever.getProperty("liked");
		
		if (liked == null)
			// user has no likes yet, initialize multi-valued key property
			liked = new ArrayList<Key>();
		else {
			// check if user already liked achievement and ignore if they have
		    for (Iterator<Key> iter = liked.listIterator(); iter.hasNext(); ) {
			  Key likedKey = iter.next();
			  if (likedKey.equals(achievement.getKey())) {
				  log.info("already liked!");
				  return;						
		      }
		    }
		  }
		
		// add achievement key to liked multi-valued key property
		liked.add(achievement.getKey());
		
		// set liked multi-value property of keys to cheever entity
		cheever.setProperty("liked", liked);
		
		// increment numLiked property on achievement entity
		achievement.setProperty("numLiked", ((long) achievement.getProperty("numLiked")) + 1);
		
		// save to datastore via put operations
		Transaction txn = datastore.beginTransaction(TransactionOptions.Builder.withXG(true));
		try {		
			datastore.put(cheever);
			datastore.put(achievement);		
			
			txn.commit();
		}
		finally{
			if (txn.isActive())
				txn.rollback();
		}
	}			


	public static void completeAchievement(String achievementKey, String userEmail)
	{
		Entity achievement = null, cheever = null;
		
		// get cheever entity from datastore, constructing key from email
		try{
			cheever = datastore.get(KeyFactory.createKey("Cheever", userEmail));
		}
		catch (EntityNotFoundException enf){
			enf.printStackTrace();
		}

		// get achievement entity from datastore, converting encoded string back to key
		try{
			achievement = datastore.get(KeyFactory.stringToKey(achievementKey));
		}
		catch (EntityNotFoundException enf){
			enf.printStackTrace();
		}

		// get cheeved key multi-valued property
		List<Key> cheeved = (List<Key>) cheever.getProperty("cheeved");
		
		if (cheeved == null)
			// user has no achievements yet, initialize key list
			cheeved = new ArrayList<Key>();
		else {
			// check if user already LIKED achievement and ignore if they have
		    for (Iterator<Key> iter = cheeved.listIterator(); iter.hasNext(); ) {
			  Key likedKey = iter.next();
			  if (likedKey.equals(achievement.getKey())) {
				  log.info("already cheeved!");
				  return;						
		      }
		    }
		  }
		
		// add achievement key to cheeved multi-valued key property
		cheeved.add(achievement.getKey());
		
		// set cheeved multi-value property of keys to cheever entity
		cheever.setProperty("cheeved", cheeved);
		
		// increment aggregate properties
		achievement.setProperty("numCheeved", ((long) achievement.getProperty("numCheeved")) + 1);
		cheever.setProperty("numScore", ((long) cheever.getProperty("numScore")) + (long) achievement.getProperty("score"));
			
		
		// save to datastore via put operations
		Transaction txn = datastore.beginTransaction(TransactionOptions.Builder.withXG(true));
		try {		
			datastore.put(cheever);
			datastore.put(achievement);		
			
			txn.commit();
		}
		finally{
			if (txn.isActive())
				txn.rollback();
		}
		
		Queue queue = QueueFactory.getQueue("statqueue");

		queue.add(TaskOptions.Builder.withUrl("/tasks/stats?action=generatesystemstats").method(TaskOptions.Method.GET));
	}			

	public static Entity getSystemStats() {

		List<Entity> sysStats = (List<Entity>) memcache.get("sysStats");
		if (sysStats != null)
			return sysStats.get(0);

		// create a query targeting achievement entities
		Query q = new Query("SystemStats");			

		// sort by created descending, then score descending
		q.addSort("created", SortDirection.DESCENDING);

		sysStats = datastore.prepare(q).asList(FetchOptions.Builder.withLimit(1));
		memcache.put("sysStats", sysStats);
		
		return sysStats.get(0);

	}		
	
	public static List<Entity> getContributorLeaderboards() {
		
		List<Entity> contribStats = (List<Entity>) memcache.get("contribStats");
		if (contribStats != null)
			return contribStats;
		
		// create a query targeting achievement entities
		Query q = new Query("LeaderboardStats");	
		
		// add a filter on type
		q.setFilter(new FilterPredicate("type", FilterOperator.EQUAL, "contribution"));
		
		// sort by created descending, then score descending
		q.addSort("value", SortDirection.DESCENDING);
		
		// execute and return the first 10 results
		contribStats = datastore.prepare(q).asList(FetchOptions.Builder.withLimit(10));
		memcache.put("contribStats", contribStats);
		
		return contribStats;
		
	}

	public static List<Entity> getScoreLeaderboards() {
		
		List<Entity> scoreStats = (List<Entity>) memcache.get("scoreStats");
		if (scoreStats != null)
			return scoreStats;
		
		// create a query targeting achievement entities
		Query q = new Query("LeaderboardStats");	
		
		// add a filter on type
		q.setFilter(new FilterPredicate("type", FilterOperator.EQUAL, "score"));
		
		// sort by created descending, then score descending
		q.addSort("value", SortDirection.DESCENDING);
		
		// execute and return the first 10 results
		scoreStats = datastore.prepare(q).asList(FetchOptions.Builder.withLimit(10));
		memcache.put("scoreStats", scoreStats);
		
		return scoreStats;
	}
	
	
}