#! /usr/bin/ruby -w

# mw2html.rb
# Copyright 2007
# Steffen Blaschke
# Research Center for New Communication Media
# University of Bamberg
# steffen.blaschke[at]split.uni-bamberg.de
#
# mw2html.rb converts MediaWiki markup in HTML.

# run: script.rb filename*.*
# trial: script.rb < filename.html # z.B. ./tagesschau.rb < index.html\?p\=444 | grep '<h2>'| less

def replace(content)
  
  # pre-structuring
  content.gsub!( /^(\w.*)\n(\w.*)/ ) { "#{$1} #{$2}" } # remove single line breaks between text in order to avoid false paragraph markup
  content.gsub!( /<!--/ ) { "--" } # replace opening html commentaries
  content.gsub!( /-->/ ) { "--" } # replace closing html commentaries
  
  # templates
  content.gsub!( /\{\{Zitat\|(.*)\|.*\}\}\s*$\n/ ) { "\{p\}\"#{$1}\"\{/p\}\n" } # citation
  content.gsub!( /\{\{.*(Finale Version).*\}\}/ ) { "<font color=\"#FF0000\">#{$1}</font><br>" } # final version
  content.gsub!( /\{\{Vorlage:(Dokument.*)\n*(.*)\}\}/ ) { "<font color=\"#FF0000\">#{$1} #{$2}</font><br>" } # document status
  content.gsub!( /\{\{.*\}\} *$/ ) { '' } # remove single line templates
  content.gsub!( /\{\{.*\n?.*\}\} *$/ ) { '' } # remove all remaining templates
  
  # tags
  content.gsub!( /<nowiki>(.*?)<\/nowiki>/ ) { "<pre>#{$1}</pre>" }
  content.gsub!( /^ +(.*)/ ) { "<pre>#{$1}</pre>" }

  # special characters
  content.gsub!(/\{/,'(') # left braces
  content.gsub!(/\}/,')') # right braces
  
  # special markup of qualidative data analysis
  content = content + "\n\n{!end}"
  
  # categories
  content.gsub!( /\[\[Kategorie(.*?)\]\]/i ) { "{category}<font color=\"#FF0000\">Kategorie:#{$1}</font>{/category}<br>" }
  
  # images
  content.gsub!( /\[\[Bild(?:.*)\|*(.*)\]\]/i ) { "{image}<font color=\"#FF0000\">Image#{$1}</font>{/image}<br>"}

  # bold
  content.gsub!(/'''(.*?)'''/) {"<strong>#{$1}</strong>"}
  content.gsub!(/'''(.*?)$/) {"<strong>#{$1}</strong>"}

  # italics
  content.gsub!(/''(.*?)''/) {"<em>#{$1}</em>"}
  content.gsub!(/''(.*?)$/) {"<em>#{$1}</em>"}

  # headings
  6.downto(1) { |i| content.gsub!( /^={#{i}}(.*?)={#{i}} *$/ ) { "<h#{i}>\{h#{i}\}#{$1}\{/h#{i}\}</h#{i}>" } }
  
  # links, internal
  content.gsub!( /\[\[([^\[\n]*?)\| *(.*?)\]\]/ ) { "<a class=\"internal\" href=\"#{$1}\">#{$2}</a>" } # with piped text
  content.gsub!( /\[\[(.*?)\]\]/ ) { "<a class=\"internal\" href=\"#{$1}\">#{$1}</a>" } # without piped text

  # links, external
  content.gsub!( /\[([^\[\n]*?)\| *(.*?)\]/ ) { "<a class=\"external\" href=\"#{$1}\">#{$2}</a>" } # with piped text
  content.gsub!( /\[(.*?)\]/ ) { "<a class=\"external\" href=\"#{$1}\">#{$1}</a>" } # without piped text
  
  # lists
  content.gsub!(/^:+/,'') # remove forced indenting
  content.gsub!( /^((?:\*.*\n+)+)/ ) { "\{ul\}<ul>\n#{$1.split(/\n/).collect { |line| "<li>#{line.gsub!(/^\*/,'')}</li>\n" }.join}</ul>\{/ul\}<br>\n" } # first level ul
  content.gsub!( /^((?:#.*\n+)+)/ ) { "\{ol\}<ol>\n#{$1.split(/\n/).collect { |line| "<li>#{line.gsub!(/^#/,'')}</li>\n" }.join}</ol>\{/ol\}<br>\n" } # first level ol
  content.gsub!( /<li>\s*<\/li>\n/ ) { '' } # remove empty list entries (this may occur if first-level wiki-lists are entered with \n\n; they look like a single list when, in fact, they are but multiple single lists)
  content.gsub!( /^((?:<li>\*.*\n+)+)/ ) { "<ul>\n#{$1.gsub(/^<li>\*/,"<li>")}</ul>\n" } # second level ul
  content.gsub!( /^((?:<li>#.*\n+)+)/ ) { "<ol>\n#{$1.gsub(/^<li>#/,"<li>")}</ol>\n" } # second level ol
  content.gsub!( /^((?:<li>\*.*\n+)+)/ ) { "<ul>\n#{$1.gsub(/^<li>\*/,"<li>")}</ul>\n" } # third level ul
  content.gsub!( /^((?:<li>#.*\n+)+)/ ) { "<ol>\n#{$1.gsub(/^<li>#/,"<li>")}</ol>\n" } # third level ol

  # tables
  # the table conversion barely works, cf. http://johbuc6.coconia.net/doku.php/mediawiki2html_machine/code?DokuWiki=7c542b97df2bc0f82fec0f4875265a20 for an implementation in PHP
  content.gsub!( /^\{\|(.*)/ ) { "\{table\}\n<table #{$1}>" }
  content.gsub!( /\|\}/ ) { "</table>\n\{/table\}" }
  content.gsub!( /^\|-(.*)/ ) { "<tr>#{$1}" }
  content.gsub!( /^!(.*?)\|(.*)/ ) { "<th #{$1}>#{$2}</th>" } # table header with piped markup
  content.gsub!( /^!(.*)/ ) { "<th>#{$1}</th>" } # table header without piped markup
  content.gsub!( /^\|(.*?)\|(.*)/ ) { "<td #{$1}>#{$2}" } # table data with piped markup
  content.gsub!( /^\|(.*)/ ) { "<td>#{$1}" } # table data without piped markup
  
  # special markup of qualidative data analysis
  content.gsub!( /(\{title\}.*\{\/title\})/ ) { "<h1>#{$1}</h1>" }

  # line breaks
  content.gsub!( /(^(?:\w|<strong|<em|<a|\").*)\n\s*\n/ ) {"<p>\{p\}#{$1}\{/p\}</p>\n"}
  
#  //$html = nl2br($html);
#  	// line breaks
#  	$html = preg_replace('/[\n\r]{4}/',"<br/><br/>",$html);
#  	$html = preg_replace('/[\n\r]{2}/',"<br/>",$html);

#  	$html = preg_replace('/[>]<br\/>[<]/',"><",$html);
  
#  // allowed tags
#  	$html = preg_replace('/&lt;(\/?)(small|sup|sub|u)&gt;/','<${1}${2}>',$html);
#
#  	$html = preg_replace('/\n*&lt;br *\/?&gt;\n*/',"\n",$html);
#  	$html = preg_replace('/&lt;(\/?)(math|pre|code|nowiki)&gt;/','<${1}pre>',$html);
#  	$html = preg_replace('/&lt;!--/','<!--',$html);
#  	$html = preg_replace('/--&gt;/',' -->',$html);
#  
  return content

end

if ARGV.length > 0
  ARGV.each do |filename|
    content = IO.read(filename)
    content = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">
    <html>
       <head>
          <meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">
          <title>" + filename.gsub(/txt/,'html') + "</title>
       </head>
       <body>
       " + replace(content) +
       "
       </body>
    </html>"
    File.open(filename.gsub(/txt/,'html'),'w') { |file| file << content }
  end
else
  puts replace($stdin.read)
end
