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
# If a table/view <tt>wio_user</tt> exists it is read instead of table +user+.
# This allows creating a view <tt>wio_user</tt> which does not include the
# password fields.
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

require 'set'

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
      require 'dbi'      # generic database engine

      DBI.connect("DBI:#{@dbengine}:#{@db}:#{@host}", 
                  @dbuser, @dbpassword) { |dbh| yield(MWDBI.new(dbh)) }
    end
    
    def to_s
      "#{@host}/#{@db}"
    end
   

    class MWDBI
      def initialize(dbh)
        @dbh = dbh
        @tables = @dbh.tables.to_set # What tables are there?
      end
      
      def users(&block)
        select_all(if @tables.include?('wio_user') # a special view for us!
                     'wio_user'
                   else
                     'user'
                   end,
                   %w{user_id user_name user_real_name user_email 
                      user_options user_touched user_email_authenticated 
                      user_email_token_expires user_registration 
                      user_newpass_time user_editcount}, 
                   &block)
      end
      
      def usergroups(&block)
        select_all("user_groups", %w{ug_user ug_group}, &block)
      end
      
      def texts(&block)
        select_all("text", %w{old_id old_text old_flags}, &block)
      end
      
      def pages(&block)
        select_all("page", 
                   %w{page_id page_namespace page_title page_restrictions 
                      page_counter page_is_redirect page_is_new page_random 
                      page_touched page_latest page_len}, 
                   &block)
      end
      
      def revisions(&block)
        select_all("revision", 
                   %w{rev_id rev_page rev_text_id rev_comment rev_user 
                      rev_user_text rev_timestamp rev_minor_edit rev_deleted},
                   &block)
      end
      
      def genres(&block) 
        select_all("wio_genres", %w{page_id genres}, &block)
      end
      
      def roles(&block) 
        select_all("wio_roles", %w{user_id roles}, &block)
      end
            
      # Select a set of columns from table _table_. 
      #
      # As we have to cope with very different Mediawiki database layouts
      # we do our very best in ignoring missing columns and tables.
      def select_all(table, fields=nil, &block)
        # Let's see if our table exists in the DB:
        if @tables.include?(table) # is it there?
          if fields
            # Ok, what columns are provided?
            cols = @dbh.columns(table).collect { |c| c.name }.to_set
            fstring = fields.collect { |f|
              if cols.include?(f) # ok, col is there
                f
              else # Damn, it's missing, we will return nils here.
                puts "Column #{f}.#{table} not found! Using NULL." if DEBUG
                'NULL'
              end
            }.join(', ')
          else
            fstring = '*'
          end
          @dbh.select_all("select #{fstring} from #{table}", &block)
        else
          puts "Table #{table} in DB not found" if DEBUG
          return []
        end
      end
    end
  end
end
