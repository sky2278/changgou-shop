// window.onload = function() {
// 	cartListController.setup();
// }

/*商品分类*/
$(function() {
	$('.all-sort-list2 > .items').hover(function() {
		//父类分类列表容器的高度

		$(this).addClass('hover');
		$(this).children('.items-list').css('display', 'block');
	}, function() {
		$(this).removeClass('hover');
		$(this).children('.items-list').css('display', 'none');
	});

	$('.items > .items-list > .close').click(function() {
		$(this).parent().parent().removeClass('hover');
		$(this).parent().hide();
	});
});