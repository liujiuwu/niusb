module.exports = function(grunt) {
    grunt.initConfig({
	  less: {
		  development: {
			options: {
			  paths: ["assets/css"]
			},
			files: {
			  "src/main/webapp/css/bootstrap.css": "src/main/less/bootstrap_232/bootstrap.less",
		      "src/main/webapp/css/application.css": "src/main/less/application.less"
			}
		  },
		  production: {
			options: {
			  paths: ["assets/css"],
			  yuicompress: true,
				  report:'gzip'
			},
			files: {
			  "src/main/webapp/css/bootstrap-min.css": "src/main/less/bootstrap_232/bootstrap.less",
              "src/main/webapp/css/application-min.css": "src/main/less/application.less"
			}
		  }
	  }
	});

    grunt.loadNpmTasks('grunt-contrib-concat');
	grunt.loadNpmTasks('grunt-contrib-uglify');
	grunt.loadNpmTasks('grunt-contrib-coffee');
	grunt.loadNpmTasks('grunt-contrib-less');

    grunt.registerTask('build', ['less:development']);
};