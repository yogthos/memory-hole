(ns yuggoth.db)

(def default-db
  {:user        (js->clj js/user)
   :active-page (if js/user :home :login)


   :issue       {:support-issue-id 1
                 :tags             ["pro"]
                 :title            "restarting"
                 :summary          "stuff"
                 :detail           "
Once server starts, you should be able to navigate to [http://localhost:3000](http://localhost:3000) and see\nthe app running. The server can be started on an alternate port by either passing it as a parameter as seen below,\nor setting the `PORT` environment variable.\n\n```\nlein run -p 8000\n```\n\nNote that the page is prompting us to run the migrations in order to initialize the database. However, we've already done that earlier, so we won't need to do that again.\n\n### Creating Pages and Handling Form Input\n\nOur routes are defined in the `guestbook.routes.home` namespace. Let's open it up and add the logic for\nrendering the messages from the database. We'll first need to add a reference to our `db` namespace along with\nreferences for [Bouncer](https://github.com/leonardoborges/bouncer) validators and [ring.util.response](http://ring-clojure.github.io/ring/ring.util.response.html)\n\n```clojure\n(ns guestbook.routes.home\n  (:require\n    ...\n    [guestbook.db.core :as db]\n    [bouncer.core :as b]\n    [bouncer.validators :as v]\n    [ring.util.response :refer [redirect]]))\n```\n\nNext, we'll create a function to validate the form parameters.\n\n```clojure\n(defn validate-message [params]\n  (first\n    (b/validate\n      params\n      :name v/required\n      :message [v/required [v/min-count 10]])))\n```
"}

   })