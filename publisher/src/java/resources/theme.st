<div class="theme">
  <h2>$first(it.articles):{<a href="$it.item.articleURL$">$it.item.headline$</a><!-- $it.weight$ -->}$</h2>
  $first(it.images):{<a href="$it.articleURL$"><img src="$it.imageURL$" class="picture" /></a>}$
  <ul>
    $rest(it.articles):{<li><a href="$it.item.articleURL$">$it.item.headline$</a><!-- $it.weight$ --></li>}$
  </ul>
</div>