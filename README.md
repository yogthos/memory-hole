# Memory Hole

Memory Hole is a support issue organizer. It's designed to provide a way to organize and search common support issues and their resolution.

## Features

- The app uses LDAP to handle authentication
- Issues are ranked based on the number of views and last time of access
- Issues are organized by user generated tags
- Markdown with live preview for issues
- Weighted full text search using PostgreSQL citext extension



## Prerequisites

You will need [Leiningen][1] 2.0 or above installed.

[1]: https://github.com/technomancy/leiningen

## Running during development

To start a web server for the application, run:

    lein run migrate
    lein run

To compile ClojureScript front-end, run:
    
    lein figwheel

## Building for production

    lein uberjar
    
This will produce `target/uberjar/memory-hole.jar` archive that can be run as follows:
   
    java -Dconf=conf.edn -jar memory-hole.jar migrate
    java -Dconf=conf.edn -jar memory-hole.jar

The `conf.edn` file should contain the configuration such as the database URL that will be used in production.

## Acknowledgments

The original implementation of the tool was written by [Ryan Baldwin](https://github.com/ryanbaldwin). The app uses the original schema and SQL queries from the original version with permission from the author.

## License

Copyright © 2016 Dmitri Sotnikov
