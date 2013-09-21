module.exports = function(grunt) {
	grunt.initConfig({
		pkg : grunt.file.readJSON('package.json'),
		setting : {
			srcLess : 'src/main/less',
			srcJs : 'src/main/javascript',
			distCss : 'src/main/webapp/css',
			distJs : 'src/main/webapp/js',
		},
		clean : {
			build : {
				src : [ "build" ]
			}
		},
		concat : {
			bootstrap : {
				src : [ '<%=setting.srcJs%>/bootstrap/*.js' ],
				dest : 'build/bootstrap.js'
			}
		},
		uglify : {
			main : {
				src : [ '<%=setting.srcJs%>/application.js' ],
				dest : '<%=setting.distJs%>/application.min.js'
			},
			other : {
				files : {
					'<%=setting.distJs%>/jquery-1.10.2.min.js' : [ '<%=setting.srcJs%>/jquery-1.10.2.js' ],
					'<%=setting.distJs%>/bootstrap.min.js' : [ '<%=setting.srcJs%>/bootstrap.js' ],
					'<%=setting.distJs%>/jquery.ui.widget.min.js' : [ '<%=setting.srcJs%>/jquery.ui.widget.js' ],
					'<%=setting.distJs%>/bootbox.min.js' : [ '<%=setting.srcJs%>/bootbox.js' ],
					'<%=setting.distJs%>/bootstrap-modal.min.js' : [ '<%=setting.srcJs%>/bootstrap-modal.js' ],
					'<%=setting.distJs%>/bootstrap-modalmanager.min.js' : [ '<%=setting.srcJs%>/bootstrap-modalmanager.js' ],
					'<%=setting.distJs%>/jquery.fileupload.min.js' : [ '<%=setting.srcJs%>/jquery.fileupload.js' ],
					'<%=setting.distJs%>/jquery.iframe-transport.min.js' : [ '<%=setting.srcJs%>/jquery.iframe-transport.js' ],
					'<%=setting.distJs%>/jquery.jcrop.min.js' : [ '<%=setting.srcJs%>/jquery.jcrop.js' ],
					'<%=setting.distJs%>/jquery.lazyload.min.js' : [ '<%=setting.srcJs%>/jquery.lazyload.js' ]
				}
			}
		},
		recess : {
			options : {
				compile : true
			},
			mainCompile : {
				files : {
					'build/application.css' : [ '<%=setting.srcLess%>/application.less' ],
					'build/admin.css' : [ '<%=setting.srcLess%>/admin.less' ],
					'build/user.css' : [ '<%=setting.srcLess%>/user.less' ]
				}
			},
			thirdCompile : {
				files : {
					'build/bootstrap.css' : [ '<%=setting.srcLess%>/bootstrap/bootstrap.less' ]
				}
			},
			mainMin : {
				options : {
					compress : true
				},
				files : {
					'<%=setting.distCss%>/application.min.css' : [ '<%=setting.srcLess%>/application.less' ],
					'<%=setting.distCss%>/admin.min.css' : [ '<%=setting.srcLess%>/admin.less' ],
					'<%=setting.distCss%>/user.min.css' : [ '<%=setting.srcLess%>/user.less' ]
				}
			},
			thirdMin : {
				options : {
					compress : true
				},
				files : {
					'<%=setting.distCss%>/bootstrap.min.css' : [ '<%=setting.srcLess%>/bootstrap/bootstrap.less' ],
					'<%=setting.distCss%>/font-awesome.min.css' : [ '<%=setting.srcLess%>/font-awesome.css' ],
					'<%=setting.distCss%>/bootstrap-modal-bs3patch.min.css' : [ '<%=setting.srcLess%>/bootstrap-modal-bs3patch.css' ],
					'<%=setting.distCss%>/bootstrap-modal.min.css' : [ '<%=setting.srcLess%>/bootstrap-modal.css' ],
					'<%=setting.distCss%>/jquery.fileupload-ui.min.css' : [ '<%=setting.srcLess%>/jquery.fileupload-ui.css' ],
					'<%=setting.distCss%>/jquery.jcrop.min.css' : [ '<%=setting.srcLess%>/jquery.jcrop.css' ]
				}
			}
		},
		watch : {
			mainRecess : {
				files : [ '<%=setting.srcLess%>/application.less', '<%=setting.srcLess%>/admin.less', '<%=setting.srcLess%>/user.less' ],
				tasks : [ 'dist-compile-main', 'dist-recess-main' ]
			},
			thirdRecess : {
				files : [ '<%=setting.srcLess%>/application.less', '<%=setting.srcLess%>/bootstrap/*.less' ],
				tasks : [ 'dist-recess-third' ]
			},
			mainJs : {
				files : [ '<%=setting.srcJs%>/application.js' ],
				tasks : [ 'dist-js-main' ]
			}
		},
	});

	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-recess');
	grunt.loadNpmTasks('grunt-contrib-clean');
	grunt.loadNpmTasks('grunt-contrib-watch');

	grunt.registerTask('dist-compile-main', [ 'recess:mainCompile' ]);
	grunt.registerTask('dist-compile-third', [ 'recess:thirdCompile' ]);
	grunt.registerTask('dist-recess-main', [ 'recess:mainMin' ]);
	grunt.registerTask('dist-recess-third', [ 'recess:thirdMin' ]);
	grunt.registerTask('dist-js-main', [ 'uglify:main' ]);
	grunt.registerTask('dist-js', [ 'uglify' ]);
	grunt.registerTask('dist-css', [ 'recess' ]);
	grunt.registerTask('default', [ 'clean', 'dist-js', 'dist-css' ]);

};