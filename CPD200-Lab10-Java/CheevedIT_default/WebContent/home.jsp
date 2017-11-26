<%@ include file="header.jsp" %>

<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<%@ page import="com.nuggets.google.gae.cheevedit.AchievementOps" %>

<%@ page import="java.util.List" %>
<%@ page import="java.util.Date" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>

<% 
Logger log = Logger.getLogger("Home");
log.severe("Home Page Loading"); 

DateFormat df = new SimpleDateFormat("MM.d.yyyy");
%>

 <!-- Home Container -->

  <section class="home container">

    <div class="intro">
      <h2>Cheeved IT! is a community driven website for submitting, tracking,
      and managing life achievements.</h2>
    </div>

    <!-- Content -->


    <section class="content">

      <div class="achievements">
        <h3><i class="fa fa-fire"></i> Popular Achievements</h3>
        <ul>
        
        <%
	   		List<Entity> popularAchievements = AchievementOps.getPopularAchievements();
        	
        	if (popularAchievements != null){
        		for (Entity achievement : popularAchievements) {     		
        %>            
        
          <li>
            <div class="heading">
              <h4><%= achievement.getProperty("title") %></h4>
              <div class="stats">
                <div>
                  <i class="fa fa-thumbs-up"></i>
                  <a href="/achievement?action=liked&id=<%= KeyFactory.keyToString(achievement.getKey()) %>"><span>(<%= achievement.getProperty("numLiked") %>)</span></a>
                </div>
                <div>
                  <i class="fa fa-check"></i>
                  <a href="/achievement?action=cheeved&id=<%= KeyFactory.keyToString(achievement.getKey()) %>"><span>(<%= achievement.getProperty("numCheeved") %>)</span></a>
                </div>
              </div>
            </div>

            <div class="info">
              <p><%= achievement.getProperty("description") %> <br><br></p>

              <ul>
                <li>
                  <span>Contributor:</span>
                  <span><%= achievement.getProperty("contributor") %></span>
                </li>
                <li>
                  <span>Category:</span>
                  <span><%= achievement.getProperty("category") %></span>
                </li>
                <li>
                  <span>Created:</span>
                  <span><%= df.format(achievement.getProperty("created")) %></span>
                </li>
              </ul>

              <div class="score"><%= achievement.getProperty("score") %></div>
            </div>
          </li>

		<%
        		}
			}
		%>

        </ul>
      </div>
    </section>

    <!-- Sidebar -->

    <aside>
      <section class="statistics">
        <h3><i class="fa fa-trophy"></i> Statistics</h3>

      </section>

      <section class="leaderboards">
        <h3><i class="fa fa-bar-chart"></i> Leaderboards</h3>
        <div class="top-scores">
          <h4>Top Scores</h4>

        </div>

        <div class="top-contributors">
          <h4>Top Contributers</h4>

        </div>
      </section>
    </aside>
  </section>

<%@ include file="footer.jsp" %>
