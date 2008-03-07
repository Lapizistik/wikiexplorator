#!/usr/bin/ruby -w
# :title: Mediawiki - Ruby Lib
# = Mediawiki Core

#--
# A kind of history object along timestamps should be built
#++

require 'set'         # the Set class
require 'mediawiki/db'
require 'parsedate'

# = The Mediawiki Namespace
module Mediawiki
  DEBUG = true

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
    # a list of namespaces found in this wiki
    attr_reader :namespaces
    
    # Creates a new Wiki object from database connection _wikidb_.
    #
    # For description of _options_ see Wiki.open
    def initialize(wikidb, options)
      @version = options[:version] || 1.8
      @uid_aliases = options[:uid_aliases] || {}
      @name = wikidb.to_s
      
      if (err=read_db(wikidb))
        warn err # !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! Error handling!
      end

      @filter = Filter.new(self) # must be last

      puts "Done." if DEBUG
    end
    
    # Creates a new Wiki object from a database
    # _db_:: database name
    # _host_:: database host
    # _user_:: database user
    # _pw_:: database password
    # _options_:: 
    #   a hash with further options:
    #   <i>:engine=>"Mysql"</i>:: the database engine to be used.
    #   <i>:version=>1.8</i>:: 
    #     used, if reading the database is different in different Mediawiki 
    #     versions. Not really implemented until now!
    #   <i>:uid_aliases=>{}</i>:: 
    #     hash of uids to be aliased. E.g. if the user with uid=15 was 
    #     registered by mistake (e.g. with the wrong user name) and the user 
    #     reregistered with uid=22, use:
    #       :uid_aliases => {17 => 22}
    #     and all appearances of uid=17 will be mapped to 22. No user
    #     with uid 17 will show up in the Wiki object.
    def Wiki.open(db, host, user, pw, *options)
      options = options.first || {}
      Wiki.new(DB.new(db, host, user, pw, options[:engine] || "Mysql", 
                      options[:version] || 1.8), options)
    end

    def to_s
      @name
    end

    def inspect
      "#<Mediawiki::Wiki #{@name} #{@pages_id.length} pages, #{@revisions_id.length} revisions, #{@users_id.length} users>"
    end

    # gives the Page object with title _t_ in namespace _ns_
    def page_by_title(t, ns=0)
      @pages_title[page_tns(t,ns)]
    end

    # gives the Page object with pid _i_
    def page_by_id(i)
      @pages_id[i]
    end

    # gives the Text object with tid _i_
    def text_by_id(i)
      @texts_id[i]
    end

    # gives the User object with uid _i_
    def user_by_id(i)
      @users_id[i]
    end

    # gives the User object with name _n_
    def user_by_name(n)
      @users_name[n]
    end

    # gives the Revision object with rid _i_
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

    #
    # internal stuff
    #
    private 
    def read_db(wikidb)
      puts "connecting to database #{wikidb}" if DEBUG
      begin # TODO: rescues etc.
        wikidb.connect do |dbh|
          puts "connected." if DEBUG
          
          # Collect the users
          # The uid=0 user:
          u0 = User.new(self, 0, 'system', 'System User', nil, nil, 
                        '19700101000000', nil, nil, nil, nil, nil);
          @users_name = {}
          @users_id = {0 => u0}
          users_aliased = {}
          dbh.users do |uid, *row|
            user = User.new(self, uid, *row)
            if alias_uid = @uid_aliases[uid]
              users_aliased[alias_uid] = user
            else
              @users_id[user.uid] = user
              @users_name[user.name] = user
            end
          end

          # Merging aliased Users:
          users_aliased.each do |uid, auser|
            @users_id[uid].merge_user(auser)
          end
          
          # Assign groups to them
          # TODO: how to deal with aliasing here?
          @usergroups = Hash.new { |h,k| h[k]=[] }
          dbh.usergroups do |uid,g|
            user = @users_id[uid]
            user.groups << g
            @usergroups[g] << user unless @uid_aliases[uid]
          end
          
          # Read all the raw text data
          @texts_id = {}
          dbh.texts do |tid, t, flags|
#            puts("»%s« (%s); »%s« (%s); »%s« (%s)" % 
#                 [tid, tid.class, t, t.class, flags, flags.class])
            @texts_id[tid] = Text.new(self, tid, t, flags)
          end
          
          # and the pages
          @pages_id = {}
          @pages_title = {}
          @namespaces = Set.new
          dbh.pages do |row|
            page = Page.new(self, *row)
            @pages_id[page.pid] = page
            ns = page.namespace
            @namespaces << ns
            @pages_title[page_tns(page.title, ns)] = page
          end
          @namespaces = @namespaces.sort
          
          # And now the revisions
          @revisions_id = {}
          @timeline = []
          dbh.revisions do |rid, pid, tid, comment, uid, user, *row|
            if alias_uid = @uid_aliases[uid] # user aliasing
              uid = alias_uid
              user = @users_id[uid]
            end
            revision = Revision.new(self, 
                                    rid, pid, tid, comment, uid, user, *row)
            @revisions_id[revision.rid] = revision
            @timeline << revision.timestamp
          end
          @timeline.sort!
          @timeline.uniq!
          @time = @timeline.last
          
          @pages_id.each_value do |page|
            page.sort_revisions
          end
          @users_id.each_value do |user|
            user.sort_revisions
          end

          # And additional information by ourselves
          # page genres ...
          dbh.genres do |pid, genres|
            if p = @pages_id[pid]
              p.set_genres_from_string(genres)
            else
              warn "page_id #{pid} from wio_genres not found in pages!"
            end
          end
          # ... and user roles
          dbh.roles do |uid, roles|
            # puts "Adding roles #{roles} to user with uid #{uid}"
            if u = (@users_id[uid] || @users_id[@uid_aliases[uid]])
              u.set_roles_from_string(roles)
            else
              warn "user_id #{uid} from wio_roles not found in users!"
            end
          end
        end
      end
      return nil
    end
    def page_tns(t, ns)
      [t, ns]
    end
  end
  
  # One user
  class User
    
    # the user id
    attr_reader :uid
    # the user login name
    attr_reader :name
    # the user real name
    attr_reader :real_name
    # the user email address
    attr_reader :email
    # personalization information 
    attr_reader :options
    # time of last User interaction with the wiki causing _any_ change
    attr_reader :touched
    attr_reader :email_authenticated # :nodoc:
    attr_reader :email_token_expires # :nodoc:
    # time of User registration
    attr_reader :registration
    attr_reader :newpass_time # :nodoc:
    attr_reader :editcount # :nodoc:
    # Set of groups the User belongs to
    attr_reader :groups
    # the roles identified for this User
    #
    # For a standard Mediawiki database this is a Set containing the
    # String "DEFAULT" (the default role) for all users.
    #
    # See <tt>mediawiki/db.rb</tt> for how to use this.
    attr_reader :roles


    # The parameters do not correspond exactly to the user table fields as we
    # do not want to read the user_password, user_newpassword, user_token and
    # user_email_token fields (as these may contain sensible data).
    def initialize(wiki,
                   user_id, name, real_name,
                   email, options, touched, email_authenticated, 
                   email_token_expires, registration, 
                   newpass_time=nil, editcount=nil)
      @wiki = wiki
      @uid = user_id
      @name = name
      @real_name = real_name
      # @password = password
      # @newpassword = newpassword
      @email = email
      @options = options
      @touched = Mediawiki.s2time(touched)
      # @token = token
      @email_authenticated = email_authenticated
      # @email_token = email_token
      @email_token_expires = email_token_expires
      @registration = Mediawiki.s2time(registration)
      @newpass_time = newpass_time
      @editcount = editcount
      
      @groups = Set.new

      @roles = ['DEFAULT'].to_set  # each user has at least the DEFAULT role.
                                   # may be updated in #set_roles_from_string
      @revisions = []
    end

    # view on revisions through _filter_
    def revisions(filter=@wiki.filter)
      RevisionsView.new(@revisions, filter) # Reuse views?
    end

    # unfiltered revisions
    def all_revisions
      @revisions
    end

    # view on pages edited by this user through _filter_
    def pages(filter=@wiki.filter)
      PagesView.new(revisions(filter).collect { |r| r.page }.compact.to_set, 
                    filter)
    end
    
    # add a revision
    def <<(r)
      @revisions << r
    end

    def inspect
      "#<Mediawiki::User id=#{@uid} name=\"#{@name}\">"
    end

    # whether this User has the given role _r_. _r_ may be given as String 
    # (exact matching) or Regexp.
    def has_role?(r)
      case r
      when String : @roles.include?(r)
      when Regexp : @roles.find { |s| s =~ r }
      end
    end

    # Returns the first role of an user which is not the +DEFAULT+ role.
    # For an user with more than one role a random role is returned
    def role
      @roles.find { |r| r!='DEFAULT' }
    end

    # id string for dotfile creation
    def node_id
      "u#{uid}"
    end

    def set_roles_from_string(roles) # :nodoc:
      @roles.merge((roles || '').split(/,\s*/))
    end

    def merge_user(other) # :nodoc:
      @touched = [@touched, other.touched].max
      # we have to check if there were registration timestamps in the database
      # as this is only for version>=1.8
      if @registration 
        @registration = [@registration, other.registration].min
      end
      # editcount was introduced in 1.9
      @editcount += (other.editcount || 0) if @editcount
      # we do not merge @groups. Think about!
      @revisions += other.all_revisions
    end
    
    def sort_revisions # :nodoc:
      @revisions = @revisions.sort_by { |r| r.timestamp }
    end

  end
  
  # One page with all revisions
  class Page
    
    # the page id
    attr_reader :pid
    # the namespace the page is in
    attr_reader :namespace
    # title of the page (all "_" converted to " ")
    attr_reader :title
    # list of permission keys (Take care: only for 1.9 or lower)
    attr_reader :restrictions
    # number of views
    attr_reader :counter
    # some value used for Special:Randompage
    attr_reader :random
    # the genres identified in this Page.
    #
    # For a standard Mediawiki database this is a Set containing the
    # String "DEFAULT" (the default genre) for all pages.
    #
    # See <tt>mediawiki/db.rb</tt> for how to use this.
    attr_reader :genres

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

      @genres = ['DEFAULT'].to_set  # each page is at least in the DEFAULT 
                                    # genre. May be updated in 
                                    # #set_genres_from_string
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

    # whether this Page has the given genre _g_. _g_ may be given as String 
    # (exact matching) or Regexp.
    #
    # TODO: better in Revision?
    def has_genre?(g)
      case g
      when String : @genres.include?(g)
      when Regexp : @genres.find { |s| s =~ g }
      end
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

    # The timestamp of the oldest revision of this page.
    def creationtime
      @revisions.first.timestamp # this works past calling #update_current
    end

    # returns a hash where the key is a user and the value is the
    # number of revisions answering a revision of the _same_ user 
    # (number of self-edits)
    def self_edits(filter=@wiki.filter)
      h = Hash.new(0)
      revisions(filter).inject { |a,b| 
        ua = a.user; ub = b.user
        h[ub] += 1 if ua==ub
        b
      }
      h
    end

    # returns a hash where the key is a user and the value is the
    # number of revisions answering a revision of _another_ user 
    # (number of self-edits)
    def foreign_edits(filter=@wiki.filter)
      h = Hash.new(0)
      revisions(filter).inject { |a,b| 
        ua = a.user; ub = b.user
        h[ub] += 1 if ua!=ub
        b
      }
      h
    end

    def inspect
      "#<Mediawiki::Page id=#{@pid} title=\"#{@title}\">"
    end

    # id string for dotfile creation
    def node_id
      "p#{pid}"
    end


    def sort_revisions     #:nodoc:
      @current_revision = @wiki.revision_by_id(@current_revision)
      @revisions = @revisions.sort_by { |r| r.timestamp }
    end

    def set_genres_from_string(genres) # :nodoc:
      @genres.merge((genres || '').split(/,\s*/))
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
    # length of revision in bytes (since 1.10). This is the length 
    # as given in the revision table. For the length of the corresponding
    # Text object use the methods #length or #size.
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

    # Size of the Text object associated with this Revision object. 
    # Don't confuse this with #len.
    def size
      @text.size
    end

    alias length size

    def inspect
      "#<Mediawiki::Revision id=#{@rid} page=#{@page.inspect}>"
    end

    # whether the Page this revision belongs to has the given genre _g_. 
    # _g_ may be given as String (exact matching) or Regexp.
    #
    # TODO: it would be better to associate the revisions with genres,
    # but very costly. We will see if we can improve this automatically
    # in future.
    def has_genre?(g)
      @page && @page.has_genre?(g)
    end

    # the namespace of the Page this Revision belongs to (-255 if no page)
    def namespace
      @page ? @page.namespace : -255
    end

    # id string for dotfile creation
    def node_id
      "r#{rid}"
    end

    private
    def set_links
      @links = []
      @full_dangling = []           # TODO: old versions my be different!
      @text.each_link do |l|
        if (p = @wiki.page_by_title(l)) # TODO: namespaces other than 0.
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

    # Array of all internal links found in the Text as strings
    attr_reader :internal_links

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

    # id string for dotfile creation
    def node_id
      "t#{tid}"
    end

    # the size of the text in bytes (this is similar but not equal to 
    # the size of the text in chars due to UTF-8 encoding).
    def size
      @text.size
    end

    alias length size

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

  # A Filter used for the views
  #
  # A Filter is applied any time a list of Pages, Users, Revisions, ...
  # is accessed through a View. See the documentation for the corresponding
  # Views.
  class Filter

    # The Set of namespaces to be allowed. To add namespaces use e.g.:
    # <tt>filter.namespaces << 1</tt>.
    #
    # This is {0} by default.
    attr_accessor :namespaces

    # Set of users to be skipped (empty by default).
    attr_accessor :denied_users

    # how to deal with redirected pages:
    # [:keep] like normal pages (default)
    # [:filter] skip them.
    attr_accessor :redirects

    # If revisions with minor edits are included (default: true).
    attr_accessor :minor_edits

    # Timespan of revisions included (please note that this filter is
    # _not_ applied for PagesView).
    attr_accessor :revision_timespan

    # All Pages/Revisions with one or more genres matching this Regex are
    # included/excluded (dependent on _genreinclude_).
    #
    # Default is <tt>//</tt> (matches always)
    attr_accessor :genregexp

    # Boolean deciding whether only Pages/Revisions with genres
    # matching the Regexp _genregexp_ are included or all
    # Pages/Revisions with matching genres are excluded by this Filter.
    # 
    # +true+ by default.
    attr_accessor :genreinclude

    # All Users with one or more roles matching this Regex are
    # included/excluded (dependent on _rolesinclude_).
    #
    # Default is <tt>//</tt> (matches always)
    attr_accessor :roleregexp

    # Boolean deciding whether only Users with roles
    # matching the Regexp _roleregexp_ are included or all
    # Users with matching rules are excluded by this Filter.
    # 
    # +true+ by default.
    attr_accessor :roleinclude


    # Creates a new filter for the _wiki_.
    #
    # By default the time is newest and the namespaces are #<Set: {0}>.
    def initialize(wiki)
      @wiki = wiki
      @namespaces = Set.new # default namespace
      @namespaces << 0
      @redirects = :keep
      @minor_edits = true
      full_timespan
      @genregexp = // # matches on everything
      @genreinclude = true
      @roleregexp = // # matches on everything
      @roleinclude = true
      @denied_users = Set.new
      # !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
      # When adding new attributes do _not_ forget to adjust #clone_attrs
      # !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    end

    # add user to the Set of denied users
    #
    # ua is one or more users to be filtered or its uid or name.
    def deny_user(*ua)
      ua.each do |u|
        case u
        when Integer    : u = @wiki.user_by_id(u)
        when String     : u = @wiki.user_by_name(u)
        end
        @denied_users << u
      end
    end

    # remove user from the Set of denied users
    #
    # ua is one or more users not longer to be filtered or its uid or name.
     def undeny_user(*ua)
      ua.each do |u|
        case u
        when Integer    : u = @wiki.user_by_id(u)
        when String     : u = @wiki.user_by_name(u)
        end
        @denied_users.delete(u)
      end
    end


    alias ns namespaces

    # restrict the namespaces to the single namespace _n_
    def namespace=(n)
      @namespaces.clear
      @namespaces << n
    end

    # :call-seq:
    # include_namespace(n, ...)
    #
    # adds one or more given namespaces
    def include_namespace(*ns)
      @namespaces.merge(ns)
    end

    # sets the filter to include all namespaces
    def include_all_namespaces
      include_namespace(*@wiki.namespaces)
    end

    # sets the revision_timespan to include the whole wiki timeline
    def full_timespan
      @revision_timespan = (@wiki.timeline.first..@wiki.timeline.last)      
    end

    # Gives the begin of the revision timespan
    def starttime
      @revision_timespan.begin
    end

    # Gives the end of the revision timespan
    def endtime
      @revision_timespan.end
    end

    # Sets the begin of the revision timespan
    def starttime=(time)
      time = convert_to_time(time)
      @revision_timespan = (time..@revision_timespan.end)
    end

    # Sets the end of the revision timespan
    def endtime=(time)
      time = convert_to_time(time)
      @revision_timespan = (@revision_timespan.begin..time)
    end

    # gives a deep copy of this filter.
    def clone
      cl = super
      cl.clone_attrs
    end

    protected
    def clone_attrs
      @namespaces = @namespaces.clone
      @denied_users = @denied_users.clone
      # @revision_timespan = @revision_timespan.clone
      self
    end

    private
    def convert_to_time(time)
      unless time.kind_of?(Time)
        ta = ParseDate.parsedate(time)
        time = Time.local(*ta)
      end
      time
    end

  end


  # Base class for views on the Wiki. Used for subclassing.
  #
  # By using a View a list of Users (UsersView), Revisions (RevisionsView), 
  # Pages (PagesView), ... can be accessed with some Filter applied, so
  # only the elements matching the Filter rules are shown. See the 
  # documentation for Filter and the derived Views for details.
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

    # whether a given object is seen through this filter. To be
    # overwritten in subclasses.
    def allowed?(i)
      true
    end
    
    private
    # care for the differences between Arrays and Hashes
    def select_methods
      if @list.respond_to?(:each_value)
        class << self
          alias each each_value # :nodoc:
        end
      else
        class << self
          alias each each_item # :nodoc:
        end        
      end
    end

  end
  
  # A view on an Enumerable of users.
  #
  # Users are filtered by
  # user itself :: Filter#denied_users
  # role :: Filter#roleregexp and Filter#roleinclude
  class UsersView < View
    # whether a given User is seen through this filter.
    def allowed?(user)
      !@filter.denied_users.include?(user) &&
        !(@filter.roleinclude ^ user.has_role?(@filter.roleregexp))
    end
  end

  # A view on an Enumerable of pages.
  #
  # TODO handling of redirects: as filtering works, but aliazing not!
  class PagesView < View
    # whether a given Page is seen through this filter.
    #
    # Pages are filtered by 
    # namespace :: Filter#namespaces,
    # redirection :: Filter#redirects,
    # genre :: Filter#genregexp and Filter#genreinclude
    def allowed?(page)
      @filter.namespaces.include?(page.namespace) &&
        (!(@filter.redirects==:filter) || !page.is_redirect?) &&
        !(@filter.genreinclude ^ page.has_genre?(@filter.genregexp))
    end
  end

  # A view on an Enumerable of revisions.
  class RevisionsView < View
    # whether a given Revision is seen through this filter.
    #
    # Revisions are filtered by
    # users :: Filter#denied_users
    # namespace of the corresponding page :: Filter#namespaces,
    # minor_edits :: Filter#minor_edits,
    # genre of the corresponding page :: 
    #   Filter#genregexp and Filter#genreinclude
    def allowed?(revision)
      !@filter.denied_users.include?(revision.user) &&
        @filter.namespaces.include?(revision.namespace) &&
        @filter.revision_timespan.include?(revision.timestamp) &&
        (@filter.minor_edits || !revision.minor_edit?) &&
        !(@filter.genreinclude ^ revision.has_genre?(@filter.genregexp))
    end
  end

  private
  # Utility funtions

  # If _s_ is a Time object it is left untouched.
  #
  # Otherwise it is assumed to be a string formed <tt>"yyyymmddhhmmss"</tt>
  # which is converted in the corresponding time object.
  def Mediawiki.s2time(s)
    #    puts "s2time: converting time #{s.inspect}" if DEBUG
    return Time.at(0) if !s
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
