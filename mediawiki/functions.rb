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
      sum = a.inject(0) { |s,x| s+x }
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
    #
    def pageeditdist(filter=@filter)
      edits = Hash.new(0)
      pages(filter).each { |p| edits[p.revisions(filter).length] += 1 }
      edits
    end

    # pretty print distribution of number of page edits
    #
    # see #pp_dist for discussion of <i>&sortby</i>
    def pp_pageeditdist(filter=@filter, &sortby)
      pp_dist(pageeditdist(filter), &sortby)
    end

    # distribution of number of page self edits (see Page#foreign_edits).
    #
    def pageselfdist(filter=@filter)
      edits = Hash.new(0)
      pages(filter).each { |p| 
        edits[p.self_edits(filter).values.inject(0) {|s,i| s+i}] += 1 }
      edits
    end

    # pretty print distribution of number of page self edits 
    # (see Page#self_edits).
    #
    # see #pp_dist for discussion of <i>&sortby</i>
    def pp_pageselfdist(filter=@filter, &sortby)
      pp_dist(pageselfdist(filter), &sortby)
    end

    # distribution of number of page foreign edits (see Page#foreign_edits).
    #
    def pageforeigndist(filter=@filter)
      edits = Hash.new(0)
      pages(filter).each { |p| 
        edits[p.foreign_edits(filter).values.inject(0) {|s,i| s+i}] += 1 }
      edits
    end

    # pretty print distribution of number of page foreign edits 
    # (see Page#foreign_edits).
    #
    # see #pp_dist for discussion of <i>&sortby</i>
    def pp_pageforeigndist(filter=@filter, &sortby)
      pp_dist(pageforeigndist(filter), &sortby)
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

    # Returns an array of Time objects in the range
    # <i>starttime=timeline.first</i> to <i>endtime=timeline.last</i>
    # of a certain step width. The first time may be greater than
    # _starttime_ (if demanded), last time greater than _endtime_.
    #
    # _attr_:: 
    #   named attributes.
    #   <i>:step</i>:: 
    #     step width. Either a number of seconds or a symbol
    #     (<i>:hour</i>=3600, <i>:day</i>=24*3600, <i>:week</i>=7*24*3600).
    #     <i>:month</i> corresponds to a real month with varying length,
    #     so the steps will be e.g. 2000-1-27 to  2000-2-27 to  2000-2-27.
    #   <i>:zero</i>:: 
    #     start time setting. _starttime_ may have some 
    #     odd value, e.g. 2003-8-20 14:33, but when stepping daily we would 
    #     prefer timespans from midnight to midnight, i.e. start with
    #     2003-8- 20 0:00. <i>:zero</i> may have the following values:
    #     <i>:hour</i>:: set minutes and seconds of _starttime_ to 0.
    #     <i>:day</i>:: set hours, minutes and seconds of _starttime_ to 0.
    #     <i>:week</i>:: 
    #       sets hours, minutes and seconds of _starttime_ to 0
    #       and the date to the closest day before or equal to _starttime_
    #       with day of the week as given in <i>:wday</i>. 
    #     <i>:month</i>:: 
    #       set day to 1 and 
    #       hours, minutes and seconds of _starttime_ to 0.
    #     <i>:year</i>:: 
    #       set month and day to 1 and 
    #       hours, minutes and seconds of _starttime_ to 0.
    #     <i>[y,m,d,h,min,s]</i>:: 
    #       each entry may either be _nil_ or an Integer. 
    #       If _nil_ the corresponding Time component of _starttime_
    #       (from year to second) is left untouched, otherwise it is set
    #       to the value given in the array. So to start the day at 5:00
    #       use <i>[nil,nil,nil,5,0,0]</i>.
    #   <i>:wday</i>:: 
    #     The day of the week the timeraster will start, if
    #     <i>:zero => :week</i>. Allowed values are 0 (sunday) 
    #     to 6 (saturday). Defaults to 0 (sunday).
    #
    # Use this function e.g. for investigating wiki dynamics:
    #  filter = wiki.filter
    #  tr = wiki.timeraster(:step => :week, :zero => :week)
    #  r1 = tr.collect { |t|
    #    filter.endtime = t
    #    [t1, wiki.revisions.length]
    #  }
    #  require 'enumerator'
    #  r2 = tr.enum_cons(3).collect { |ta| 
    #    filter.starttime = ta.first
    #    filter.endtime = ta.last
    #    [ta.first, ta_last, wiki.revisions.length]
    #  }
    # Now <i>r1</i> holds a table (array of arrays) with the time in the first
    # column and the number of revisions in the wiki up to this time in the 
    # second column.
    #
    # In <i>r2</i> the first and second column give start and end time of a two
    # week time slice and the third column gives the number of revisions
    # created within this timespan. The trick is here to use the enumerator
    # package to get the right slices from timeraster.
    def timeraster(attr={})
      attr = { 
        :step => :day,
        :zero => :day
      }.merge(attr)
      
      step = case attr[:step]
             when :hour : 3600
             when :day  : 24*3600
             when :week : 7*24*3600
             else attr[:step]
             end
      
      ct = @timeline.first
      et = @timeline.last

      case z = attr[:zero]
      when :hour
        ct = Time.local(ct.year, ct.month, ct.day, ct.hour)
      when :day
        ct = Time.local(ct.year, ct.month, ct.day)
      when :week
        wday = (attr[:wday] || 0)
        d = ct.wday-wday
        d = d + 7 if d < 0
        ct = ct - (d*3600*24)
      when :month
        ct = Time.local(ct.year, ct.month, 1)
      when :year
        ct = Time.local(ct.year, 1, 1)
      else
        if z.kind_of?(Array)
          ct = Time.local(z[0] || ct.year, 
                          z[1] || ct.month, 
                          z[2] || ct.day,
                          z[3] || ct.hour,
                          z[4] || ct.min,
                          z[5] || ct.sec)
        end
      end

      a = [ct]
      if step == :month
        while ct < et
          if (m = ct.month) < 12
            ct = Time.local(ct.year, m+1, ct.day, ct.hour, ct.min, ct.sec)
          else
            ct = Time.local(ct.year+1, 1, ct.day, ct.hour, ct.min, ct.sec)
          end
          a << ct
        end
      else
        while ct < et
          a << (ct += step)
        end
      end
      a
    end

  end

  class Page
    # We say an User _v_ (interlocking-)responds to another User _u_ if one 
    # or more revisions of _v_ follow a revision of _u_ 
    # (similar but not equal to to Wiki#groupresponsegraph).
    #
    # If two or more revisions of _v_ follow a revision of _u_ this counts
    # as one response. But if a revision of _v_ follows a _new_ revision 
    # of _u_ this counts as _new_ response.
    #
    # For a revision history of
    #  0 1 2 3 4 5 6 7 8 9 
    #  u x u v u u x u v x
    # we have (this excludes self-responsess for simplification)
    #  x1 -> u0,   u2 -> x1,   v3 -> x1,   v3 -> u2,   u4 -> v3,
    #  x6 -> v3,   x6 -> u5,   u7 -> x6,   v8 -> x6,   v8 -> u7, 
    #  x9 -> u7,   x9 -> v8.
    #
    # So we get (including self-responses): 
    #  u -> u  =  4    u -> v  =  1    u -> x  =  2
    #  v -> u  =  2    v -> v  =  1    v -> x  =  2
    #  x -> u  =  3    x -> v  =  2    x -> x  =  2
    #
    def interlockingresponses(filter=@wiki.filter)
      uhs = Hash.new { |h,k| h[k]=Hash.new(0) }
      timedinterlockingresponses(filter).each_pair { |u,h|
        uh = uhs[u]
        h.each_key { |r| uh[r.user] += 1 }
      }
      uhs
    end

    # Computes the timed interlocking response graph for the users
    # on this page. See #interlockingresponses for a description.
    # (for use in DotGraph.to_son).
    #
    # Returns a nested Hash structure of user to revision links with 
    # timestamps.
    def timedinterlockingresponses(filter=@wiki.filter)
      latest_users = Hash.new
      usersh = Hash.new { |h,k| h[k]=Hash.new }

      revisions(filter).each do |r|
        u = r.user
        latest_users.each_pair { |lu,lr| usersh[u][lr] = r.timestamp }
        latest_users[u] = r
      end

      usersh
    end

    # :call-seq:
    # groupresponses(filter=@filter, compatible=true)
    # groupresponses(filter=@filter)
    # groupresponses(compatible=true)
    # groupresponses()
    #
    # We say an User _v_ (group-)responses another User _u_ if any
    # revision of _v_ follows any revision of _u_ 
    # (similar to Wiki#groupresponsegraph).
    #
    # By definition each user-user-combination may occour at most once.
    # To be compatible to #directcommunications and #interlockingresponses 
    # this method by default nevertheless returns a Hash of Hashes. 
    # This can be changes by setting the parameter _compatible_ to _false_.
    #
    # If a block is given it is called with each user and the result
    # used as key. E.g.:
    #  page.groupresponses { |u| u.name }
    def groupresponses(*params) # :yields: user
      filter = @wiki.filter
      compatible=true
      params.each { |par|
        if Filter===par
          filter = par
        else
          compatible = par
        end
      }
      if block_given?
        us = revisions(filter).collect { |r| yield(r.user) }
      else
        us = revisions(filter).collect { |r| r.user }
      end
      s = Set.new
      while b = us.pop
        us.each { |a| s << [b,a] }
      end
      if compatible
        usersh = Hash.new { |h,k| h[k]=Hash.new(0) }
        s.each { |a,b| usersh[a][b]=1 }
        return usersh
      else
        return s
      end
    end

    # We say an User _v_ (direct-)responses to another User _u_ if a 
    # revision of _v_ directly follows a revision of _u_ 
    # (similar to Wiki#communicationgraph).
    #
    # If a block is given it is called with each user and the result
    # used as key. E.g.:
    #  page.directresponses { |u| u.name }
    def directresponses(filter=@wiki.filter) # :yields: user
      if block_given?
        us = revisions(filter).collect { |r| yield(r.user) }
      else
        us = revisions(filter).collect { |r| r.user }
      end
      usersh = Hash.new { |h,k| h[k]=Hash.new(0) }
      us.inject do |a,b|
        usersh[b][a] += 1
        b
      end
      usersh
    end
  end
end
