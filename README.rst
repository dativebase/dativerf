================================================================================
  Dative
================================================================================

Dative is a tool for documenting languages. It is a single-page application
(SPA) that is designed for interacting with an `Online Linguistic Database`_
(OLD) HTTP/JSON web service.

This is the new Dative. It is Dative written in re-frame_ and ClojureScript_.

Dative was created using the `re-frame template`_. See the source of that
project for details on the directory structure, dependencies and general setup
of this one.


Local Development
================================================================================

First, ensure you have Clojure, ClojureScript and Node installed on your system.

Then install the dependencies and serve the application::

  $ npm install
  $ npx shadow-cljs watch app

Once you see ``[:app] Build completed.`` in the terminal output, you should be
able to visit `http://localhost:8280`_ and see Dative running. As you modify and
save the code, the application will refresh automatically.

You can connect to the running REPL on port ``8777``. For example, with
`Leiningen`_::

  $ lein repl :connect localhost:8777
  shadow.user=> (shadow.cljs.devtools.api/nrepl-select :app)
  cljs.user=> (require '[dativerf.utils :as u])
  cljs.user=> (u/->kebab-case-recursive {:a_b 2})
  {:a-b 2}


Run the Tests
================================================================================

Run the tests::

  $ npx shadow-cljs compile test && node out/node-tests.js

Lint the project::

  $ clj-kondo --lint src


Build for Production
================================================================================

To build the app for production deployment::

  $ npm install
  $ npm run release

The above may take over 15 seconds before any output is printed, and over 30
seconds to complete.

Once complete, the ``release`` command will have created the
``resources/public/js/compiled`` directory, which contains the compiled
``app.js`` and ``manifest.edn`` files.

To verify locally that a production build is working as expected, run a quick
Python HTTP server in that directory::

  $ python -m http.server
  Serving HTTP on 0.0.0.0 port 8000

Then visit `http://0.0.0.0:8000/`_ to visit the production build of the app.


.. _re-frame: https://github.com/day8/re-frame
.. _`re-frame template`: https://github.com/day8/re-frame-template
.. _`http://localhost:8280`: http://localhost:8280 
.. _`http://0.0.0.0:8000/`: http://0.0.0.0:8000/
.. _Leiningen: https://leiningen.org/
.. _`Online Linguistic Database`: https://www.onlinelinguisticdatabase.org/
.. _ClojureScript: https://clojurescript.org/
