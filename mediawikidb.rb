#!/usr/bin/ruby -w
# :title: MediawikiDB
# = Mediawiki Generic Database Connector

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

class MediawikiDB

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
