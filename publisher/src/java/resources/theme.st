<div class="theme">
  <h2>
    $first(it.articles):{
      <a href="$it.item.feedLogo.articleURL$">$if(it.item.feedLogo)$<img src="$it.item.feedLogo.imageURL$" class="logo" onerror="this.style.display='none'"/> $endif$</a>
      <a href="$it.item.articleURL$">$it.item.headline$</a><!-- $it.weight$ -->
    }$
  </h2>
  $first(it.images):{<a href="$it.articleURL$"><img src="$it.imageURL$" class="picture"/></a>}$
  <div class="links">
    $rest(it.articles):{<a href="$it.item.articleURL$">$it.item.headline$</a><!-- $it.weight$ --><br />}$
  </div>
</div>