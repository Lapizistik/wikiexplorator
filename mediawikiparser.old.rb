#! /usr/bin/ruby -w

require "mysql" 


class MWText
  attr_accessor :id, :text, :enc, :title

  def initialize(id, text=nil, enc="utf-8", title=nil)
    @id = id
    @text = text
    @enc = enc
    @title = title
  end

  def to_s
    "{id}#{@id}{/id}: {title}#{@title}{/title}

{text}
#{@text.gsub(/\{/,"\342\206\222").gsub(/\}/,"\342\206\220")}
{/text}"
  end
end


def readtext(pw, user="wikiuser", db="wikidb", 
             host="www.kinf.wiai.uni-bamberg.de")
  
  my = Mysql::new(host, user, pw, db)
  
  pages = my.query("select * from page") 

  texte = my.query("select * from text") 
  
  ids = Hash.new

  pages.each do |id, ns, title, restrictions, counter, redirect,
                 new, random, touched, latest, len|
    id = id.to_i
    ids[id] = title if ((ns=="0") && (redirect=="0"))
  end

  mwtexte = Hash.new
  texte.each do |id,text,flags|
    id = id.to_i
    mwtexte[id] = MWText.new(id,text,flags,ids[id]) if ids.key?(id)
  end

  return mwtexte
end

def writemwtexte(mwtexte)
  mwtexte.each do |k,v|
    File.open("mfg-%04d.txt" % k, 'w') do |file| 
      file << v.to_s
    end
  end
end

def getpw()
  $stderr.print "DB-Passwort: "
  system("stty  -echo") 
  pw = $stdin.gets.chomp
  system("stty  echo")
  $stderr.print "\n"
  return pw
end

writemwtexte(readtext(getpw, *ARGV))
