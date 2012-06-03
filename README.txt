Zeitgeist - An intelligent RSS news aggregator
(C) Copyright 2009-2012 Daniel W. Dyer

SYNOPSIS

  Zeitgeist is a Java library for identifying common topics among a set of news
  articles downloaded from RSS feeds.  It groups and ranks related articles.
  It is loosely based on the non-negative matrix factorisation example
  presented in chapter 10 of Toby Segaran's Programming Collective Intelligence
  book.


LICENCE

  Zeitgeist is licensed under the terms of the Apache Software Licence version
  2.0 (http://www.apache.org/licenses/LICENSE-2.0.txt).


BUILDING FROM SOURCE

  Zeitgeist is built using Apache Ant (http://ant.apache.org).   Version 1.7.1
  or later is required.  To rebuild from scratch run the following command from
  the project root directory:

      ant clean dist


GENERATING HTML OUTPUT

  Zeitgeist includes a basic HTML publisher for generating a page of news
  headlines, complete with relevant images extracted from the feed articles.
  This application can be run as follows:

      java -jar zeitgeist-publisher-1.0.jar feedlist.txt "Page Title"

  The first argument is a text file that contains a list of feed URLs, one per
  line, and the second argument is the title to use for the generated page.

  To get good results you should aim to have about 20 different feeds that cover
  the same broad topics.  By default, articles that are more than 36 hours old
  are discarded.

