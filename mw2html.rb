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

  # special markup of qualidative data analysis
  content = content + "\n\n{!end}"

  # bold
  content.gsub!(/'''(.*?)'''/) {"<strong>" + $1 + "</strong>"}
  # italics
  content.gsub!(/''(.*?)''/) {"<em>" + $1 + "</em>"}

  # headings
  i = 7 # levels of heading
  while i > 0 
    content.gsub!( /={#{i}}(.*?)={#{i}}/ ) { "<h#{i}>" + $1 + "</h#{i}>\n" }
    i = i - 1
  end
  
  # links, internal
  content.gsub!(/\[\[(.*?)\|(.*?)\]\]/) {"<a href=\"" + $1 + "\">" + $2 + "</a>"} # with piped text
  content.gsub!(/\[\[(.*?)\]\]/ ) {"<a href=\"" +  $1 + "\">" + $1 + "</a>"} # without piped text

  # links, external
  content.gsub!( /\[(.*?)\|(.*?)\]/ ) { "<a href=\"" + $1 + "\">" + $2 + "</a>" } # with piped text
  content.gsub!( /\[(.*?)\]/ ) { "<a href=\"" + $1 + "\">" + $1 + "</a>" } # without piped text
  
  # lists
  content.gsub!(/^:+/) {""} # remove indenting
  content.gsub!(/(\n[^*]*\n)((\*.*\n)+)/) {$1+"<ul>\n"+$2+"</ul>\n"} # unnumbered list
  content.gsub!(/(\n[^#]*\n)((#.*\n)+)/) {$1+"<ol>\n"+$2+"</ol>\n"} # ordered list
  content.gsub!(/\n[#*]+(.*)/) {"\n<li>"+$1+"</li>"} # list elements
  
  # tables
  # the table conversion barely works, cf. http://johbuc6.coconia.net/doku.php/mediawiki2html_machine/code?DokuWiki=7c542b97df2bc0f82fec0f4875265a20 for an implementation in PHP
  content.gsub!( /^\{\|(.*)/ ) { "<table" + $1 + ">" }
  content.gsub!( /\|\}/ ) { "</table>" }
  content.gsub!( /^\|-/ ) { "<tr>" }
  content.gsub!( /^!(.*)/ ) { "<th>" + $1 }
  content.gsub!( /^\|(.*)/ ) { "<td>" + $1 }

  # templates
  content.gsub!( /\{\{(.*?)\}\}/ ) { "<p><font color=\"#FF0000\">" + $1 + "</font></p>" }
  content.gsub!( /\{\{(.*?)\n*?(.*?)\}\}/ ) { "<p><font color=\"#FF0000\">" + $1 + " " + $2 + "</font></p>" }
  
  # special markup of qualidative data analysis
  content.gsub!( /\{title\}(.*)\{\/title\}/ ) { "<h1>\{title\}" + $1 + "\{\/title\}</h1>" }

  # line breaks
  content.gsub!( /(^\w.*)\n\n/ ) { "<p>" + $1 + "</p>\n\n" }
  content.gsub!( /(^<a.*)\n/ ) { $1 + "<br>\n" }
  
  return content
end

if ARGV.length>0
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

