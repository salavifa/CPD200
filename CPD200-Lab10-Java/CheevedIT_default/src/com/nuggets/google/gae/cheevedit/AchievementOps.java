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

import java.util.logging.*;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

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
	
	public static List<Entity> getPopularAchievements() throws EntityNotFoundException {
		
		// create a query targeting achievement entities
		Query q = new Query("Achievement");	
		
		// sort by liked descending, then score descending
		q.addSort("numLiked", SortDirection.DESCENDING);
		q.addSort("score", SortDirection.DESCENDING);

		q.setFilter(new FilterPredicate("numLiked", FilterOperator.GREATER_THAN, 0));		
						
		// execute and return the first 5 results (will be top 5 with sort)
		return datastore.prepare(q).asList(FetchOptions.Builder.withLimit(5));
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
	
}