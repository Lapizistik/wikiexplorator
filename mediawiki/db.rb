#!/usr/bin/ruby -w
# :title: MediawikiDB
# = Mediawiki Generic Database Connector
#
# The database connector. The database class Mediawiki::DB encapsulates
# all functionality needed to read a Mediawiki database (currently only
# Mysql but subject to change soon).
#
# The following tables are read: 
# +user+, <tt>user_groups</tt>, +text+, +page+, +revision+.
#
# Additionally the tables <tt>wio_genres</tt> and <tt>wio_roles</tt> are
# read (TODO: if existent):
#
# <tt>wio_genres</tt>::
#    You may manually annotate some or all pages (we use
#    TAWS for this) and create an additional table in the 
#    database:
#      CREATE TABLE 'wio_genres' ('page_id' INT(8),'genres' VARCHAR(255));
#    with the pid of the annotated page in the first column and a
#    comma-separated list of strings representing the genres in the
#    second.  If a Page is in this table, Page#genres is a Set 
#    containing all genres found.  
#
#    TODO: better in Revision?
# <tt>wio_roles</tt>::
#    If you are able to assign the Users of the Wiki with roles you may
#    create an additional table in the database:
#      CREATE TABLE 'wio_roles' ('user_id' INT(8),'roles' VARCHAR(255));
#    with the uid of the User in the first column and a
#    comma-separated list of strings representing her roles in the
#    second.  If a User is in this table, User#roles is a Set 
#    containing all roles found.  

require 'dbi'      # generic database engine

# Asks for user input with echo off at console
def IO.getpw(question="Password: ")
  $stderr.print(question)
  system("stty  -echo") 
  pw = $stdin.gets.chomp
  system("stty  echo")
  $stderr.print "\n" 
  return pw
end


module Mediawiki
  class DB

    def initialize(db, host, user, pw, dbengine='Mysql', version=1.8)
      @db = db
      @host = host
      @dbuser = user
      @dbpassword = pw
      @dbengine = dbengine
      @version = version
    end
    
    def connect
      DBI.connect("DBI:#{@dbengine}:#{@db}:#{@host}", 
                  @dbuser, @dbpassword) { |dbh| yield(MWDBI.new(dbh)) }
    end
    
    def to_s
      "#{@host}/#{@db}"
    end
   

    class MWDBI
      def initialize(dbh)
        @dbh = dbh
      end
      
      def users(&block) # TODO: cope with different mediawiki versions
        @dbh.select_all("select user_id, user_name, user_real_name, user_email, user_options, user_touched, user_email_authenticated, user_email_token_expires, user_registration, user_newpass_time, user_editcount from user", &block)
      end
      
      def usergroups(&block)
        @dbh.select_all("select * from user_groups", &block)
      end
      
      def texts(&block)
        @dbh.select_all("select * from text", &block)
      end
      
      def pages(&block)
        @dbh.select_all("select * from page", &block)
      end
      
      def revisions(&block)
        @dbh.select_all("select * from revision", &block)
      end
      
      # TODO: should work regardless of the table existing in the database.
      def genres(&block) 
        @dbh.select_all("select * from wio_genres", &block)
      end
      
      # TODO: should work regardless of the table existing in the database.
      def roles(&block) 
        @dbh.select_all("select * from wio_roles", &block)
      end
            
      
      # Fallback, not to be used.
      def select_all(table, fields=nil, &block)
        if fields
          f = fields.join(', ')
        else
          f = '*'
        end
        @dbh.select_all("select #{f} from #{table}", &block)
      end
    end
  end
end
