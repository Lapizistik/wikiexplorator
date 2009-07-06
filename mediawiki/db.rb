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
# read (if they exist):
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
#    If your tables have a prefix "<i>prefix_</i>" in the database use
#    "<i>prefix_</i><tt>wio_genres</tt>".
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
#
#    If your tables have a prefix "<i>prefix_</i>" in the database use
#    "<i>prefix_</i><tt>wio_roles</tt>".

require 'set'
require 'dbi'      # generic database engine
require 'mediawiki/dbfields'

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

    # Setup for a new database connection. The connection itself is initiated
    # by calling #connect.
    #
    # _db_:: database name
    # _host_:: database host
    # _user_:: database user
    # _pw_:: database password
    # _options_:: 
    #   a hash with further options:
    #   <tt>:engine</tt>=><tt>"Mysql"</tt>:: the database engine to be used.
    #   <tt>:driver</tt> :: The driver for the ODBC engine.
    #   <tt>:port</tt> :: database port (of not default port)
    #   <tt>:connectionstring</tt> ::
    #     if a connectionstring is given it is used as provided and 
    #     _db_, _host_, <tt>:engine</tt>, <tt>:port</tt> are ignored.
    #   <tt>:prefix</tt>=><tt>""</tt> :: table names prefix
    #   <tt>:version</tt>=><tt>1.8</tt>:: 
    #     used, if reading the database is different in different Mediawiki 
    #     versions. Not really implemented until now!
    # 
    # Creating DBI connections is a mess, as the connection string heavily 
    # depends on the engine used. So if connecting does not work try to 
    # provide a connection string.
    #
    # You may get problems if you have a ";" or "{" in your password 
    # (I do not know how to fix this, see source code).
    #
    # Patches on this are certainly welcome.
    def initialize(db, host, user, pw, options={})
      @db = db
      @host = host
      @dbuser = user
      @dbpassword = pw
      @version = options[:version] || 1.8
      @prefix = options[:prefix] || ''
      dbengine = options[:engine] || 'Mysql'
      driver = options[:driver]
      port = options[:port]

      @dbs = options[:connectionstring] || 
        begin
          # now things get complicated as the DBI connection string is DBD 
          # specific (and may depend on whatever). It's all a big mess as
          # you can see here:
          # http://www.connectionstrings.com/Articles/Show/important-rules-for-connection-strings
          # (and the roles given there do not really apply everywhere).
          s = "DBI:#{dbengine}:database=#{@db};"
          s << "driver=#{driver};" if driver
          s << case dbengine.downcase # some DBD want "host", others "server"
               when 'mysql','pg'
                 'host'
               when 'odbc'
                 'server'
               else # unknown database engine
                 'server'  # hm, what should be the default here?
               end
          s << "=#{@host}"
          s << ";port=#{port}" if port
          # We include username and passwort in the connectionstring
          # as ODBC with Mysql DBD did not work with user and password provided
          # as additional parameters (when I tried on Windows).
          s << ";user=#{@dbuser}"
          # As "{" and ";" have special meaning and I did not find any
          # quoting mechanism working with all drivers (and I tried a lot) 
          # we only add the password if it does not include these characters:
          s << ";password=#{@dbpassword}" unless (@dbpassword.include?(?;) ||
                                                  @dbpassword.include?(?{))
          s 
        end
    end

    def connect
      DBI.connect(@dbs, @dbuser, @dbpassword) do |dbh| 
        yield(MWDBI.new(dbh, @prefix))
      end
    end
    
    def to_s
      "#{@host}/#{@db}"
    end
   

    class MWDBI
      def initialize(dbh, prefix)
        @dbh = dbh
        @prefix = prefix
        @tables = @dbh.tables.to_set # What tables are there?
      end
      
      def users(&block)
        select_all(if @tables.include?('wio_user') # a special view for us!
                     'wio_user'
                   else
                     'user'
                   end,
                   FIELDS_USER, 
                   &block)
      end
      
      def usergroups(&block)
        select_all('user_groups', FIELDS_USER_GROUPS, &block)
      end
      
      def texts(&block)
        select_all('text', FIELDS_TEXT, &block)
      end
      
      def pages(&block)
        select_all('page', FIELDS_PAGE, &block)
      end
      
      def revisions(&block)
        select_all('revision', FIELDS_REVISION, &block)
      end
      
      def genres(&block) 
        select_all('wio_genres', FIELDS_GENRES, &block)
      end
      
      def roles(&block) 
        select_all('wio_roles', FIELDS_ROLES, &block)
      end
            
      # Select a set of columns from table _table_. 
      #
      # As we have to cope with very different Mediawiki database layouts
      # we do our very best in ignoring missing columns and tables.
      def select_all(table, fields=nil, &block)
        table = @prefix + table
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
