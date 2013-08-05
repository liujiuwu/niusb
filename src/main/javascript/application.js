function mtab(selId, activeIdx, activeCls, tabWidth) {
	function activeTab(selId, activeIdx, activeCls, tabWidth) {
		$(selId + " .js-rline").animate({
			left : (activeIdx * tabWidth) + "px"
		}, {
			queue : false
		});
		$(selId + " .js-tab").removeClass(activeCls);
		$(selId + " .js-tab-c").hide();

		$(selId + " .js-tab:eq(" + activeIdx + ")").addClass(activeCls);
		$(selId + " .js-tab-c:eq(" + activeIdx + ")").show();
	}
	$(selId + " .js-tab").hover(function() {
		activeTab(selId, $(selId + " .js-tab").index($(this)), activeCls, tabWidth);
	});
	activeTab(selId, activeIdx, activeCls, tabWidth);
}

$(function() {

})