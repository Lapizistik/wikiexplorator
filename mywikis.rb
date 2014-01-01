#!/usr/bin/ruby -w
# :title: MWParser Startup File
# = MWParser Startup File
# This file requires the full set of mwparser libraries and some 
# convenient shortcuts. You can directly require this file instead of 
# <tt>mediawiki.rb</tt>.
#
# Use this file as template to create your own startup file
#
# For stand alone usage:
#  > ruby mywikis.rb
#  ...
#  Connecting to your wiki ...
#  Password: ...
#  ... [some statistics] ...
# or:
#  > ruby mywikis.rb report
#  ...
#  Connecting to your wiki ...
#  Password: ...
#  ... [some statistics] ...
#  generating report
#  ...
#  /tmp/mw-report/default.pdf
# this is a simplified report of your wiki as availible by our web interface: 
# <http://www.kinf.wiai.uni-bamberg.de/mwstat/>. 
# 
# For usage in irb:
#  > irb -r mywikis
#  irb> wiki = MyWiki.open('****') # replace '****' by the password
#  ...
#
# See the description of class MyWiki for details.
#

require 'mediawiki/full'
require 'pp'

module Mediawiki

  # Creates a new Wiki object from the mywiki database.
  # (using 
  #
  # _pw_ is the database password.
  def Mediawiki.mywiki(pw, options={})
    Wiki.open("wikidb", "localhost", "wikiuser", pw,
              {:language => 'de', :name => 'MyWiki'}.merge(options))
  end

  # Creates a new Wiki object from the otherwiki database using
  # odbc.
  def Mediawiki.otherwiki(pw, options={})
    Wiki.open("otherwiki", "localhost", "wiki-reader", pw,
              {:language => 'en', :name => 'Other',
                :engine => 'odbc', :driver => '{MySQL ODBC 5.1 Driver}'
              }.merge(options))
  end
end

# For easy usage in irb: just use this module and be happy:
#  > irb -r mywiki
#  irb> wiki = MyWiki.open('****') # replace '****' by the password
#  irb> wiki.filter.deny_user(*MyWiki::Interns)
#  irb> wiki.pp_typelinkusers
#
# ToDo: Some introductionary documentation and examples.
module MyWiki
  Interns = []   # list of users to exclude
  class << self
    def open(pw)
      Mediawiki.mywiki(pw)
    end
  end
end

# For easy usage in irb: just use this module and be happy:
#  > irb -r mywiki
#  irb> wiki = OtherWiki.open('****') # replace '****' by the password
#  irb> wiki.filter.deny_user(*OtherWiki::Interns)
#  irb> wiki.pp_typelinkusers
module OtherWiki
  Interns = [1,2,6]   # list of users to exclude
  class << self
    def open(pw)
      Mediawiki.otherwiki(pw)
    end
  end
end


# Standalone:
if __FILE__ == $0
  puts "\n\n\n"
  puts "Connecting to your wiki ..."
  wiki = Mediawiki.mywiki(IO.getpw)

  ## Examples:

  # Number of users per page:
  nusers = wiki.pages.collect { |p| p.users.length }
  puts "Avg. # of users: #{nusers.inject(0.0) { |s,i| s+i }/nusers.length}"
  l = (nusers.select { |i| i>1 }).length
  puts "# of pages with more than one user: %d/%d (%.2f%%)" % 
    [l, nusers.length, l.to_f/nusers.length*100]

  if ARGV.shift == 'report'
    puts 'generating report'
    puts Mediawiki::Report.new(wiki, :pdf, 
                               :template => 'web',
                               :outputdir => 'mw-report').generate
    puts '...done (see filename given above if successful)'
  else
    puts "\n"
    puts 'give "report" as command line parameter to create a pdf report of your wiki.'
    puts "\n"
  end
end

