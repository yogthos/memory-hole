# Memory Hole

Memory Hole is a support issue organizer. It's designed to provide a way to organize and search common support issues and their resolution.

### [screenshots](https://github.com/yogthos/memory-hole/tree/master/screenshots)

## Features

- The app uses LDAP and/or internal table to handle authentication
- Issues are ranked based on the number of views and last time of access
- File attachments for issues
- Issues are organized by user generated tags
- Markdown with live preview for issues
- Weighted full text search using PostgreSQL citext extension

## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running during development

You will need to setup a `profiles.clj` with the configuration settings for the database and optionally LDAP, e.g:

```clojure
{:profiles/dev
 {:env
  {:database-url "jdbc:postgresql://localhost/postgres?user=admin&password=admin"
  ;;optional, will use internal table otherwise
  :ldap
  {:host
     {:address         "my-ldap-server.ca"
      :port            389
      :connect-timeout (* 1000 5)
      :timeout         (* 1000 30)}}}}}
```

Run the migrations

    lein run migrate

This will create the tables and add a default admin user, The default login is: `admin/admin`.

To start a web server for the application, run:

    lein run

To compile ClojureScript front-end, run:

    lein figwheel

## Building for production

    lein uberjar

This will produce `target/uberjar/memory-hole.jar` archive that can be run as follows:

    java -Dconf=conf.edn -jar memory-hole.jar migrate
    java -Dconf=conf.edn -jar memory-hole.jar

The `conf.edn` file should contain the configuration such as the database URL that will be used in production, e.g:

```clojure
{:database-url "jdbc:postgresql://localhost/postgres?user=admin&password=admin"}
```

### Security

To enable HTTPS support in production add the the following configuration under the `:ssl` key:

```clojure
{:database-url "jdbc:postgresql://localhost/postgres?user=admin&password=admin"
 :ssl
 {:port 3001
  :keystore "keystore.jks"
  :keystore-pass "changeit"}}
```

To disable HTTP access, set the `:port` to `nil`:

```clojure
{:database-url "jdbc:postgresql://localhost/postgres?user=admin&password=admin"
 :port nil
 :ssl
 {:port 3001
  :keystore "keystore.jks"
  :keystore-pass "changeit"}}
```

The app is not setup to use HTTPS, so it should be fronted with
Nginx or similar in production for secure transport. See [here](http://www.luminusweb.net/docs/deployment.md#setting_up_ssl) for details.

## Acknowledgments

The original implementation of the tool was written by [Ryan Baldwin](https://github.com/ryanbaldwin). The app is based on the original schema and SQL queries.

## License

Copyright © 2016 Dmitri Sotnikov
