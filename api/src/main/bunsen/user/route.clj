(ns bunsen.user.route)

(def routes
  ["/" {"user/v1" {"/status" :status
                   "/user" :user
                   "/users" {"" :users
                             ["/" :id] :users}
                   "/session" :session
                   "/sessions" :sessions
                   "/password" :password
                   "/seed" {"" :seed
                            "/users" :seed-users}}}])