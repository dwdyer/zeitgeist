<div class="theme">
  <h2>$first(it.item.articles):{<a href="$it.item.articleURL$">$it.item.headline$</a><!-- $it.weight$ -->}$</h2>
  $first(it.item.images):{<a href="$it.articleURL$"><img src="$it.imageURL$" class="picture" /></a>}$
  <ul>
    $rest(it.item.articles):{<li><a href="$it.item.articleURL$">$it.item.headline$</a><!-- $it.weight$ --></li>}$
  </ul>
</div>