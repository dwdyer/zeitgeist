<div class="theme">
  <h2>
    $first(it.articles):{
      <a href="$it.item.articleURL$">$it.item.headline$</a><!-- $it.weight$ -->
      <a href="$it.item.feedLogo.articleURL$">$if(it.item.feedLogo)$<img
        src="$it.item.feedLogo.imageURL$" alt="$it.item.feedTitle$"
        title="$it.item.feedTitle$" class="logo" onerror="this.style.display='none'"
      />$endif$</a>
    }$
  </h2>
  <div class="picture">
    $first(it.images):{<a href="$it.articleURL$"><img src="$it.imageURL$"/></a><br />
                       <a href="$it.articleURL$" class="credit">$it.imageCredit$</a>}$
  </div>
  <div class="links">
    $rest(it.articles):{
      <img src="$it.item.feedIcon.imageURL$" alt="$it.item.feedTitle$" title="$it.item.feedTitle$"
           class="icon" onerror="this.style.visibility='hidden'"/>
      <a href="$it.item.articleURL$">$it.item.headline$</a><!-- $it.weight$ --><br />
    }$
  </div>
</div>