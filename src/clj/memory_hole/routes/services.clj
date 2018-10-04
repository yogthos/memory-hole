(ns memory-hole.routes.services
  (:require [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :refer [TempFileUpload wrap-multipart-params]]
            [schema.core :as s]
            [compojure.api.meta :refer [restructure-param]]
            [memory-hole.routes.services.attachments :as attachments]
            [memory-hole.routes.services.auth :as auth]
            [memory-hole.routes.services.groups :as groups]
            [memory-hole.routes.services.issues :as issues]
            [memory-hole.config :refer [env]]
            [buddy.auth.accessrules :refer [restrict]]
            [buddy.auth :refer [authenticated?]]))

(defn admin?
  [request]
  (:admin (:identity request)))

(defn access-error [_ _]
  (unauthorized {:error "unauthorized"}))

(defn wrap-restricted [handler rule]
  (restrict handler {:handler  rule
                     :on-error access-error}))

(defmethod restructure-param :auth-rules
  [_ rule acc]
  (update-in acc [:middleware] conj [wrap-restricted rule]))

(defmethod restructure-param :current-user
  [_ binding acc]
  (update-in acc [:letks] into [binding `(:identity ~'+compojure-api-request+)]))

(defapi service-routes
  {:swagger {:ui   "/swagger-ui"
             :spec "/swagger.json"
             :data {:info {:version     "1.0.0"
                           :title       "Sample API"
                           :description "Sample Services"}}}}

  (POST "/api/login" req
    :return auth/LoginResponse
    :body-params [userid :- s/Str
                  pass :- s/Str]
    :summary "user login handler"
    (auth/login userid pass req))

  (context "/admin" []
    :auth-rules admin?
    :tags ["admin"]

    ;;users
    (GET "/users/:screenname" []
      :path-params [screenname :- s/Str]
      :return auth/SearchResponse
      :summary "returns users with matching screennames"
      (auth/find-users screenname))

    (GET "/users/group/:group-name" []
      :path-params [group-name :- s/Str]
      :return auth/SearchResponse
      :summary "returns users that are part of a group"
      (auth/find-users-by-group group-name))

    (POST "/user" []
      :body-params [screenname :- s/Str
                    pass :- s/Str
                    pass-confirm :- s/Str
                    admin :- s/Bool
                    belongs-to :- [s/Str]
                    is-active :- s/Bool]
      (auth/register! {:screenname   screenname
                       :pass         pass
                       :pass-confirm pass-confirm
                       :admin        admin
                       :belongs-to   belongs-to
                       :is-active    is-active}))

    (PUT "/user" []
      :body-params [user-id :- s/Int
                    screenname :- s/Str
                    pass :- (s/maybe s/Str)
                    pass-confirm :- (s/maybe s/Str)
                    admin :- s/Bool
                    belongs-to :- [s/Str]
                    is-active :- s/Bool]
      :return auth/LoginResponse
      (auth/update-user! {:user-id      user-id
                          :screenname   screenname
                          :belongs-to   belongs-to
                          :pass         pass
                          :pass-confirm pass-confirm
                          :admin        admin
                          :is-active    is-active}))

    ;;groups
    (POST "/group" []
      :body [group groups/Group]
      :return groups/GroupResult
      :summary "add a new group"
      (if (contains? env :ldap)
        (groups/add-group! (select-keys group [:group-id :group-name]))
        (groups/add-group! (select-keys group [:group-name])))))

  (context "/api" []
    :auth-rules authenticated?
    :tags ["private"]

    (POST "/logout" []
      :return auth/LogoutResponse
      :summary "remove the user from the session"
      (auth/logout))

    ;;groups
    (GET "/groups" []
      :return groups/GroupsResult
      :summary "list all groups current user belongs to (or all groups if admin)"
      :current-user user
      (groups/groups-by-user {:user-id (:user-id user)}))

    ;;tags
    (GET "/tags" []
      :return issues/TagsResult
      :summary "list available tags"
      :current-user user
      (issues/tags {:user-id (:user-id user)}))

    ;;issues
    (GET "/issues" []
      :return issues/IssueSummaryResults
      :summary "list all issues"
      :current-user user
      (issues/all-issues {:user-id (:user-id user)}))

    (GET "/recent-issues" []
      :return issues/IssueSummaryResults
      :summary "list 10 most recent issues"
      :current-user user
      (issues/recent-issues {:user-id (:user-id user)
                             :limit 10}))

    (GET "/issues-by-views/:offset/:limit" []
      :path-params [offset :- s/Int limit :- s/Int]
      :return issues/IssueSummaryResults
      :summary "list issues by views using the given offset and limit"
      :current-user user
      (issues/issues-by-views {:user-id (:user-id user) :offset offset :limit limit}))

    (GET "/issues-by-tag/:tag" []
      :path-params [tag :- s/Str]
      :return issues/IssueSummaryResults
      :summary "list issues by the given tag"
      :current-user user
      (issues/issues-by-tag {:tag tag
                             :user-id (:user-id user)}))

    (GET "/issues-by-group/:group" []
         :path-params [group :- s/Str]
         :return issues/IssueSummaryResults
         :current-user user
         :summary "list issues by the given group name"
         (issues/issues-by-group {:group-name group
                                  :user-id (:user-id user)}))

    (GET "/issues-by-content/:part" []
         :path-params [part :- s/Str]
         :query-params [limit :- s/Int]
         :return issues/IssueHintResults
         :current-user user
         :summary "list issues starting with index prefix or title part"
         (issues/issues-by-content {:index-prefix part
                                    :titlepart    part
                                    :user-id      (:user-id user)
                                    :limit        limit}))

    (GET "/issues-by-content/" []
         :query-params [limit :- s/Int]
         :return issues/IssueHintResults
         :current-user user
         :summary "list issues starting with index prefix or title part"
         (issues/issues-by-content {:index-prefix nil
                                    :titlepart    nil
                                    :user-id      (:user-id user)
                                    :limit        limit}))

    (DELETE "/issue/:id" []
      :path-params [id :- s/Int]
      :return s/Int
      :current-user user
      :summary "delete the issue with the given id"
      (issues/delete-issue! {:support-issue-id id
                             :user-id (:user-id user)}))

    (POST "/search-issues" []
      :body-params [query :- s/Str
                    limit :- s/Int
                    offset :- s/Int]
      :return issues/IssueSummaryResults
      :summary "search for issues matching the query"
      :current-user user
      (issues/search-issues {:query  query
                             :limit  limit
                             :offset offset
                             :user-id (:user-id user)}))

    (GET "/issue/:id" []
      :path-params [id :- s/Int]
      :return issues/IssueResult
      :summary "returns the issue with the given id"
      :current-user user
      (issues/issue {:support-issue-id id
                     :user-id (:user-id user)}))

    (POST "/issue" []
      :current-user user
      :body-params [title :- s/Str
                    summary :- s/Str
                    detail :- s/Str
                    group-id :- s/Str
                    tags :- [s/Str]]
      :return s/Int
      :summary "adds a new issue"
      (issues/add-issue!
        {:title    title
         :summary  summary
         :detail   detail
         :tags     tags
         :group-id group-id
         :user-id (:user-id user)}))

    (PUT "/issue" []
      :current-user user
      :body-params [support-issue-id :- s/Int
                    title :- s/Str
                    summary :- s/Str
                    detail :- s/Str
                    group-id :- s/Str
                    tags :- [s/Str]]
      :return s/Int
      :summary "update an existing issue"
      (issues/update-issue!
        {:support-issue-id support-issue-id
         :title            title
         :summary          summary
         :detail           detail
         :tags             tags
         :group-id         group-id
         :user-id          (:user-id user)}))

    ;;attachments
    (POST "/attach-file" []
      :multipart-params [support-issue-id :- s/Int
                         file :- TempFileUpload]
      :middleware [wrap-multipart-params]
      :current-user user
      :summary "handles file upload"
      :return attachments/AttachmentResult
      (attachments/attach-file-to-issue! {:support-issue-id support-issue-id
                                          :user-id (:user-id user)} file))

    (GET "/file/:support-issue-id/:name" []
      :summary "load a file from the database matching the support issue id and the filename"
      :path-params [support-issue-id :- s/Int
                    name :- s/Str]
      :current-user user
      (attachments/load-file-data {:user-id (:user-id user)
                                   :support-issue-id support-issue-id
                                   :name             name}))

    (DELETE "/file/:support-issue-id/:name" []
      :summary "delete a file from the database"
      :path-params [support-issue-id :- s/Int
                    name :- s/Str]
      :current-user user
      :return attachments/AttachmentResult
      (attachments/remove-file-from-issue! {:user-id (:user-id user)
                                            :support-issue-id support-issue-id
                                            :name             name}))))
