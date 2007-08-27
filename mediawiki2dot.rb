#! /usr/bin/ruby -w

require "mysql" 

def makedot(pw, user="wikiuser", db="wikidb", 
            host="www.kinf.wiai.uni-bamberg.de")
  
  my = Mysql::new(host, user, pw, db)
  
  pages = my.query("select * from page") 
  
  links = my.query("select * from pagelinks") 
  
  n2id={};id2n={}; 
  pages.each do |id,ns,title,rest|
    title.gsub!(/"(.*?)"/) { "»#{$1}«" }
    if title =~ /"/
      warn 'Einzelne " in title gefunden!' 
      title.gsub!(/"/,'')
    end
    if ns=="0"
      n2id[title]=id
      id2n[id]=title
    end
  end

  refs = []
  links.each do |id,ns,dest|
    dest.gsub!(/"(.*?)"/) { "»#{$1}«" }
    if dest =~ /"/
      warn 'Einzelne " in dest gefunden: '+dest+', deleting'
      dest.gsub!(/"/,'')
    end
    if ns=="0" 
      refs << [id2n[id],dest]
    end
  end

  dot = "digraph G {
"
  n2id.keys.each { |n| dot << '  "' + n +"\";\n" }
  refs.each { |s,d| dot << '  "' + s + '" -> "' + d +"\";\n" if (s&&d) }
  dot << "}\n"

  return dot
end


$stderr.print "DB-Passwort: "
system("stty  -echo") 
pw = $stdin.gets.chomp
system("stty  echo")
$stderr.print "\n" 
puts makedot(pw, *ARGV)
