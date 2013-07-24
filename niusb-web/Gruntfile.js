module.exports = function(grunt) {
	grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-contrib-coffee');
	grunt.loadNpmTasks('grunt-contrib-less');
	grunt.loadNpmTasks('grunt-contrib-clean');
	grunt.loadNpmTasks('grunt-contrib-watch');
	grunt.loadNpmTasks('grunt-hash');

	grunt
			.initConfig({
				pkg : grunt.file.readJSON('package.json'),
				dirs : {
					main : "src/main",
					test : "src/test",
					build : "grunt-build",
					target : "<%= dirs.build %>/out",
					temp : "<%= dirs.build %>/temp"
				},
				meta : {
					artifact : "<%= pkg.name %>-<%= pkg.version %>",
					concated : "<%= dirs.target %>/assets/<%= meta.artifact %>.js"
				},
				less : {
					compile : {
						files : {
							"<%= dirs.target %>/assets/<%= meta.artifact %>.css" : "<%= dirs.main %>/less/styles.less"
						}
					},
					compress : {
						options : {
							yuicompress : true
						},
						files : {
							"<%= dirs.temp %>/<%= meta.artifact %>.min.css" : "<%= dirs.main %>/less/styles.less"
						}
					}
				},
				uglify : {
					options : {
						banner : '/*! <%= pkg.name %>-<%= pkg.version %> <%= grunt.template.today("yyyy-mm-dd") %> */\n'
					},
					build : {
						src : "<%= meta.concated %>",
						dest : "<%= dirs.temp %>/<%= meta.artifact %>.min.js"
					}
				},
				hash : {
					options : {
						mapping : '<%= dirs.build %>/hash/assets.json',
					},
					src : [ "<%= dirs.temp %>/<%= meta.artifact %>.min.css" ],
					//dest : '<%= dirs.target %>/assets/'
				},
				watch : {
					less : {
						files : [ "<%= dirs.less %>/**/*.less" ],
						tasks : [ "less:compile" ]
					},
				}
			});

	grunt.registerTask('compile', [ 'less:compile' ]);
	grunt.registerTask('compress', [ 'less:compile', 'less:compress', 'hash' ]);
	grunt.registerTask('default', [ 'compile' ]);
};