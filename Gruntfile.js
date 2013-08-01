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
        src: ['<%=setting.less%>/bootstrap/js/*.js'], 
        dest: 'build/bootstrap.js' 
      } 
    }, 
	uglify: {
      bootstrap: {
        src: ['<%=concat.bootstrap.dest%>'],
        dest: '<%=setting.js%>/bootstrap.min.js'
      }
    },
	recess: {
	  options: {
		compile: true
	  },
	  compile: {
		files: {
			'build/bootstrap.css': ['<%=setting.less%>/bootstrap/less/bootstrap.less'],
			'build/bootstrap-responsive.css':['<%=setting.less%>/bootstrap/less/responsive.less'],
			//'build/font-awesome.css':['<%=setting.less%>/font-awesome/font-awesome.less'],
			'build/font-awesome-ie7.css':['<%=setting.less%>/font-awesome/font-awesome-ie7.less']
		}
	  },
	  min: {
		options: {
			compress: true
		},
		files: {
			'<%=setting.css%>/bootstrap.min.css': ['<%=setting.less%>/bootstrap/less/bootstrap.less'],
			'<%=setting.css%>/bootstrap-responsive.min.css':['<%=setting.less%>/bootstrap/less/responsive.less'],
			//'<%=setting.css%>/font-awesome.min.css':['<%=setting.less%>/font-awesome/font-awesome.less'],
            '<%=setting.css%>/font-awesome-ie7.min.css':['<%=setting.less%>/font-awesome/font-awesome-ie7.less']
		}
	  }
	},
	watch: {  
	  recess: {
		files: '<%=setting.less%>/**/*.less',
		tasks: ['recess']
	  }
    }, 
  });


  grunt.loadNpmTasks('grunt-contrib-concat');
  grunt.loadNpmTasks('grunt-contrib-uglify');
  grunt.loadNpmTasks('grunt-recess');
  grunt.loadNpmTasks('grunt-contrib-clean');
  grunt.loadNpmTasks('grunt-contrib-watch');


  grunt.registerTask('dist-js', ['clean','concat','uglify']);
  grunt.registerTask('dist-css', ['recess']);
  grunt.registerTask('default', ['dist-js','dist-css']);

};