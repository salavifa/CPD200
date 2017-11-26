import webapp2
import jinja2
import os
import logging
import models
import datetime

from google.appengine.ext import ndb
from google.appengine.api import users
from apiclient.discovery import build
from oauth2client.contrib.appengine import OAuth2Decorator

profileauthdecorator = OAuth2Decorator(
            client_id='314326393426-6bvmm9sds571kgnnd3k886i1sjq7co82.apps.googleusercontent.com',
            client_secret='QgKCMayAA5t2C1nmBbeg-Itn',
            scope='https://www.googleapis.com/auth/plus.login')

jinja_environment = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)))

class _BaseHandler(webapp2.RequestHandler):
    def initialize(self, request, response):
        super(_BaseHandler, self).initialize(request, response)

        self.user = users.get_current_user()

        if self.user:
            self.template_values = {
                'user': self.user,
                'is_admin': users.is_current_user_admin(),
                'logout_url': users.create_logout_url('/')}          
        else:
            self.template_values = {
                'login_url': users.create_login_url(self.request.url)}     

class MainPage(_BaseHandler):
    def get(self):
        logging.error('MainPage class requested')

        query = models.Achievement.query(models.Achievement.numLiked > 0)
        query = query.order(
                    -models.Achievement.numLiked,
                    -models.Achievement.score
                )        
        results = query.fetch(5)

        self.template_values['achievements'] = results
        
        template = jinja_environment.get_template('home.template')
        self.response.out.write(template.render(self.template_values))

class ProfilePage(_BaseHandler):
    @profileauthdecorator.oauth_required
    def get(self):
        logging.info('ProfilePage class requested')        
        
        #-------------------------------------------------------------------
        #Look for existing profile based on User's email
        cheever_key = ndb.Key('Cheever', self.user.email())
        cheever = cheever_key.get()

        if not cheever:
          #Profile doesn't yet exist, create a new Cheever with default values
          newKey = ndb.Key("Cheever", self.user.email())
          cheever = models.Cheever(key=newKey)

        #Add the cheever model to our template values for rendering
        self.template_values['cheever'] = cheever
        #--------------------------------------------------------------------        
        
        auth_http = profileauthdecorator.http()

        logging.info(auth_http)

        service = build('plus', 'v1', http=auth_http)

        people_resource = service.people()
        people_document = people_resource.get(userId='me').execute()

        self.template_values['imgUrl'] = people_document['image']['url']          
        
        template = jinja_environment.get_template('profile.template')
        self.response.out.write(template.render(self.template_values))         

    def post(self):
        logging.info('ProfilePage posted')

        #Look for existing profile based on Users' email
        cheever_key = ndb.Key('Cheever', self.user.email())
        cheever = cheever_key.get()

        if not cheever:
           #Profile doesn't yet exist, create a new Cheever with default values
           cheever = models.Cheever(key=ndb.Key("Cheever",self.user.email()))

        #Update the user controlled values
        cheever.username = self.request.get('username')
        cheever.notifyEmail = self.request.get('notifyEmail')   
        cheever.bio = self.request.get('bio')      

        #Commit our updates to the datastore
        cheever.put()

        self.redirect('/profile')

class AchievementsPage(_BaseHandler):
    def get(self):
        logging.info('AchievementsPage class requested')


        template = jinja_environment.get_template('achievements.template')
        self.response.out.write(template.render(self.template_values))

    def post(self):
        logging.info('AchievementsPage posted')

        query = models.Achievement.query()

        if self.request.get('title') != '':
            query = query.filter(models.Achievement.title == self.request.get('title'))    
            
        if self.request.get('contributor') != '':
            query = query.filter(models.Achievement.contributor == self.request.get('contributor')) 

        try:
            beginDate = datetime.datetime.strptime(self.request.get('beginDate'), '%Y-%m-%d')
        except:
            beginDate = datetime.datetime(1900,1,1)

        try:
            endDate = datetime.datetime.strptime(self.request.get('endDate'), '%Y-%m-%d')
        except:
            endDate = datetime.datetime.now()

        query = query.filter(
            models.Achievement.created >= beginDate,
            models.Achievement.created <= endDate)
            
        results = query.fetch()

        self.template_values['achievements'] = results     

        template = jinja_environment.get_template('achievements.template')
        self.response.out.write(template.render(self.template_values)) 
        
        
class NewAchievement(_BaseHandler):
    def post(self):
        logging.info('newAchievement posted')

        #Get the current Cheever's profile so we can update their numContribs
        cheever_key = ndb.Key('Cheever', self.user.email())
        cheever = cheever_key.get()

        if not cheever:
          #Profile doesn't yet exist, create a new Cheever with default values
          cheever = models.Cheever(key=ndb.Key("Cheever",self.user.email()))    

        #Create new achievement with auto-generated key
        achievement = models.Achievement()

        achievement.populate(
            title=self.request.get('title'),
            description=self.request.get('description'),
            category=self.request.get('category'),
            score=int(self.request.get('score')),
            contributor=cheever.username,
            verified=True
        )

        cheever.numContribs += 1

        #Commit our updates to the datastore
        achievement.put()
        cheever.put()

        self.redirect('/profile')        
        

class LikeAchievement(_BaseHandler):
    def get(self):
        logging.info('likeAchievement requested')

        achievement_key = ndb.Key(urlsafe=self.request.get('key'))
        achievement = achievement_key.get()

        current_key = ndb.Key('Cheever', self.user.email())
        current = current_key.get()

        if achievement_key in current.liked:
          current.liked.remove(achievement_key)
          achievement.numLiked -= 1

        else:
          current.liked.append(achievement_key)
          achievement.numLiked += 1

        #Commit our updates to the datastore
        @ndb.transactional(xg=True)
        def commit():
          achievement.put()
          current.put()

        commit()

        self.redirect('/')

class CompleteAchievement(_BaseHandler):
  def get(self):
    logging.info('completeAchievement requested')

    achievement_key = ndb.Key(urlsafe=self.request.get('key'))
    achievement = achievement_key.get()

    current_key = ndb.Key('Cheever', self.user.email())
    current = current_key.get()

    if achievement_key in current.cheeved:
      current.cheeved.remove(achievement_key)
      current.numScore -= achievement.score
      achievement.numCheeved -= 1

    else:
      current.cheeved.append(achievement_key)
      current.numScore += achievement.score
      achievement.numCheeved += 1

    #Commit our updates to the datastore
    @ndb.transactional(xg=True)
    def commit():
      achievement.put()
      current.put()

    commit()

    self.redirect('/')    
        
app = webapp2.WSGIApplication([
    ('/admin', MainPage),
    ('/profile', ProfilePage), 
    ('/achievements', AchievementsPage),
    ('/newAchievement', NewAchievement),
    ('/likeAchievement', LikeAchievement),
    ('/completeAchievement', CompleteAchievement),
    (profileauthdecorator.callback_path, profileauthdecorator.callback_handler()),    
    ('/', MainPage)
], debug=True)