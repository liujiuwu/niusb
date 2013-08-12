(function($) {
	$.fn.extend({
		simpeSlider : function(options) {
			var defaults = {
				slideshowSpeed : 4000
			}

			var options = $.extend(defaults, options);

			return this.each(function() {
				var slider = $(this);
				var sliderWidth = slider.width(); // 获取焦点图的宽度（显示面积）
				var len = slider.find('ul li').length; // 获取焦点图个数
				var sliderIdx = 0;
				var picTimer;

				var btn = "<div class='slider-btn-bg'></div><div class='slider-btn'>";
				for ( var i = 0; i < len; i++) {
					btn += "<span></span>";
				}
				btn += "</div><div class='preNext pre'></div><div class='preNext next'></div>";
				slider.append(btn);
				slider.find(".slider-btn-bg").css("opacity", 0.5);

				// 为小按钮添加鼠标滑入事件，以显示相应的内容
				slider.find(".slider-btn span").css("opacity", 0.4).mouseenter(function() {
					sliderIdx = slider.find(".slider-btn span").index(this);
					showSlider(sliderIdx);
				}).eq(0).trigger("mouseenter");

				// 上一页、下一页按钮透明度处理
				slider.find(".preNext").css("opacity", 0.2).hover(function() {
					$(this).stop(true, false).animate({
						"opacity" : "0.5"
					}, 300);
				}, function() {
					$(this).stop(true, false).animate({
						"opacity" : "0.2"
					}, 300);
				});

				slider.find(".pre").click(function() {// 上一页按钮
					sliderIdx -= 1;
					if (sliderIdx <= -1) {
						sliderIdx = len - 1;
					}
					showSlider(sliderIdx);
				});

				slider.find(".next").click(function() {// 下一页按钮
					sliderIdx += 1;
					if (sliderIdx >= len) {
						sliderIdx = 0;
					}
					showSlider(sliderIdx);
				});

				// 本例为左右滚动，即所有li元素都是在同一排向左浮动，所以这里需要计算出外围ul元素的宽度
				slider.find("ul").css("width", sliderWidth * len);

				// 鼠标滑上焦点图时停止自动播放，滑出时开始自动播放
				slider.hover(function() {
					clearInterval(picTimer);
				}, function() {
					picTimer = setInterval(function() {
						showSlider(sliderIdx);
						sliderIdx++;
						if (sliderIdx == len) {
							sliderIdx = 0;
						}
					}, options.slideshowSpeed); // 此4000代表自动播放的间隔，单位：毫秒
				}).trigger("mouseleave");

				function showSlider(sliderIdx) { // 普通切换
					var nowLeft = -sliderIdx * sliderWidth; // 根据index值计算ul元素的left值
					slider.find("ul").stop(true, false).animate({
						"left" : nowLeft
					}, 300); // 通过animate()调整ul元素滚动到计算出的position
					slider.find(".slider-btn span").stop(true, false).animate({
						"opacity" : "0.4"
					}, 300).eq(sliderIdx).stop(true, false).animate({
						"opacity" : "1"
					}, 300); // 为当前的按钮切换到选中的效果
				}
			});
		}
	});
})(jQuery);

(function($) {
	$.fn.extend({
		simpeTab : function(options) {
			var defaults = {
				activeIdx : 0,
				activeCls : "txt1-active",
				tabWidth : 247,
				autoScroll : 0
			}

			var options = $.extend(defaults, options);
			return this.each(function() {
				var tab = $(this);
				var len = tab.find(".js-tab").length;
				function activeTab(activeIdx) {
					var activeCls = options.activeCls;
					var tabWidth = options.tabWidth;
					if (activeIdx >= len || activeIdx < 0) {
						activeIdx = 0;
					}

					tab.find(".js-rline").animate({
						left : (activeIdx * tabWidth) + "px"
					}, {
						queue : false
					});

					tab.find(".js-tab").removeClass(activeCls).eq(activeIdx).addClass(activeCls);
					tab.find(".js-tab-c").hide().eq(activeIdx).show();
				}
				tab.find(".js-tab").hover(function() {
					activeTab(tab.find(".js-tab").index($(this)));
				});
				activeTab(options.activeIdx);

				if (options.autoScroll > 0) {
					var scrollIdx = 0;
					setInterval(function() {
						if (scrollIdx >= len) {
							scrollIdx = 0;
						}
						activeTab(scrollIdx++);
					}, options.autoScroll);
				}
			});
		}
	});
})(jQuery);

$(document).ready(function() {
	$(window).scroll(function() {
		var scrolltop = $(document).scrollTop();
		/*
		 * if (scrolltop > 20) { $('#menu').addClass('fixed'); } else {
		 * $('#menu').removeClass('fixed'); }
		 */
		if (scrolltop > 20) {
			$('#toTop').fadeIn();
		} else {
			$('#toTop').fadeOut();
		}
	});
	$('#toTop').click(function() {
		$('body,html').animate({
			scrollTop : 0
		}, 600);
	});
});

$(function() {

})
