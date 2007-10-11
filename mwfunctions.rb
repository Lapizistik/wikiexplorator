#!/usr/bin/ruby -w
# :title: Mediawiki-Extensions - Ruby Lib
# = Mediawiki Extensions
# This file extends the Mediawiki::Wiki class with a set of very specialized
# functions for making things easier to us, but which are not part of core
# functionality. You can directly require this file instead of 
# <tt>mediawiki.rb</tt>.

require 'mediawiki'

module Mediawiki
  class Wiki
    # Find all users adding internal links matching Regexp _type_ 
    # (<tt>/^Kategorie:/</tt> by default).
    # 
    # A Regexp and a Filter may be given as parameters in any order.
    def typelinkusers(*params)
      type=/^Kategorie:/
      f = @filter
      params.each { |par|
        case par
        when Filter : f = par
        when Regexp : type = par
        else warn "Ignoring parameter #{par.inspect}"
        end }
      kusers = Hash.new(0)
      systemuser = user_by_id(0)
      pages(f).each { |p|
        p.revisions(f).collect { |r|
          [r.text.internal_links.select { |s| s =~ type }.sort, r.user] 
        }.inject([[],systemuser]) { |v,n|
          kusers[n.last] += 1  if (v.first!=n.first)
          n
        }
      }
      kusers
    end
    # Find and pretty print all users adding internal links 
    # matching Regexp type (see #typelinkusers).
    def pp_typelinkusers(*params)
      puts typelinkusers(*params).collect { |u,c| 
        "%-20s: %4i" % [u.name,c] }.join("\n")
    end

    # Pretty print user statistics
    def pp_userstats(filter=@filter)
      puts users(filter).collect { |u| 
        [u.name, u.real_name, u.uid, u.revisions.length, u.pages.length] 
      }.sort.collect { |a| "%-20s %-30s %4i %4i %4i" % a }.join("\n")
    end

    # Pretty print the revision to page edit ratio of each user.
    # 
    # By default the _name_ of the user is printed, this may be changed e.g.
    # to the uid by giving the Symbol :uid as parameter.
    def pp_user_revisiondensity(*params)
      userkey=:name
      f = @filter
      params.each { |par|
        case par
        when Filter : f = par
        when Symbol : userkey = par
        else warn "Ignoring parameter #{par.inspect}"
        end }
      puts users(f).select { |u| u.pages(f).length>0 }.collect { |u| 
        [u.send(userkey), u.revisions(f).length.to_f/u.pages(f).length] 
      }.sort.collect {|a| "%-20s: %f" % a}.join("\n")
    end

    # Pretty print users editing foreign user pages:
    def pp_user_foreignedits(filter=@filter)
      puts pages(f).collect { |p| 
        pt = p.title.split("/").first
        [p.title, p.users(f).collect { |u| u.name }.select { |un| pt != un}] 
      }.select { |t,ua| !ua.empty? }.collect { |t,ua| 
        "%-45s: %s" % [t, ua.join(', ')] }.join("\n") 
    end
  end
end
