<%@ page import="java.security.Principal" %>
<%@ page import="com.google.appengine.api.users.UserService" %>
<%@ page import="com.google.appengine.api.users.UserServiceFactory" %>

<!doctype html>
<html>
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width, maximum-scale=1, user-scalable=0">
  <title>Cheeved IT!</title>
  <link rel="stylesheet" href="css/style.css">
  <link href="http://maxcdn.bootstrapcdn.com/font-awesome/4.2.0/css/font-awesome.min.css" rel="stylesheet">
</head>
<body>
<%
UserService userService = UserServiceFactory.getUserService();         	          	
String requestUri = request.getRequestURI();
Principal userPrincipal = request.getUserPrincipal();

String url, urlLinkText;
Boolean userFlag, adminFlag = false;

if (userPrincipal == null) { 
	url = userService.createLoginURL(requestUri);	
  	userFlag = false;
} 
else { 
	url = userService.createLogoutURL("/");	
  	userFlag = true;
   	
	if (userService.isUserAdmin()) { 
		adminFlag = true;
   	}
   }
 %>
  <!-- Header -->

  <header>
    <div class="container">
      <div class="logo-box">
        <div class="logo">
          <i class="fa fa-line-chart"></i>
        </div>
        <h1>Cheeved IT!</h1>
        <span>Achievement Tracker for Life</span>
      </div>

      <!-- Navigation -->
	
      <div class="user-ctrl">
        <nav class="main-nav">
          <ul>
            <li><a href="/">Home</a></li>
            <li><a href="/achievements">Achievements</a></li>
            <li><a href="/cheevers">Cheevers</a></li>
            <% if (userFlag) { %>            
            <li>
              <a href="#">Welcome <%= userService.getCurrentUser().getNickname() %> <i class="fa fa-chevron-down"></i></a>
              <ul>
                <li><a href="/profile">My Profile</a></li>               
            	<% if (adminFlag) { %>    
                <li><a href="/admin">Admin</a></li>
                <% } %>
                <li><a href="<%= url %>">Logout</a></li>
              </ul>
            </li>
            <% } else { %>
            <li>
              <a href="<%= url %>">Login</a>
            </li>
            <% } %>     
          </ul>
        </nav>
      </div>
    </div>
 
  </header>