#!/usr/bin/ruby -w
# :title: Mediawiki XML Database Interface
# = Mediawiki XML Database Interface
#
# The XML-Database-Dump-Reader.
#
# Used to read MySQL database dumps created by
#
# <tt>mysqldump -p --xml</tt> _dbname_ <tt>user user_groups page revision text ></tt> _xmlfilename_
#
# You want to create the XML by hand, so we explain its structure.
# Within the +database+ element there exist the following two elements 
# describing each table:
# <tt>table_structure</tt>::
#   contains +field+ elements which give the data type of the corresponding 
#   table column. You only need to give them for numeric data 
#   (use +int+ and +float+ as types).
# <tt>table_data</tt>::
#   contains one +row+ element for each table row. Fields omitted are set to 
#   _nil_.
#
# XML structure:
#   <mysqldump>
#     <database>
#       <table_structure name="user">
#         <field Field="user_id" Type="int" />
#       </table_structure>
#       <table_data name="user">
#         <row>
#           <field name="user_id">1</field>
#           <field name="user_name">example_user</field>
#           <field name="user_real_name">Example User</field>
#         </row>
#         <row>
#           ...
#         </row>
#         ...
#       </table_data>
#       <table_structure name="page">
#         <field Field="page_id" Type="int" />
#         <field Field="page_namespace" Type="int" />
#         <field Field="page_counter" Type="int" />
#         <field Field="page_is_redirect" Type="int" />
#       </table_structure>
#       <table_data name="page">
#         <row>
#           <field name="page_id">1</field>
#           <field name="page_namespace">0</field>
#           <field name="page_title">Hauptseite</field>
#           <field name="page_counter">1014</field>
#           <field name="page_is_redirect">0</field>
#         </row>
#         ...
#       </table_data>
#       ...
#     </database>
#   </mysqldump>
#
# The tables <tt>user user_groups page revision text</tt> and optionally
# <tt>wio_genres</tt> and <tt>wio_roles</tt> may be present, <tt>user</tt>
# may be replaced by <tt>wio_user</tt> (do _not_ include <tt>wio_user</tt>
# _and_ <tt>user</tt> as this will go wrong).
#
# For a description of these tables see 
# http://www.mediawiki.org/wiki/Manual:Database_layout
#
# For which fields are read see the constants FIELDS_USER,
# FIELDS_USER_GROUPS, FIELDS_TEXT, 
# FIELDS_PAGE, FIELDS_REVISION, 
# FIELDS_GENRES, FIELDS_ROLES
# in Mediawiki::DB

begin
  require 'xml/libxml'
rescue LoadError => e
  require 'libxml'
end
require 'mediawiki/dbfields'

module Mediawiki

  class DB_libXML

    class << self
      def open(filename)
        new(XML::Document.file(filename), File.basename(filename))
      end
      def parse(string)
        new(XML::Document.string(string), '-')
      end
    end

    def initialize(xml, name)
      @xml = xml
      @name = name
    end

    def connect
      yield(self)
    end

    def to_s
      "xml:#{@name}"
    end

    def users(&block)
      table('wio_user').each_row(DB::FIELDS_USER, &block)
      table('user').each_row(DB::FIELDS_USER, &block)
    end

    def usergroups(&block)
      table('user_groups').each_row(DB::FIELDS_USER_GROUPS, &block)
    end
    def texts(&block)
      table('text').each_row(DB::FIELDS_TEXT, &block)
    end
    def pages(&block)
      table('page').each_row(DB::FIELDS_PAGE, &block)
    end
    def revisions(&block)
      table('revision').each_row(DB::FIELDS_REVISION, &block)
    end
    def genres(&block)
      table('wio_genres').each_row(DB::FIELDS_GENRES, &block)
    end
    def roles(&block)
      table('wio_roles').each_row(DB::FIELDS_ROLES, &block)
    end



    def table(table)
      Table.new(@xml, table)
    end

    class Table
      attr_reader :types
      Ints = ['bool', 'boolean', 'tinyint', 'smallint', 'mediumint',
              'int', 'integer', 'bigint']
      Floats = ['float', 'double', 'real', 'decimal', 'dec', 'fixed']

      def initialize(xml, table)
        @table = table
        @xml = xml
        @types = Hash.new(:to_s)
        tn = @xml.find("/mysqldump/database/table_structure[@name=\"#{@table}\"]/field")
        tn.each do |node|
          node['Type'].downcase =~ /^(\w*)/
          sql_t = $1
          @types[node['Field']] = if Ints.member?(sql_t)
                                    :to_i
                                  elsif Floats.member?(sql_t)
                                    :to_f
                                  else
                                    :to_s
                                  end
        end
      end
      
      def each_row(fields)
        rs = @xml.find("/mysqldump/database/table_data[@name=\"#{@table}\"]/row")
        rs.each do |row|
          yield(fields.collect { |f| 
            nodes = row.find("./field[@name=\"#{f}\"]") 
            if nodes.empty?
              nil
            else
              nodes.first.content.send(@types[f])
            end
          })
        end
      end
    end
  end
end

#def test
#  Mediawiki::DB_libXML.open('/tmp/wikidb.xml').connect do |db|
#    t = db.table('user')
#    puts t.types.inspect
#    db.users { |a,b,c| puts a,b }
#  end
#end
