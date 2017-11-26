<%@ include file="header.jsp" %>

<% 
Logger log = Logger.getLogger("Home");
log.severe("Home Page Loading"); 
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
