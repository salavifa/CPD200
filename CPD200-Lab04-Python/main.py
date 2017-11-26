import webapp2
import jinja2
import os
import logging

jinja_environment = jinja2.Environment(
    loader=jinja2.FileSystemLoader(os.path.dirname(__file__)))

class MainPage(webapp2.RequestHandler):
    def get(self):
	    logging.info('MainPage class requested')

	    template = jinja_environment.get_template('home.template')
	    self.response.out.write(template.render())

app = webapp2.WSGIApplication([
    ('/', MainPage)
], debug=True)