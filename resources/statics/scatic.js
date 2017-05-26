// scatic.js

/*
 * Filter by value
 * value: category name or tag name
 */
function filterBy(value) {
  $('.blog-list-container').each(function() {
    $(this).attr('class', 'blog-list-container hidden');
  });

  var list = $("#"+value+'-container');
  if (list.length) {
    list.attr('class', 'blog-list-container');
  }
}

/**********************/
var byCategoryPagename = "byCategory.html";
var byTagPagename = "byTag.html";
var currentPagename = window.location.pathname.replace('/', '');

switch (currentPagename) {
  case byCategoryPagename:
    if (window.location.hash) {
      var cat = window.location.hash.split('#')[1];
      filterBy(cat);
    }
    break;
  case byTagPagename:
    if (window.location.hash) {
      var tag = window.location.hash.split('#')[1];
      filterBy(tag);
    }
    break;
  default:
    break;
}
