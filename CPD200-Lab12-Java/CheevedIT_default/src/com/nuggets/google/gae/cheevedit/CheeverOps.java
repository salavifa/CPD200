package com.nuggets.google.gae.cheevedit;

import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;

import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import java.util.logging.*;
import java.util.ArrayList;
import java.util.Date;

public class CheeverOps {
	
	private static final Logger log = Logger.getLogger(CheeverOps.class.getName()); 
	private static DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

	public static Entity getCheever(String userEmail) throws EntityNotFoundException {
		log.info("getCheever");
		// return cheever via get operation, constructing key from email
	    return datastore.get(KeyFactory.createKey("Cheever", userEmail));		
	}
	
	public static void saveCheever(String userEmail, String username, String notifyEmail, String bio)
	{
		log.info("saveCheever");
		
		// get cheever, if they don't exist, create entity with default values
		Entity cheever;
		try{
			cheever = datastore.get(KeyFactory.createKey("Cheever", userEmail));
		}
		catch (EntityNotFoundException enf){
			cheever = new Entity("Cheever", userEmail);
			cheever.setProperty("username", username);
			cheever.setProperty("followers", new ArrayList<Key>());
			cheever.setProperty("following", new ArrayList<Key>());
			cheever.setProperty("liked", new ArrayList<Key>());
			cheever.setProperty("cheeved", new ArrayList<Key>());
			cheever.setProperty("numScore", 0);
			cheever.setProperty("numFollowers", 0);
			cheever.setProperty("numFollowing", 0);
			cheever.setProperty("numContribs", 0);			
			cheever.setProperty("created", new Date());			
		}
		
		cheever.setProperty("notifyemail", notifyEmail);
		cheever.setProperty("bio", bio);

		// save to datastore via put operation
		datastore.put(cheever);	
	}
}