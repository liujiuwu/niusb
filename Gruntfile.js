module.exports = function(grunt) {
	grunt.initConfig({
		pkg : grunt.file.readJSON('package.json'),
		setting : {
			srcLess : 'src/main/less',
			srcJs : 'src/main/javascript',
			distCss : 'src/main/webapp/css',
			distJs : 'src/main/webapp/js',
		},
		clean: {
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
			bootstrap : {
				src : [ '<%=concat.bootstrap.dest%>' ],
				dest : '<%=setting.distJs%>/bootstrap.min.js'
			},
			other : {
				files : {
					'<%=setting.distJs%>/jquery-1.10.2.min.js' : [ '<%=setting.srcJs%>/jquery-1.10.2.js' ],
					'<%=setting.distJs%>/jquery.ui.widget.min.js' : [ '<%=setting.srcJs%>/jquery.ui.widget.js' ],
					'<%=setting.distJs%>/bootbox.min.js' : [ '<%=setting.srcJs%>/bootbox.js' ],
					'<%=setting.distJs%>/bootstrap-modal.min.js' : [ '<%=setting.srcJs%>/bootstrap-modal.js' ],
					'<%=setting.distJs%>/bootstrap-modalmanager.min.js' : [ '<%=setting.srcJs%>/bootstrap-modalmanager.js' ],
					'<%=setting.distJs%>/jquery.fileupload.min.js' : [ '<%=setting.srcJs%>/jquery.fileupload.js' ],
					'<%=setting.distJs%>/jquery.iframe-transport.min.js' : [ '<%=setting.srcJs%>/jquery.iframe-transport.js' ],
					'<%=setting.distJs%>/jquery.jcrop.min.js' : [ '<%=setting.srcJs%>/jquery.jcrop.js' ],
				    '<%=setting.distJs%>/jquery.flexslider.min.js' : [ '<%=setting.srcJs%>/jquery.flexslider.js' ],
					'<%=setting.distJs%>/application.min.js' : [ '<%=setting.srcJs%>/application.js' ]
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
					'build/admin.css' : [ '<%=setting.srcLess%>/admin.less' ]
				}
			},
			thirdCompile : {
				files : {
					'build/bootstrap.css' : [ '<%=setting.srcLess%>/bootstrap/bootstrap.less' ],
					'build/bootstrap-responsive.css' : [ '<%=setting.srcLess%>/bootstrap/responsive.less' ],
					'build/font-awesome-ie7.css' : [ '<%=setting.srcLess%>/font-awesome/font-awesome-ie7.less' ]
				}
			},
			mainMin : {
				options : {
					compress : true
				},
				files : {
					'<%=setting.distCss%>/application.min.css' : [ '<%=setting.srcLess%>/application.less' ],
					'<%=setting.distCss%>/admin.min.css' : [ '<%=setting.srcLess%>/admin.less' ]
				}
			},
			thirdMin : {
				options : {
					compress : true
				},
				files : {
					'<%=setting.distCss%>/bootstrap.min.css' : [ '<%=setting.srcLess%>/bootstrap/bootstrap.less' ],
					'<%=setting.distCss%>/bootstrap-responsive.min.css' : [ '<%=setting.srcLess%>/bootstrap/responsive.less' ],
					'<%=setting.distCss%>/font-awesome-ie7.min.css' : [ '<%=setting.srcLess%>/font-awesome/font-awesome-ie7.less' ],
					'<%=setting.distCss%>/bootstrap-modal.min.css' : [ '<%=setting.srcLess%>/bootstrap-modal.css' ],
					'<%=setting.distCss%>/jquery.fileupload-ui.min.css' : [ '<%=setting.srcLess%>/jquery.fileupload-ui.css' ],
					'<%=setting.distCss%>/jquery.jcrop.min.css' : [ '<%=setting.srcLess%>/jquery.jcrop.css' ],
					'<%=setting.distCss%>/flexslider.min.css' : [ '<%=setting.srcLess%>/flexslider.css' ]
				}
			}
		},
		watch : {
			mainRecess : {
				files : [ '<%=setting.srcLess%>/application.less', '<%=setting.srcLess%>/admin.less' ],
				tasks : [ 'dist-compile-main','dist-recess-main' ]
			},
			thirdRecess : {
				files : [ '<%=setting.srcLess%>/application.less', '<%=setting.srcLess%>/bootstrap/*.less' ],
				tasks : [ 'dist-recess-third' ]
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
	grunt.registerTask('dist-js', [ 'concat', 'uglify' ]);
	grunt.registerTask('dist-css', [ 'recess' ]);
	grunt.registerTask('default', [ 'clean','dist-js', 'dist-css' ]);

};