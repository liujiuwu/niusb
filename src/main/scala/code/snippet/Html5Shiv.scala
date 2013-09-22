package code.snippet

import scala.xml.Unparsed

object Html5Shiv {
  def render = Unparsed("""
    <!--[if lt IE 9]>
		  <script src="/js/html5shiv.min.js"></script>
		  <script src="/js/respond.min.js"></script>
    <![endif]-->""")
}