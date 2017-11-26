<%@ include file="header.jsp" %>

    <section class="profile container">
    <div class="intro">
      <h2>Keep your Achievements up to date!</h2>
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
                <input type="text" name="username"></input>
              </div>

              <div class="email">
                <label>Email:</label>
                <input type="email" name="email"></input>
              </div>

              <div class="bio">
                <label>Bio:</label>
                <textarea name="bio"></textarea>
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