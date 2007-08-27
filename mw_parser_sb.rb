#! /usr/bin/ruby -w

require "mysql" 

class MWText
  attr_accessor :id, :text, :title

  def initialize(id, title=nil, text=nil)
    @id = id
    @title = title
    @text = text
  end

  def to_s
    "{id}#{@id}{/id}: {title}#{@title}{/title}

{text}
#{@text.gsub(/\{/,"\342\206\222").gsub(/\}/,"\342\206\220")}
{/text}"
  end
end

def readtext(pw, user="mfg-reader", db="mfg", host="kinf01.kinf.wiai.uni-bamberg.de")
  
  mysql = Mysql::new(host,user,pw,db)
  
  pages = mysql.query("select old_id, page_title, old_text	from page, revision, text	where page_is_redirect = 0 and page_namespace = 0	and page_latest = rev_id and rev_text_id = old_id")

  mwtext = Hash.new
  pages.each do |id,title,text|
    id = id.to_i
    mwtext[id] = MWText.new(id,title,text)
  end

  return mwtext
end

def writemwtext(mwtext)
  mwtext.each do |k,v|
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

writemwtext(readtext(getpw, *ARGV))
