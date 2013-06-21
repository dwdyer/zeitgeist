Zeitgeist - An intelligent RSS news aggregator
(C) Copyright 2009-2013 Daniel W. Dyer

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

      java -jar zeitgeist-publisher-1.2.jar

  By default this will look for a file called zeitgeist.properties in the
  working directory.  Alternatively, you can specify the path to some other
  properties file as a command line argument.

  To get good results you should aim to have at least 20 different feeds that
  cover the same broad topics.
