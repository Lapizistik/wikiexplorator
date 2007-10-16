#!/usr/bin/ruby -w
# :title: Mediawiki-Extensions - Ruby Lib
# = Mediawiki Extensions
# This file extends the Mediawiki::Wiki class with a set of very specialized
# functions for making things easier to us, but which are not part of core
# functionality.

require 'mediawiki/core'

module Mediawiki
  class Wiki

    # :call-seq: 
    #  typelinkusers(regexp=/^Kategorie:/, filter=@wiki.filter)
    #  typelinkusers(regexp=/^Kategorie:/)
    #  typelinkusers(filter=@wiki.filter)
    #  typelinkusers()
    #
    # Find all users adding internal links matching Regexp _type_ 
    # (<tt>/^Kategorie:/</tt> by default).
    # 
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
    # :call-seq: 
    #  pp_typelinkusers(regexp=/^Kategorie:/, filter=@wiki.filter)
    #  pp_typelinkusers(regexp=/^Kategorie:/)
    #  pp_typelinkusers(filter=@wiki.filter)
    #  pp_typelinkusers()
    #
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

    # :call-seq: 
    #  pp_user_revisiondensity(userkey=:name, filter=@wiki.filter)
    #  pp_user_revisiondensity(userkey=:name)
    #  pp_user_revisiondensity(filter=@wiki.filter)
    #  pp_user_revisiondensity()
    #
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

    # role distribution
    def userroledist(filter=@filter)
      roles = Hash.new(0)
      users(filter).each { |u| u.roles.each { |r| roles[r]+=1 }}
      roles
    end
    # pretty print role distribution
    def pp_userroledist(filter=@filter)
      pp_dist(userroledist(filter))
    end

    # genre distribution
    #
    # if <i>detailed</i> is +false+ the genre strings are cut at <tt>">"</tt>.
    # This is because TAWS (which we used for genre annotation) uses this 
    # to indicate sub-genres. So the genre <tt>"definition>computing"</tt> 
    # is cut to <tt>"definition"</tt>.
    def genredist(detailed=false, filter=@filter)
      genres = Hash.new(0)
      pages(filter).each { |p| 
        p.genres.each { |g| 
          g = g.split('>').first unless detailed
          genres[g]+=1 
        }
      }
      genres
    end
    # pretty print genre distribution
    #
    # see #genredist for discussion of parameter _detailed_.
    def pp_genredist(detailed=false, filter=@filter)
      pp_dist(genredist(detailed, filter))
    end

    # distribution of number of page edits
    def pageeditdist(filter=@filter)
      edits = Hash.new(0)
      pages(filter).each { |p| edits[p.revisions.length] += 1 }
      edits
    end

    # pretty print distribution of number of page edits
    def pp_pageeditdist(filter=@filter)
      pp_dist(pageeditdist(filter))
    end

    private
    def pp_dist(hash)
      puts hash.sort.collect { |kv|
        "%-40s: %3i" % kv
      }.join("\n")
    end
  end
end
