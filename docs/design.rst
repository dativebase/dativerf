================================================================================
  DativeRF Design Document
================================================================================

Requirements
================================================================================

- Each page should have a unique URL.

  - It should be possible to visit a form at this url, e.g., /forms/1
  - DONE. It should be possible to browse the most recent page of forms at this
    URL, e.g., /forms.
  - DONE. It should be possible to browse a specific page of forms at this URL,
    e.g., /forms/page/1/items-per-page/5

- Form navigation should use its cache so that network requests are made when
  appropriate.

  - If the forms for a page are present in memory and they are not stale
    (threshold), use the in-memory data instead of making a request.
  - Allow for some manual control over this. For example, a user could hold
    command when clicking the "Forms" tab or a navigation button to ignore
    the cache.

- The URL should indicate the authenticated OLD, e.g., /blaold/forms/1 should
  resolve to a display of the form with ID = 1 of the Blackfoot OLD.

- It should be possible to manage authentication more easily in DativeRF.

  - It should be possible to switch between OLDs more easily.
  - It should be clear which OLDs have valid cookies, i.e., which are
    authenticated.
  - A failure to de-authenticated should never break the app.

- Unnecessary data should not be gathered up front.

  - For example, previously we were fetching an OLD's application settings, its
    data to create a new form, and its data to create a new form search upon
    every login.

 - Utilities for constructing route maps should be defined in the routes ns.

   - Right now, the events ns contains a lot of duplicative boilerplate around
     constructing routes.
