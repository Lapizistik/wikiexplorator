#!/usr/bin/ruby -w
# :title: Mediawiki-Extensions - Ruby Lib
# = Mediawiki Extensions
# This file extends the Mediawiki::Wiki class with a set of very specialized
# functions for making things easier to us, but which are not part of core
# functionality.

require 'mediawiki/core'

module Mediawiki
  class Wiki

    # user statistics
    def userstats(filter=@filter)
      se = Hash.new(0)
      fe = Hash.new(0)
      ke = typelinkusers(/^Kategorie:/, filter) 
      ie = typelinkusers(/^Bild:/, filter) 
      pages(filter).each { |p| 
        p.self_edits(filter).each { |u,n| se[u] += n }
        p.foreign_edits(filter).each { |u,n| fe[u] += n }
      }
      uh = Hash.new
      users(filter).each { |u| 
        # user => [edits, pages, edits/pages
        #          self edits, foreign edits, category edits, image edits]
        uh[u] = [el=u.revisions(filter).length, 
                 pl=u.pages(filter).length, el.to_f/pl,
                 se[u], fe[u], ke[u], ie[u]]
      }
      uh
    end

    # Pretty print user statistics
    def pp_userstats(filter=@filter, &sortby)
      ul = userstats(filter)
      if sortby
        ul = ul.sort_by(&sortby)
      else
        ul = ul.sort_by { |u,| u.name }
      end
      puts ul.collect { |u,values|
        ('%-12s %-23s %4s %4i %4i %6.2f %4i %4i %4i %4i' %
         ([u.name, u.real_name, u.uid] + values))
      }.join("\n")
    end    

    # global user statistics.
    #
    # A cumulated view on #userstats.
    def global_userstats(filter=@filter)
      ['Edits', 'Edited Pages', 'Edits/Page', 
       'Self-Edits', 'Foreign-Edits', 
       'Category-Edits', 'Image-Edits'
      ].zip(userstats(filter).values.transpose.collect { |a| 
              descstats(a) }).collect { |a| a.flatten }
    end

    # Pretty print global user statistics. See #global_userstats.
    def pp_global_userstats(filter=@filter)
      puts '%-20s  %7s %7s %5s %5s %5s' % ["global user stats",
                                           "avg","std","med","min","max"]
      puts global_userstats(filter).collect { |a|
        '%-20s: %7.2f %7.2f %5i %5i %5i' % a
      }.join("\n")
    end

    # Descriptive statistics on the values of Enumerable _a_.
    def descstats(a)
      ll = a.length
      a = a.reject { |i| i.respond_to?(:nan?) && i.nan? }
      l = a.length
      warn "Found some NaN's! Removed!" if l < ll
      a.sort!
      sum = a.inject { |s,x| s+x }
      qsum = a.inject(0) { |s,x| s+x*x }
      avg = sum.to_f/l
      std = qsum.to_f/l - avg**2
      std = (std.nan? ? std : Math.sqrt(std))
      [avg, std, a[(l-1)/2], a.first, a.last]
    end


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
    def pp_user_foreign_up_edits(filter=@filter)
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
    #
    # see #pp_dist for discussion of <i>&sortby</i>
    def pp_userroledist(filter=@filter, &sortby)
      pp_dist(userroledist(filter), &sortby)
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
    #
    # see #pp_dist for discussion of <i>&sortby</i>
    def pp_genredist(detailed=false, filter=@filter, &sortby)
      pp_dist(genredist(detailed, filter), &sortby)
    end

    # distribution of number of page edits
    def pageeditdist(filter=@filter)
      edits = Hash.new(0)
      pages(filter).each { |p| edits[p.revisions.length] += 1 }
      edits
    end

    # pretty print distribution of number of page edits
    #
    # see #pp_dist for discussion of <i>&sortby</i>
    def pp_pageeditdist(filter=@filter, &sortby)
      pp_dist(pageeditdist(filter), &sortby)
    end

    # pretty print a Hash (or assoc array) as distribution.
    # 
    # If a block <i>&sort_by</i> is given, it is used for sorting:
    # <tt>pp_dist(h) { |k,v| k }</tt>:: sorts by the key
    # <tt>pp_dist(h) { |k,v| v }</tt>:: sorts by the key
    def pp_dist(hash, &sortby)
      if sortby
        hs = hash.sort_by(&sortby)
      else
        hs = hash.sort
      end
      puts hs.collect { |kv|
        "%-40s: %3i" % kv
      }.join("\n")
    end
  end
end
