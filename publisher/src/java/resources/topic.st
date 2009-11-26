<div class="topic">
  <h2>$first(it.articles):{
    <a href="$it.item.articleURL$">$it.item.headline$</a><!-- $it.weight$ -->
    <a href="$it.item.feedLogo.articleURL$">$if(it.item.feedLogo)$<img
       src="$it.item.feedLogo.imageURL$" alt="$it.item.feedTitle$"
       title="$it.item.feedTitle$" class="logo" onerror="this.style.display='none'"/>$endif$</a>}$
  </h2>  
  $first(it.images):picture()$
  <div class="links">
    $rest(it.articles):link()$
  </div>
</div>