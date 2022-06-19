================================================================================
  DativeRF Design Document
================================================================================


Requirements
================================================================================

These requirements are not exhaustive. I am using them right now as a simple
kanban board and to keep track of a backlog of ideas. For the "Dative v2 MVP"
project, there is a private GitHub project board at
https://github.com/orgs/dativebase/projects/10.


In Progress
--------------------------------------------------------------------------------


Ready
--------------------------------------------------------------------------------



Backlog
--------------------------------------------------------------------------------

- Successful authentication should result in the home page of the authenticated
  OLD being rendered under the home tab.
- URLs should work on page refresh or on a fresh page. Currently they do not.
  One reason is that the in-memory DB is lost and therefore the authenticated
  OLD cannot be retrieved.
- Form navigation should use its cache so that network requests are made when
  appropriate.

  - If the forms for a page are present in memory and they are not stale
    (threshold), use the in-memory data instead of making a request.
  - Allow for some manual control over this. For example, a user could hold
    command when clicking the "Forms" tab or a navigation button to ignore
    the cache.
  - This applies to other OLD resources also, not just forms. For example, the
    OLD's application-settings cache needs to be invalidated also.

- It should be possible to manage authentication more easily in DativeRF.

  - It should be possible to switch between OLDs more easily.
  - It should be clear which OLDs have valid cookies, i.e., which are
    authenticated.
  - A failure to de-authenticated should never break the app.


Done
--------------------------------------------------------------------------------

- DONE. The authenticated OLD's application settings should be displayed at a
  URL path that contains the OLD slug.
- DONE. When a user is logged out, we route them to the login GUI but the path
  still shows the /oldslug/logout path. It should show the /login path.
- DONE. The db ns needs a utility for accessing the slug of the authenticated
  OLD. This will make the events ns more clear, I think.
- DONE. It should be possible to visit a form at this url, e.g., /forms/1
- DONE. Each page should have a unique URL.

  - DONE. When form 1 is displayed, the URL path should display /forms/1.
  - DONE. When the most recent page of forms is displayed, the URL path should
    display /forms.
  - DONE. When a specific page of forms is displayed, the URL path should
    display /forms/page/1/items-per-page/5, for example.

- DONE. The URL should indicate the authenticated OLD, e.g., /blaold/forms/1
  should resolve to a display of the form with ID = 1 of the Blackfoot OLD.
- DONE. Unnecessary data should not be gathered up front.

  - DONE. For example, previously we were fetching an OLD's application
    settings, its data to create a new form, and its data to create a new form
    search upon every login.
