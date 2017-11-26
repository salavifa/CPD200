import webapp2
import jinja2
import os
import logging

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

        template = jinja_environment.get_template('home.template')
        self.response.out.write(template.render(self.template_values))

class ProfilePage(_BaseHandler):
    @profileauthdecorator.oauth_required
    def get(self):
        logging.info('ProfilePage class requested')        
        
        auth_http = profileauthdecorator.http()

        logging.info(auth_http)

        service = build('plus', 'v1', http=auth_http)

        people_resource = service.people()
        people_document = people_resource.get(userId='me').execute()

        self.template_values['cheever'] = {
            'imgUrl' : people_document['image']['url']
            }        
        
        template = jinja_environment.get_template('profile.template')
        self.response.out.write(template.render(self.template_values))         
        
app = webapp2.WSGIApplication([
    ('/admin', MainPage),
    ('/profile', ProfilePage),      
    (profileauthdecorator.callback_path, profileauthdecorator.callback_handler()),    
    ('/', MainPage)
], debug=True)