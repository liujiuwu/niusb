(function($) {
	"use strict";
	$.fn.simpeSlider = function(options) {
		var defaults = {
			slideshowSpeed : 4000
		}
		var opts = $.extend({}, defaults, options);
		return this.each(function() {
			var $this = $(this);
			var width = $this.width(); // 获取焦点图的宽度（显示面积）
			var len = $this.find('ul li').length; // 获取焦点图个数
			var idx = 0;
			var sliderTimer;

			var showSlider = function(showIdx) {
				var nowLeft = -showIdx * width;
				$this.find("ul").stop(true, false).animate({
					left : nowLeft,
				}, {
					queue : false
				});
				$this.find(".slider-btn span").stop(true, false).animate({
					opacity : "0.5"
				}).eq(showIdx).stop(true, false).animate({
					opacity : "1"
				});
			}

			var btn = "<div class='slider-btn-bg'></div><div class='slider-btn'>";
			for ( var i = 0; i < len; i++) {
				btn += "<span></span>";
			}
			btn += "</div><div class='preNext pre'></div><div class='preNext next'></div>";
			$this.append(btn);
			$this.find(".slider-btn-bg").css("opacity", 0.2);

			// 为小按钮添加鼠标滑入事件，以显示相应的内容
			$this.find(".slider-btn span").css("opacity", 0.4).mouseenter(function() {
				idx = $this.find(".slider-btn span").index(this);
				showSlider(idx);
			}).eq(0).trigger("mouseenter");

			$this.find(".preNext").css("opacity", 0.2).hover(function() {
				$(this).stop(true, false).animate({
					opacity : "0.5"
				});
			}, function() {
				$(this).stop(true, false).animate({
					opacity : "0.2"
				});
			});

			$this.find(".pre").click(function() {
				idx -= 1;
				if (idx <= -1) {
					idx = len - 1;
				}
				showSlider(idx);
			});

			$this.find(".next").click(function() {
				idx += 1;
				if (idx >= len) {
					idx = 0;
				}
				showSlider(idx);
			});

			// 本例为左右滚动，即所有li元素都是在同一排向左浮动，所以这里需要计算出外围ul元素的宽度
			$this.find("ul").css("width", width * len);
			$this.hover(function() {
				clearInterval(sliderTimer);
			}, function() {
				sliderTimer = setInterval(function() {
					showSlider(idx);
					idx++;
					if (idx == len) {
						idx = 0;
					}
				}, opts.slideshowSpeed);
			}).trigger("mouseleave");
		});
	}

	$.fn.simpeTab = function(options) {
		var defaults = {
			activeIdx : 0,
			activeCls : "txt1-active",
			tabWidth : 247,
			autoScroll : 0
		}

		var opts = $.extend({}, defaults, options);
		return this.each(function() {
			var tab = $(this);
			var len = tab.find(".js-tab").length;
			var activeTab = function(activeIdx) {
				var activeCls = opts.activeCls;
				var tabWidth = opts.tabWidth;
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
				$(window).scroll();
			}
			tab.find(".js-tab").hover(function() {
				activeTab(tab.find(".js-tab").index($(this)));
			});

			if (opts.autoScroll > 0) {
				var scrollIdx = 0;
				setInterval(function() {
					if (scrollIdx >= len) {
						scrollIdx = 0;
					}
					activeTab(scrollIdx++);
				}, opts.autoScroll);
			}
			activeTab(opts.activeIdx);
		});
	}

	$.fn.countdown = function(options) {
		var defaults = {
			waitTime : 60,
			waitBtnText : "秒后重新获取"
		}

		var opts = $.extend({}, defaults, options);
		var wt = opts.waitTime;
		var countDownTimer;
		var $btn = $(this);
		var btnText = $btn.text();
		var smsCodeCountdown = function() {
			if (wt <= 0) {
				$btn.removeClass("disabled").text(btnText);
				wt = opts.waitTime;
				clearTimeout(countDownTimer);
			} else {
				wt--;
				$btn.addClass("disabled").text(wt + opts.waitBtnText);
				countDownTimer = setTimeout(function() {
					smsCodeCountdown();
				}, 1000)
			}
		}
		return this.each(function() {
			smsCodeCountdown();
		});
	}
})(jQuery)

$(function() {
	$("img.lazy").lazyload({
		effect : "fadeIn"
	});
	$(window).scroll(function() {
		var scrolltop = $(document).scrollTop();
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
