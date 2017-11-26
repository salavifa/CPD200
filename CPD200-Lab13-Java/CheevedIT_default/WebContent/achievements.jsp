<%@ page import="com.nuggets.google.gae.cheevedit.*" %>
<%@ page import="com.google.appengine.api.datastore.Entity" %>
<%@ page import="com.google.appengine.api.datastore.KeyFactory" %>
<%@ page import="java.util.*" %>
<%@ page import="java.text.DateFormat" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%@ include file="header.jsp" %>

<% 
Logger log = Logger.getLogger("Achievements");
log.severe("Home Page Loading"); 

DateFormat df = new SimpleDateFormat("MM.d.yyyy");
%>

  <section class="achievements container">

    <div class="intro">
      <h2>See what achievements others are earning!</h2>
    </div>

    <div class="search">
      <form action="/achievements/search" method="post">
        <div class="form-row">
          <label>Achievement Title:</label>
          <input type="text" name="achievement-title" placeholder="Enter Achievement"></input>
        </div>

        <div class="form-row">
          <label>Contributor:</label>
          <input type="text" name="achievement-contributor" placeholder="Enter Name"></input>
        </div>

        <div class="form-row dates">
          <label>Date Created:</label>
          <input type="date" name="achievement-begindate" ></input>
          <span>to</span>
          <input type="date" name="achievement-enddate" ></input>
        </div>

        <div class="form-row">
          <button name="search" type="submit">Search</button>
        </div>
      </form>
    </div>


    <!-- Content -->

    <section class="content">

      <div class="achievements">
        <h3><i class="fa fa-search"></i> Search Results:</h3>
        <ul>
        
        <%
        List<Entity> achievements = (List<Entity>)request.getAttribute("achievements");
          
          if (achievements != null){
            for (Entity achievement : achievements) {         
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
  </section>

 <%@ include file="footer.jsp" %>