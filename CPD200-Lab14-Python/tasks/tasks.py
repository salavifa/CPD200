import webapp2
import logging
import datetime
import models
import random
from google.appengine.ext import ndb

class TasksPage(webapp2.RequestHandler):
    def get(self):
    	logging.info('TasksPage class requested')            	
        self.response.out.write('TasksPage class requested')      

class GenerateSystemStats(webapp2.RequestHandler):
    def get(self):
        logging.info('Generating SystemStats')

        achievements = models.Achievement.query().fetch()
        cheevers = models.Cheever.query().fetch()
        contributors = models.Achievement.query(
                projection=[models.Achievement.contributor], 
                distinct=True).fetch()

        maxScore = 0
        for a in achievements:
            maxScore += a.score

        systemStats = models.SystemStats(
              numUsers = cheevers.__len__(),
              numContributors = contributors.__len__(),
              numAchievements = achievements.__len__(),
              maxScore = maxScore,
              created=datetime.datetime.now()
          )

        systemStats.put()
        self.response.out.write('System Stats Generated.')
        
class GenerateLeaderboardStats(webapp2.RequestHandler):
    def get(self):
        logging.info('Generate LeaderboardStats')

        cheevers = models.Cheever.query().order(-models.Cheever.numScore).fetch(5)

        for pos in range(0,cheevers.__len__()):
          leaderboardPos_key = ndb.Key('LeaderboardStats', str(pos)+'s')
          leaderboardPos = leaderboardPos_key.get()
          if not leaderboardPos:
            leaderboardPos = models.LeaderboardStats(key=leaderboardPos_key)

          leaderboardPos.populate(
            username = cheevers[pos].username,
            statType = 'score',
            value = cheevers[pos].numScore
          )

          leaderboardPos.put()
    
        cheevers = models.Cheever.query().order(-models.Cheever.numContribs).fetch(5)

        for pos in range(0,cheevers.__len__()):
          leaderboardPos_key = ndb.Key('LeaderboardStats', str(pos)+'c')
          leaderboardPos = leaderboardPos_key.get()
          if not leaderboardPos:
            leaderboardPos = models.LeaderboardStats(key=leaderboardPos_key)

          leaderboardPos.populate(
                username = cheevers[pos].username,
                statType = 'contribution',
                value = cheevers[pos].numContribs
            )

          leaderboardPos.put()

class BuildTestAchievements(webapp2.RequestHandler):
    def get(self):
        for x in range(1,50):
            a = models.Achievement()
            a.populate(
                title='Test Achievement ' + str(x),
                description = 'Test Achievement ' + str(x),
                category='Test',
                score=random.randint(50,250),
                contributor='Test'+str(x%3),
                verified=True
                )
            
            a.put()
            
app = webapp2.WSGIApplication([
    ('/tasks/', TasksPage),
    ('/tasks/GenerateLeaderboardStats', GenerateLeaderboardStats),
    ('/tasks/GenerateSystemStats', GenerateSystemStats),
    ('/tasks/BuildTestAchievements', BuildTestAchievements),
], debug=True)