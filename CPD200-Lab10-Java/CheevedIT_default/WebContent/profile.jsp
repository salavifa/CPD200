<%@ include file="header.jsp" %>

<%@ page import="com.nuggets.google.gae.cheevedit.*" %>
<%@ page import="com.google.appengine.api.datastore.*" %>
<%@ page import="java.util.*" %>

<%
	String heading;
	Entity user = null;
	
	try{
		user = CheeverOps.getCheever(userService.getCurrentUser().getEmail());
		heading = "Edit your Cheever profile";
	}
	catch (EntityNotFoundException e) {
		heading = "Add your Cheever profile";
	}
	
%>


    <section class="profile container">
    <div class="intro">
      <h2><%= heading %></h2>
    </div>

    <!-- Content -->

    <section class="content">
      <section class="profile-edit">
        <div class="profile-info">
          <h2><i class="fa fa-edit"></i> My Profile</h2>

          <div class="profile-info-body">
            <form name="edit-user" action="/cheevers/save" method="post">
              <div class="username">
                <label>Username:</label>
                <input type="text" name="username" value="<%= user != null ? user.getProperty("username") : "yourusername" %>"></input>
              </div>

              <div class="email">
                <label>Email:</label>
                <input type="email" name="email" value="<%= user != null ? user.getProperty("notifyemail") : "you@email.com" %>"></input>
              </div>

              <div class="bio">
                <label>Bio:</label>
                <textarea name="bio"><%= user != null ? user.getProperty("bio") : "Your bio" %></textarea>
              </div>

              <button name="save" type="submit">Update</button>
            </form>
          </div>
        </div>

        <div class="add-new">
          <h2><i class="fa fa-plus"></i> Add New Achievement</h2>

          <div class="add-new-body">
            <form name="add-new" action="/achievements/save" method="post">
              <div class="name">
                <label>Name:</label>
                <input type="text" name="achievement-name"></input>
              </div>

              <div class="category">
                <label>Category:</label>
                <input type="text" name="achievement-category"></input>
              </div>

              <div class="description">
                <label>Description:</label>
                <textarea name="achievement-description"></textarea>
              </div>

              <div class="score">
                <label>Score:</label>
                <input type="text" name="achievement-score" value="50"></input>
              </div>

              <button name="save" type="submit">Add</button>
            </form>
          </div>
        </div>
      </section>

      <section class="my-achievements">
        <h3><i class="fa fa-flag"></i> My Achievements</h3>
      </section>
    </section>
  </section>

<%@ include file="footer.jsp" %>