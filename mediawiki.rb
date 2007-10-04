#!/usr/bin/ruby -w
# :title: Mediawiki - Ruby Lib
# = Mediawiki Library

#--
# A kind of history object along timestamps should be built
#++

require 'mysql-typed' # my own hack
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

# = The Mediawiki Namespace
module Mediawiki
  DEBUG = true

  # Creates a new Wiki object from the wio database.
  #
  # _pw_ is the database password.
  def Mediawiki.wio(pw)
    Wiki.wio(pw)
  end

  # Creates a new Wiki object from the mfg database.
  #
  # _pw_ is the database password.
  def Mediawiki.mfg(pw)
    Wiki.mfg(pw)
  end

  
  # = Main class representing the whole wiki
  #
  # You can choose a view on the Wiki which only includes certain namespaces
  # and a certain point in time.
  class Wiki
    
    # list of usergroups
    attr_reader :usergroups
    # a timeline of the revision history
    attr_reader :timeline
    # the filter used for the views
    attr_accessor :filter
    
    # Creates a new Wiki object from the wio database.
    #
    # _pw_ is the database password.
    def Wiki.wio(pw)
      puts "Creating wio wiki." if DEBUG
      Wiki.new("wikidb", "www.kinf.wiai.uni-bamberg.de", "wikiuser", pw)
    end
    
    # Creates a new Wiki object from the mfg database.
    #
    # _pw_ is the database password.
    def Wiki.mfg(pw)
      Wiki.new("mfg", "kinf01.kinf.wiai.uni-bamberg.de", "mfg-reader", pw)
    end
    
    # Creates a new Wiki object from a database
    # _db_:: database name
    # _host_:: database host
    # _user_:: database user
    # _pw_:: database password
    # _version_:: used, if reading the database is different in different
    #             Mediawiki versions
    def initialize(db, host, user, pw, version=1.8)
      @db = db
      @host = host
      @dbuser = user
      @dbpassword = pw
      @version=version
      
      @filter = Filter.new(self)
      
      read_db
      puts "Done." if DEBUG
    end
    
    def namespaces
      @filter.namespaces
    end

    alias ns namespaces

    def namespace=(n)
      @filter.namespace = n
    end
    
    def deny_user(u)
      @filter.deny_user(u)
    end

    def to_s
      @host+'/'+@db
    end

    def inspect
      "#<Mediawiki::Wiki #{@dbuser}@#{@host}/#{@db} #{@pages_id.length} pages, #{@revisions_id.length} revisions, #{@users_id.length} users>"
    end

    def page_by_title(t)
      @pages_title[t]
    end

    def page_by_id(i)
      @pages_id[i]
    end
    def text_by_id(i)
      @texts_id[i]
    end
    def user_by_id(i)
      @users_id[i]
    end
    def revision_by_id(i)
      @revisions_id[i]
    end

    # view on users through _filter_
    def users(filter=@filter)
      UsersView.new(@users_id, filter) # is this to heavyweighted? Reuse views?
    end

    # view on pages through _filter_
    def pages(filter=@filter)
      PagesView.new(@pages_id, filter) # Reuse views?
    end

    # view on revisions through _filter_
    def revisions(filter=@filter)
      RevisionsView.new(@revisions_id, filter) # Reuse views?
    end

    def pagegraph(filter=@filter)
      ps = pages(filter)
      g = Graph.new(ps, :directed) { |n| n.title }
      ps.each do |p|
        p.links(filter).each do |q|
          g.link(p,q)
        end
      end
      g
    end

    def coauthorgraph(filter=@filter)
      us = users(filter)
      g = Graph.new(us, :undirected) { |n| n.name }
      pages(filter).each do |p| 
        nodes = p.users(filter)
        nodes.each do |n| 
          nodes.each do |m| 
            g.link(n,m) if n.uid < m.uid
          end
        end
      end
      g
    end

    def communicationgraph(filter=@filter)
      us = users(filter)
      g = Graph.new(us, :directed) { |n| n.name }
      pages(filter).each do |p| 
        p.revisions(filter).inject do |a,b|
          g.link(a.user,b.user)
          b
        end
      end
      g
    end
      

    #
    # internal stuff
    #
    private 
    def read_db
      puts "connecting to database #{@host}/#{@db}" if DEBUG
      mysql = Mysql::new(@host, @dbuser, @dbpassword, @db)
      puts "connected." if DEBUG
      
      # Collect the users
      # The uid=0 user:
      u0 = User.new(self, 0, 'system', 'System User', nil, nil, 
                   '', nil, nil, nil, nil, nil, nil, nil, nil, nil);
      @users_id = {0 => u0}      
      mysql.each("select * from user") do |row|
        user = User.new(self, *row)
        @users_id[user.uid] = user
      end
      
      # Assign groups to them
      @usergroups = Hash.new { |h,k| h[k]=[] }
      mysql.each("select * from user_groups") do |uid,g|
        user = @users_id[uid]
        user.groups << g
        @usergroups[g] << user
      end
      
      # Read all the raw text data
      @texts_id = {}
      mysql.each("select * from text") do |tid, t, flags|
        @texts_id[tid] = Text.new(self, tid, t, flags)
      end
      
      # and the pages
      @pages_id = {}
      @pages_title = {}
      mysql.each("select * from page") do |row|
        page = Page.new(self, *row)
        @pages_id[page.pid] = page
        @pages_title[page.title] = page
      end

      # And now the revisions
      @revisions_id = {}
      @timeline = []
      mysql.each("select * from revision") do |row|
        revision = Revision.new(self, *row)
        @revisions_id[revision.rid] = revision
        @timeline << revision.timestamp
      end
      @timeline.sort!
      @time = @timeline.last

      @pages_id.each_value do |page|
        page.update_current
      end
    end

  end
  
  # One user
  class User
    
    attr_reader :uid
    attr_reader :name
    attr_reader :real_name
    attr_reader :email
    attr_reader :options
    attr_reader :touched
    attr_reader :token
    attr_reader :email_authenticated
    attr_reader :email_token
    attr_reader :email_token_expires
    attr_reader :registration
    attr_reader :newpass_time
    attr_reader :editcount
    attr_reader :groups
    attr_reader :revisions
    
    def initialize(wiki,
                   user_id, name, real_name, password, newpassword, 
                   email, options, touched, token, email_authenticated, 
                   email_token, email_token_expires, registration, 
                   newpass_time=nil, editcount=nil)
      @uid = user_id
      @name = name
      @real_name = real_name
      # @password = password
      # @newpassword = newpassword
      @email = email
      @options = options
      @touched = touched
      @token = token
      @email_authenticated = email_authenticated
      @email_token = email_token
      @email_token_expires = email_token_expires
      @registration = registration
      @newpass_time = newpass_time
      @editcount = editcount
      
      @groups = []

      @revisions = []
    end

    def <<(r)
      @revisions << r
    end

    def inspect
      "#<Mediawiki::User id=#{@uid} name=\"#{@name}\">"
    end

  end
  
  # One page with all revisions
  class Page
    
    # the page id
    attr_reader :pid
    # the namespace the page is in
    attr_reader :namespace
    # title of the page (all '_' converted to ' ')
    attr_reader :title
    # list of permition keys (Take care: only for 1.9 or lower)
    attr_reader :restrictions
    # number of views
    attr_reader :counter
    # some value used for Special:Randompage
    attr_reader :random

    # creates a new Page. _wiki_ is the Wiki the page belongs to, all
    # other parameters correspond to the fields in the corresponding database
    # table. 
    def initialize(wiki,
                   pid, namespace, title, restrictions, counter, redirect, 
                   new, random, touched, latest, len)
      @wiki = wiki
      @pid = pid
      @namespace = namespace
      @title = title.tr('_',' ')
      @restrictions = restrictions # not used in 1.10
      @counter = counter
      @is_redirect = (redirect!=0)
      @is_new = (new!=0)
      @random = random
      @touched = Mediawiki.s2time(touched)
      @len = len

      @revisions = []  # will be filled soon
      @current_revision = latest # will get replaced by a link to a Revision
                                 # object in #update_current
    end
    
    # The plain text of the current revision of the page
    def content
      @current_revision.content
    end

    # page is redirected?
    #
    # ToDo: this should be determined from the current revision!
    def is_redirect?
      @is_redirect
    end

    alias is_redirected? is_redirect?

    # page is new? (one single revision)
    def is_new?
      @is_new
    end

    # adds a revision to the page
    def <<(revision)
      @revisions << revision
    end

    # view on revisions through _filter_
    def revisions(filter=@wiki.filter)
      RevisionsView.new(@revisions, filter) # Reuse views?
    end

    # current revision (may get changed while implementing history)
    def revision
      @current_revision
    end

    # links of current revision (may get changed while implementing history)
    def links(filter=@filter)
      revision.links(filter)
    end

    # view on users through _filter_
    def users(filter=@wiki.filter)
      a = Set.new
      revisions(filter).each { |r| a << r.user }
      UsersView.new(a, filter)
    end

    def inspect
      "#<Mediawiki::Page id=#{@pid} title=\"#{@title}\">"
    end

    def update_current     #:nodoc:
      @current_revision = @wiki.revision_by_id(@current_revision)
    end
  end
  
  
  # a single revision of a Page
  class Revision
    
    # the revision id
    attr_reader :rid
    # the corresponding Text (take care, this is *not* the plain text, 
    # see content())
    attr_reader :text
    # the User's edit summary
    attr_reader :comment
    # the Page this revision belongs to
    attr_reader :page
    # the User who made the edit
    attr_reader :user
    # the time of edit (I don't bother whether this is GMT or MET)
    attr_reader :timestamp
    # length of revision in bytes (since 1.10)
    attr_reader :len
    # list of links to non-existing pages (regardless of views)
    attr_reader :full_dangling
    
    # creates a new Revision. _wiki_ is the Wiki the revision belongs to, all
    # other parameters correspond to the fields in the corresponding database
    # table. 
    def initialize(wiki,
                   rev_id, page_id, text_id, comment, user_id, user_text, 
                   timestamp, minor_edit, deleted, len=nil, parent_id=nil)
      @wiki = wiki
      @rid = rev_id
      @comment = comment
      @timestamp = Mediawiki.s2time(timestamp)
      @minor_edit = (minor_edit == 1)
      # @deleted = deleted  # unused
      @len = len
      @parent_id = parent_id

      @user = @wiki.user_by_id(user_id)
      if @user
        @user << self
      else
        warn "Revision #{@rid}: User #{user_id} does not exist!"
      end
      @page = @wiki.page_by_id(page_id)
      if @page
        @page << self
      else
        warn "Revision #{@rid}: Page #{page_id} does not exist!"
      end

      @text = @wiki.text_by_id(text_id)

      set_links
    end
    
    # the plain text of this revision
    def content
      @text.text
    end
    
    # did the User mark this as minor edit?
    def minor_edit?
      @minor_edit
    end

    
    # list of pages linked from this revision. Returns a list of Pages
    def links(filter=@wiki.filter)
      PagesView.new(@links, filter)
    end

    def inspect
      "#<Mediawiki::Revision id=#{@rid} page=#{@page.inspect}>"
    end

    private
    def set_links
      @links = []
      @full_dangling = []           # TODO: old versions my be different!
      @text.each_link do |l|
        if (p = @wiki.page_by_title(l))
          @links << p
        else
          @full_dangling << l
        end
      end
    end
    
  end
  
  # the pure plain text
  class Text
    
    # the text id
    attr_reader :tid

    # text flags: this could be 
    # gzip:: the text is compressed
    # utf8:: the text is encoded in utf8 
    #        (but mayby the opposite depending on mediawiki software setup)
    # object:: we do not have text but a serialized PHP object 
    #          (I hope we will never ever see this)
    attr_reader :flags
    
    # creates a new Text. _wiki_ is the Wiki the revision belongs to, all
    # other parameters correspond to the fields in the corresponding database
    # table. 
    def initialize(wiki, tid, text, flags)
      @wiki = wiki
      @tid = tid
      @text = text
      @flags = flags

      @internal_links = []

      parse_text
    end
    
    # the raw text as found in the database
    def rawtext
      @text
    end
    
    # the text converted according to the flags given.
    #
    # currently this is the same as rawtext, we will change this if needed
    def text
      # here we should do some conversions according to @flags
      # but it seems this is not needed for our data
      @text
    end

    # iterator over all links found in the text given as strings.
    # For a link iterator giving Page objects please see the 
    # corresponding Revision object (as Text objects do not have timestamps
    # we need to decide whether a link is dangling)
    def each_link(&block)
      @internal_links.each(&block)
    end

    def inspect
      "#<Mediawiki::Text id=#{@tid}>"
    end

    private
    def parse_text
      @text.gsub(/<nowiki>.*?<\/nowiki>/,'').scan(/\[\[(.*?)\]\]/m).each do |l|
        l = l.first
        if l =~/(.*)\|(.*)/  # Named link
          l = $1
        end
        l.tr!('_',' ')  # may give problems with sublinks?
        @internal_links << l   unless l==''
      end
    end
  end

  # A filter used for the views
  class Filter

    # The Set of namespaces to be allowed. To add namespaces use e.g.:
    # <tt>filter.namespaces << 1</tt>.
    #
    # If the set includes :all all namespaces are included.
    attr_accessor :namespaces

    attr_accessor :denied_users

    attr_accessor :redirects

    # If revisions with minor edits are included
    attr_accessor :minor_edits

    # Creates a new filter for the _wiki_.
    #
    # By default the time is newest and the namespaces are #<Set: {0}>.
    def initialize(wiki)
      @wiki = wiki
      @namespaces = [0]  # default namespace
      @namespaces = Set.new
      @namespaces << 0
      @redirects = :keep

      @denied_users = Set.new
    end

    # add user to the Set of denied users
    #
    # u is the user to be filtered or its uid.
    def deny_user(u)
      u = @wiki.user_by_id if u.kind_of?(Integer)
      @denied_users << u
    end

    def namespace=(n)
      @namespaces.clear
      @namespaces << n
    end

    def include_namespace(n)
      @namespaces << n
    end

    def include_all_namespaces
      @namespaces << :all
    end

    alias ns namespaces
  end


  # Base class for views on the Wiki. Used for filtering
  class View

    include Enumerable

    # Creates a new View. 
    # _list_ is some kind of Enumerable, e.g. an Array or Set or Hash.
    # Hashes are dealed specific as here the hash-values are used!
    def initialize(list, filter)
      @list = list
      @filter = filter

      select_methods
    end

    # Returns the number of objects in the view.
    def size
      l=0
      each { l+=1 }
      l
    end
    
    alias length size

    def each_item &block     # :nodoc:
      @list.each { |i| block.call(i) if allowed?(i) }
    end

    def each_value &block    # :nodoc:
      @list.each_value { |i| block.call(i) if allowed?(i) }
    end

    private
    # care for the differences between Arrays and Hashes
    def select_methods
      if @list.respond_to?(:each_value)
        class << self
          alias each each_value
        end
      else
        class << self
          alias each each_item
        end        
      end
    end

    def allowed?(i)
      true
    end
    
    public
    alias each each_item    # mainly for rdoc

  end
  
  # ToDo handling of redirects: as filtering works, but aliazing not!
  class PagesView < View
    def allowed?(page)
      (@filter.namespaces.include?(:all) || 
       @filter.namespaces.include?(page.namespace)) &&
        (!(@filter.redirects==:filter) || !page.is_redirect?)
    end
  end

  class UsersView < View
    def allowed?(user)
      !@filter.denied_users.include?(user) 
    end
  end

  class RevisionsView < View
    def allowed?(revision)
      !@filter.denied_users.include?(revision.user) &&
        !(@filter.minor_edits && revision.minor_edit?)
    end
  end


  # Container for simple Graphs
  class Graph
    attr_reader :nodes, :links, :linkcount, :directed
    def initialize(nodes, *attrs, &lproc)
      @nodes = nodes.to_a
      @lproc = lproc || lambda { |n| n.label }
      @links = Hash.new(0)
      @linkcount = true
      attrs.each do |attr|
        case attr
        when :directed : @directed = true
        when :linkcount : @linkcount = true
        when :nolinkcount : @linkcount = false
        end
      end
    end
    def link(src, dest, *attrs)
      src, dest = dest, src  if !@directed && (src.object_id > dest.object_id)
      @links[Link.new(self, src, dest, *attrs)] += 1
    end

    def to_dot(*attrs)
      d = "#{'di' if @directed}graph G {\n"
      d << attrs.join(';')
      @nodes.each { |n| 
        d << "  \"#{nid(n)}\" [label=\"#{@lproc.call(n).tr('"',"'")}\"];\n" }
      @links.each { |l,count| d << l.to_dot(count) }
      d << "}\n"
    end

    def to_dotfile(filename, *attrs)
      File.open(filename,'w') { |file| file << to_dot(*attrs) }
    end

    def Graph::nid(o)
      "n%x" % o.object_id
    end

    class Link
      attr_reader :src, :dest, :attr
      def initialize(graph, src, dest, *attrs)
        @graph = graph
        @src = src
        @dest = dest
        @attrs = attrs
      end
      def to_dot(count)
        s = "  \"#{nid(@src)}\" #{edgesymbol} \"#{nid(@dest)}\" "
        s << "[#{@attrs.join(',')}]" unless @attrs.empty?
        s << weightlabel(count) if linkcount
        s << ";\n"
        s
      end

      def edgesymbol
        directed ? '->' : '--'
      end

      def nid(o)
        Graph::nid(o)
      end

      def linkcount
        @graph.linkcount
      end

      def directed
        @graph.directed
      end

      def weightlabel(count)
        "[weight=#{count},taillabel=\"#{count}\",fontcolor=\"grey\",fontsize=5,labelangle=0]"
      end

      def eql?(other)
        (@src==other.src) && (@dest==other.dest) && (@attr==other.attr)
      end
      def hash
        @src.hash ^ @dest.hash
      end
    end
  end


  # Utility funtions

  # If _s_ is a Time object it is left untouched.
  #
  # Otherwise it is assumed to be a string formed <tt>"yyyymmddhhmmss"</tt>
  # which is converted in the corresponding time object.
  def Mediawiki.s2time(s)
    return s if s.kind_of?(Time)
    return Time.gm(s[0..3], s[4..5], s[6..7], 
                   s[8..9], s[10..11], s[12..13])
  end
  
end

if __FILE__ == $0
  wio = Mediawiki.wio(IO.getpw)

  ## Beispiele:

  # Anzahl Nutzer pro Seite:
  nusers = wio.pages.collect { |p| p.users.length }
  puts "Avg. # of users: #{nusers.inject(0.0) { |s,i| s+i }/nusers.length}"
  l = (nusers.select { |i| i>1 }).length
  puts "# of pages with more than one user: %d/%d (%.2f%%)" % 
    [l, nusers.length, l.to_f/nusers.length*100]
end
