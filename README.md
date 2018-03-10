# Memory Hole

<img src="https://cdn.rawgit.com/yogthos/memory-hole/master/memory-hole.png"
 hspace="20" align="left" height="200"/>

>When one knew that any document was due for destruction, or even when one saw a scrap of waste paper lying about, it was an automatic action to lift the flap of the nearest memory hole and drop it in, whereupon it would be whirled away on a current of warm air to the enormous furnaces which were hidden somewhere in the recesses of the building
>- from [1984 by George Orwell](https://www.goodreads.com/book/show/5470.1984)

---

Memory Hole is a support issue organizer. It's designed to provide a way to organize and search common support issues and their resolution.

### [screenshots](https://github.com/yogthos/memory-hole/tree/master/screenshots)

## 1.0 Features

- The app uses LDAP and/or internal table to handle authentication
- Issues are ranked based on the number of views and last time of access
- File attachments for issues
- Issues are organized by user generated tags
- Markdown with live preview for issues
- Weighted full text search using PostgreSQL citext extension
- Users can view/edit issues based on their group membership
- If using LDAP, issues can be assigned to LDAP groups
- LDAP groups can be aliased with user friendly names

## Prerequisites

You will need the following to compile and run the application:

* [JDK](http://www.azul.com/downloads/zulu)
* [Leiningen](https://leiningen.org/)
* [PostgreSQL](http://postgresql.org) - see [here](#configuring-postgresql) for configuration details

## Running with Docker

```
mkdir memory-hole
cd memory-hole
curl -O https://raw.githubusercontent.com/yogthos/memory-hole/master/docker-compose.yml
docker-compose up
```

The app will be available at `http://localhost:8000` once it starts.

## Configuring the database

### PostgreSQL

Follow these steps to configure the database for the application:

0. Make sure you have the [CITEXT](https://www.postgresql.org/docs/9.5/static/citext.html)
extension installed on PostgreSQL.

1. Run the `psql` command:

        psql -U <superuser|postgres user> -d postgres -h localhost

2. Create the role role for accessing the database:

        CREATE ROLE memoryhole;

3. Set the password for the role:

        \password memoryhole;

4. Optionally, create a schema and grant the `memoryhole` role authorization:

        CREATE SCHEMA memoryhole AUTHORIZATION memoryhole;
        GRANT ALL ON SCHEMA memoryhole TO memoryhole;
        GRANT ALL ON ALL TABLES IN SCHEMA memoryhole TO memoryhole;

5. Add the CITEXT extension to the schema:

        CREATE EXTENSION IF NOT EXISTS citext WITH SCHEMA memoryhole;
        
6. Make sure memoryhole is allowed to login:

        ALTER ROLE "memoryhole" WITH LOGIN;

7. Exit the shell

        \q
        
This setup should lead to similar `:database-url` (eg. on local machine).

```clojure
:database-url "jdbc:postgresql://localhost/postgres?user=memoryhole&password=memoryhole"
```
        
### H2

H2 DB can use various hosting scenarios, which are available on its [feature list](http://h2database.com/html/features.html).

This setup can lead to following `:database-url` on local machine.

```clojure
:database-url "jdbc:h2:~/memory-hole-dev"
```

When H2 DB is used for development or production, it needs to have properly set migratus `:migration-dir` pointing to H2 specific migrations for populating schema.

```clojure
:migration-dir "migrations/h2"
```

## Running during development

Create a `profiles.clj` file in the project directory with the configuration settings for the database. Optionally migrations directory and LDAP can be configured, e.g:

```clojure
{:profiles/dev
 {:env
  {:database-url "jdbc:postgresql://localhost/postgres?user=memoryhole&password=memoryhole"
  ;; :migratus {:migration-dir "migrations/h2"}
  ;;ldap is optional, will use internal table otherwise
  ;;Admin users (able to manage groups) defined by their sAMAccountName
  :ldap-admin-users ["my-ldap-sAMAccountName" "another-ldap-sAMAccountName"]
  ;;Or Admin Groups defined by their distinguished names
  :ldap-admin-groups ["CN=some-ldap-group,OU=foo123,DC=domain,DC=ca"]
  :ldap
  {:host
     {:address         "my-ldap-server.ca"
      :domain          "domain.ca"
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

The `conf.edn` file should contain the configuration such as the database URL that will be used in production. The following options are available.

### Database URL

```clojure
:database-url "jdbc:postgresql://localhost/postgres?user=memoryhole&password=memoryhole"
```

### Migration directory

Depending on selected DB backend, migration directory needs to be set, eg.

```clojure
:migration-dir "migrations/postgresql"
```

### HTTP Port

The HTTP port defaults to `3000`, to set a custom port add the following key to the config:

```clojure
:port 80
```

### Session Configuration 

The app defaults to using a server-side memory based session store.

The number of sessions before a memory session times out can be set using the `:memory-session` key as follows:

```clojure
:memory-session
{:max-age 3600}
```

If you wish to use a cookie based memory store, then add a `:cookie-session` key to the configuration.
The `:cookie-session` key should point to a map containing two optional key:

* `:key` - a secret key used to encrypt the session cookie
* `:cookie-attrs` - a map containing optional cookie attributes:
* `:http-only` - restrict the cookie to HTTP if true (default)
* `:secure` - restrict the cookie to HTTPS URLs if true
* `:max-age` - the number of seconds until the cookie expires

An example configuration might look as follows:

```clojure
:cookie-session
{:key "a 16-byte secret"
 :cookie-attrs
 {:secure  true
  :max-age 3600}}
```

### LDAP Support

The LDAP connection configuration should be placed under the `:ldap` key as follows:

```clojure
:ldap
  {:host
     {:address         "my-ldap-server.ca"
      :domain          "domain.ca"
      :port            389
      :connect-timeout (* 1000 5)
      :timeout         (* 1000 30)}}
```

There are two options for managing user groups when using LDAP, you can either assign
admin users using the `sAMAccountName`, or specify groups that correspond to the `memberOf` key.

```clojure
:ldap-admin-users ["my-ldap-sAMAccountName" "another-ldap-sAMAccountName"]
```

```clojure
:ldap-admin-groups ["CN=some-ldap-group,OU=foo123,DC=domain,DC=ca"]
```

### HTTPS Support

To enable HTTPS support in production add the the following configuration under the `:ssl` key:

```clojure
:ssl
{:port 3001
 :keystore "keystore.jks"
 :keystore-pass "changeit"}
```

To disable HTTP access, set the `:port` to `nil`:

```clojure
:port nil
```

Alternatively, you can front the app with Nginx in production.
See [here](http://www.luminusweb.net/docs/deployment.md#setting_up_ssl) for details on configuring Nginx.

A complete `conf.edn` example:

```clojure
{:database-url "jdbc:postgresql://localhost/postgres?user=memoryhole&password=memoryhole"
 :cookie-session
 {:key "a 16-byte secret"
  :cookie-attrs
  {:max-age 60}}
 :port nil
 :ssl
 {:port 3001
  :keystore "keystore.jks"
  :keystore-pass "changeit"}}
```

## Nginx Proxy

The app can be proxied with Nginx to a custom path as follows:

```
server {
    listen ...;
    ...
    location /memory-hole {
        proxy_pass http://127.0.0.1:3000;
    }
    ...
}
```

You will then need to add the `:app-context` in the `conf.edn` file with the context:

```clojure
{:database-url "jdbc:postgresql://localhost/postgres?user=memoryhole&password=memoryhole"
 :port 3000
 :app-context "/memory-hole"}
```


## Acknowledgments

The original implementation of the tool was written by [Ryan Baldwin](https://github.com/ryanbaldwin). The app is based on the original schema and SQL queries.

## License

Copyright © 2016 Dmitri Sotnikov
