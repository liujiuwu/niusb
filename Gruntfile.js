module.exports = function(grunt) {

  grunt.initConfig({
    pkg: grunt.file.readJSON('package.json'),
	setting: {
		less:'src/main/less',
		css:'src/main/webapp/css',
		js:'src/main/webapp/js',
	},
    clean: {
	  build: {
		src: ["build", "dist"]
	  }
	},
	concat: { 
      bootstrap: { 
        src: ['src/bootstrap/js/*.js'], 
        dest: 'build/bootstrap.js' 
      } 
    }, 
	uglify: {
      bootstrap: {
        src: ['<%=concat.bootstrap.dest%>'],
        dest: '<%=setting.js%>/bootstrap.min.js'
      }
    },
	less: {
	  bootstrap: {
		options: {
		  yuicompress: true
		},
		files: {
		  "<%=setting.css%>/bootstrap.min.css": "<%=setting.less%>/bootstrap/less/bootstrap.less",
		  "<%=setting.css%>/bootstrap-responsive.min.css":"<%=setting.less%>/bootstrap/less/responsive.less"
		}
	  }
	},
	watch: { 
      files: '<%=setting.less%>/**/*.less', 
      tasks: 'less' 
    }, 
  });


  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-contrib-less');
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-watch');


  grunt.registerTask('default', ['clean','concat','uglify','less']);

};