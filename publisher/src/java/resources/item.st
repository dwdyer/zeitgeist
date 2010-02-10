<item>
  $first(it.articles):{<title>$it.item.headline$</title>
  <link>$it.item.articleURL$</link>
  <pubDate>$it.item.date;format="EEE, dd MMM yyyy HH:mm:ss Z"$</pubDate>}$
  <description>
    $rest(it.articles):{&lt;ul&gt;
      &lt;li&gt;&lt;a href="$it.item.articleURL$"&gt;$it.item.headline$&lt;/a&gt;&lt;/li&gt;<!-- $it.weight$ -->
    &lt;/ul&gt;}$
  </description>
</item>