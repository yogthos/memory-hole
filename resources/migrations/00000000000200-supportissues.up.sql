CREATE TABLE support_issues
(
  support_issue_id SERIAL    NOT NULL,
  title            TEXT      NOT NULL,
  summary          TEXT      NOT NULL,
  detail           TEXT      NOT NULL,
  search_vector    TSVECTOR,
  create_date      TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
  created_by       BIGINT    NOT NULL REFERENCES users (user_id),
  update_date      TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
  last_updated_by  BIGINT    NOT NULL REFERENCES users (user_id),
  delete_date      TIME      NULL,
  last_viewed      TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
  views            NUMERIC   DEFAULT 1,
  CONSTRAINT pk_support_issues PRIMARY KEY (support_issue_id)
)
WITH (
OIDS = FALSE
);
--;;
-- index our search vector for faster searches
CREATE INDEX idx_search_vector ON support_issues USING GIST (search_vector);
--;;
-- create the update/insert search_vector function for support_issues.
-- This trigger creates a concatenated, weighted vector across
-- title, summary, and detail, where title is the heaviest weighted
-- and detail the least weighted.
-- for more info conuslt
-- http://www.postgresql.org/docs/9.1/static/textsearch-features.html#TEXTSEARCH-MANIPULATE-TSVECTOR
CREATE FUNCTION tsvector_update_trigger_support_issues()
  RETURNS TRIGGER AS $$
BEGIN
  new.search_vector :=
  setweight(to_tsvector('english', new.title), 'A') ||
  setweight(to_tsvector('english', new.summary), 'B') ||
  setweight(to_tsvector('english', new.detail), 'C');
  RETURN new;
END
$$ LANGUAGE plpgsql;
--;;
-- create the update/insert trigger for support_issues which will update
-- the search_vector column, allowing users to perform a weighted search
-- for issues.
CREATE TRIGGER search_vector_update BEFORE INSERT OR UPDATE
ON support_issues FOR EACH ROW EXECUTE PROCEDURE tsvector_update_trigger_support_issues();
--;;
INSERT INTO support_issues (support_issue_id, title, summary, detail, created_by, last_updated_by) VALUES (1, 'Basics of Supper', 'Supper is a simple tool that helps you quickly create and find solutions to support issues. This article quickly describes how to use it.', 'Upvoting Issues
-
Upvoting is at the heart of  If an issue solves your problem, Up Vote it!  As you upvote issues, the more frequent ones will float to the top, and the less frequent ones will fall to the bottom.

_Aw man, but I didn''t mean to upvote it!_

No problem, you can click the Upvote button again and it will then downvote the article (ie. cancel your upvote). Note, however, that you can''t go around down-voting issues. If you don''t like the solution then, by all means, edit it and make it better!

Creating an Issue
-
Creating an issue in Supper is easy - simply click the +Add Issue button in the top right corner. You''ll be presented with 4 fields, all of which are required:

1. Title
2. Summary
3. Description
4. Tags

As you fill in the fields, a live preview will be displayed immediately to the left. This gives you an idea of how your issue will look, including all Markdown formatting.

###Title
This is the main header of the issue, and will be the main link and title that other users will see when browsing, or in their search results. Markdown formatting is not used in Title.

###Summary
The Summary is also displayed in search results and when browsing. This can be somewhat longer description of the Title, but not too long. Markdown formatting is not used on summaries.

###Description
Ah, the meat of the issue. Here you can go into detail about how to quickly resolve the issue at hand. You can use Markdown to beautifully format it. Markdown is a super easy, intuitive way of formatting a document. You can [read more about here](https://daringfireball.net/projects/markdown/basics).

###Tags
You can add tags to any issue, just type it. Supper will automatically prefix the familiar ''#'' character for you. You can then browse issues by tag (though, currently, you cannot search by tag because I''m lazy).

Editing Issues
-
When viewing the issue details (that is, the full issue), you can hit the Edit button. This will take you to a view nigh identical to the Add Issue view. You can then modify the issue however you want. Issues _*are not*_ saved unless you click the Save button.

_But what if I made a bunch of changes and I don''t want to save? Or I want to start over?_

Easy, just click the _Rollback_ button. That will re-fetch the issue and completely discount any changes you made. Ironically, _Rollback_ cannot be rolled back, so once you click it, your changes are gone, baby, gone.

Saving an Issue
-
Yea, just click the Save button

Previewing an Issue
-
At any point while editing an issue you can click the Preview button. This will let you see what your issue will actually look like. After you''re done fawning over the beauty of your issue, you can resume editing by clicking _Edit_.

Browsing Issues
-
Browsing issues is pretty straight forward, you just scroll. Alternatively, you can click on the list of tags in the panel on the left. Clicking on a tag will show only issues with that tag. Alternatively, you can check/uncheck the various tags to show/hide matching issues. It''s pretty simple, really.

Searching Issues
-
Alternatively to browsing, you can also search. This is a bit more complex, as you can use trivial operators to help you find what you''re looking for.

###Default Operators
By default, the search will _and_ the various search terms together, so only issues that match all of the terms will be returned.

For example, the terms _dogs cats_ will be translated into _dogs & cats_ on the server.

####Ands
As stated above, _and_''ing your search terms will only return articles that match all terms _and_''ed together. It''s pretty straight forward. "I only want articles that are about dogs _and_ cats _and_ ticks." Well, you can either type _dogs cats_ or, if you like to type, _dogs & cats & ticks_.

####Ors
Alternatively, if you don''t want _all_ the search terms to match against the same issue, you can use the _or_ operator (which is just a pipe, |). This will return issues that match any of the search terms, and not just all of them.

For example, say you want to find issues about dogs _or_ cats, and not just articles talking about dogs _and_ cats. You could submit the following search

_dogs | cats_

####Nots
You can also negate a search term. This will _remove_ articles from the results that have the matching terms. For example, maybe during your search for dogs you keep getting a bunch of articles back talking about dog catchers. You could write something like the following:

_dogs !catchers_

Boom. No more articles about dog catchers.

####Grouping
Lastly, none of this would be _that_ useful if you couldn''t group these things together. Maybe you have a bunch of pets that are having problems with ticks, and you want to find similar issues. You can group components of your search together to try and get exactly what you like. For example

_(dogs & cats) & ticks !catchers_

Or, alternatively, you can remove the superfluous &''s:insert into support_issues(title, summary, detail)
    values (''Basics of Supper'',
    ''Supper is a simple tool that helps you quickly create and find solutions to support issues. This article quickly describes how to use it.'',
    ''Upvoting Issues
-
Upvoting is at the heart of  If an issue solves your problem, Up Vote it!  As you upvote issues, the more frequent ones will float to the top, and the less frequent ones will fall to the bottom.

_Aw man, but I didn''''t mean to upvote it!_

No problem, you can click the Upvote button again and it will then downvote the article (ie. cancel your upvote). Note, however, that you can''''t go around down-voting issues. If you don''''t like the solution then, by all means, edit it and make it better!

Creating an Issue
-
Creating an issue in Supper is easy - simply click the +Add Issue button in the top right corner. You''''ll be presented with 4 fields, all of which are required:

1. Title
2. Summary
3. Description
4. Tags

As you fill in the fields, a live preview will be displayed immediately to the left. This gives you an idea of how your issue will look, including all Markdown formatting.

###Title
This is the main header of the issue, and will be the main link and title that other users will see when browsing, or in their search results. Markdown formatting is not used in Title.

###Summary
The Summary is also displayed in search results and when browsing. This can be somewhat longer description of the Title, but not too long. Markdown formatting is not used on summaries.

###Description
Ah, the meat of the issue. Here you can go into detail about how to quickly resolve the issue at hand. You can use Markdown to beautifully format it. Markdown is a super easy, intuitive way of formatting a document. You can [read more about here](https://daringfireball.net/projects/markdown/basics).

###Tags
You can add tags to any issue, just type it. Supper will automatically prefix the familiar ''''#'''' character for you. You can then browse issues by tag (though, currently, you cannot search by tag because I''''m lazy).

Editing Issues
-
When viewing the issue details (that is, the full issue), you can hit the Edit button. This will take you to a view nigh identical to the Add Issue view. You can then modify the issue however you want. Issues _*are not*_ saved unless you click the Save button.

_But what if I made a bunch of changes and I don''''t want to save? Or I want to start over?_

Easy, just click the _Rollback_ button. That will re-fetch the issue and completely discount any changes you made. Ironically, _Rollback_ cannot be rolled back, so once you click it, your changes are gone, baby, gone.

Saving an Issue
-
Yea, just click the Save button

Previewing an Issue
-
At any point while editing an issue you can click the Preview button. This will let you see what your issue will actually look like. After you''''re done fawning over the beauty of your issue, you can resume editing by clicking _Edit_.

Browsing Issues
-
Browsing issues is pretty straight forward, you just scroll. Alternatively, you can click on the list of tags in the panel on the left. Clicking on a tag will show only issues with that tag. Alternatively, you can check/uncheck the various tags to show/hide matching issues. It''''s pretty simple, really.

Searching Issues
-
Alternatively to browsing, you can also search. This is a bit more complex, as you can use trivial operators to help you find what you''''re looking for.

###Default Operators
By default, the search will _and_ the various search terms together, so only issues that match all of the terms will be returned.

For example, the terms _dogs cats_ will be translated into _dogs & cats_ on the server.

####Ands
As stated above, _and_''''ing your search terms will only return articles that match all terms _and_''''ed together. It''''s pretty straight forward. "I only want articles that are about dogs _and_ cats _and_ ticks." Well, you can either type _dogs cats_ or, if you like to type, _dogs & cats & ticks_.

####Ors
Alternatively, if you don''''t want _all_ the search terms to match against the same issue, you can use the _or_ operator (which is just a pipe, |). This will return issues that match any of the search terms, and not just all of them.

For example, say you want to find issues about dogs _or_ cats, and not just articles talking about dogs _and_ cats. You could submit the following search

_dogs | cats_

####Nots
You can also negate a search term. This will _remove_ articles from the results that have the matching terms. For example, maybe during your search for dogs you keep getting a bunch of articles back talking about dog catchers. You could write something like the following:

_dogs !catchers_

Boom. No more articles about dog catchers.

####Grouping
Lastly, none of this would be _that_ useful if you couldn''''t group these things together. Maybe you have a bunch of pets that are having problems with ticks, and you want to find similar issues. You can group components of your search together to try and get exactly what you like. For example

_(dogs & cats) & ticks !catchers_

Or, alternatively, you can remove the superfluous &''''s:

_(dogs cats) ticks !catchers_', 1, 1);