<?xml version="1.0" encoding="utf-8" ?>
<html>
  <head>
    <title>$title$</title>
    <link href="zeitgeist.css" rel="stylesheet" type="text/css" />
  </head>
  <body>
    <div id="snippet">
      $first(topics):{
        $first(it.images):{
          <div class="picture">
            <a href="$it.articleURL$"><img src="$it.cachedFileName$"/></a><br />
            <a href="$it.articleURL$" class="credit">$it.imageCredit$</a>
          </div>}$
          <strong><big>$first(it.articles):link()$</big></strong>
      }$
      $rest(topics):{$first(it.articles):link()$}$
    </div>
  </body>
</html>