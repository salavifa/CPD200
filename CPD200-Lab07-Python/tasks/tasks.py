import webapp2
import logging

class TasksPage(webapp2.RequestHandler):
    def get(self):
    	logging.info('TasksPage class requested')            	
        self.response.out.write(response)

app = webapp2.WSGIApplication([
    ('/tasks', TasksPage)
], debug=True)