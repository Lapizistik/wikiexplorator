#!/usr/bin/ruby -w
# :title: Mediawiki Graph - Ruby Lib
# = Mediawiki graph functions

require 'util/dotgraph'    # generic dotfile graph class

# = The Mediawiki Namespace
module Mediawiki
  class Wiki
    def pagegraph(filter=@filter, &block)
      ps = pages(filter)
      if block
        g = DotGraph.new(ps, :directed, &block)
      else
        g = DotGraph.new(ps, :directed) { |n| n.title }
      end
      ps.each do |p|
        p.links(filter).each do |q|
          g.link(p,q)
        end
      end
      g
    end

    def coauthorgraph(filter=@filter, &block)
      us = users(filter)
      if block
        g = DotGraph.new(us, :undirected, &block)
      else
        g = DotGraph.new(us, :undirected) { |n| n.name }
      end
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

    # :call-seq:
    # respondcommunicationgraph(filter=@filter, counts=:add) { |n| ... }
    # respondcommunicationgraph(filter=@filter) { |n| ... }
    # respondcommunicationgraph(counts=:add) { |n| ... }
    # respondcommunicationgraph() { |n| ... }
    #
    # Luhmann communication graph. Any revision is considered as an answer
    # to the last revisions of other users before.
    #
    # _filter_:: the Filter to use.
    # _counts_:: 
    #   a Symbol indicating how links are counted:
    #   <i>:add</i>::
    #     for any pair of users _a_, _b_ the link weight of the link from
    #     _a_ to _b_ is the sum of all Page#respondcommunications between
    #     _a_ and _b_ for each page.
    #   <i>:max</i>::
    #     for any pair of users _a_, _b_ the link weight of the link from
    #     _a_ to _b_ is the maximum of all Page#respondcommunications between
    #     _a_ and _b_ over all pages.
    #   <i>:page</i>::
    #     for any pair of users _a_, _b_ the link weight of the link from
    #     _a_ to _b_ is the number of pages having a respondcommunication 
    #     _a_ to _b_.
    # See Page#respondcommunications for discussion.
    #
    # If a block is given it is passed to DotGraph::new (see there)
    def respondcommunicationgraph(*params, &block)
      filter=@filter
      counts=:add
      params.each { |par|
        case par
        when Filter : filter = par
        when Symbol : counts = par
        else warn "Ignoring parameter #{par.inspect}"
        end }
      us = users(filter)
      if block
        g = DotGraph.new(us, :directed, &block)
      else
        g = DotGraph.new(us, :directed) { |n| n.name }
      end
      case counts
      when :add
        pages(filter).each do |p| 
          p.respondcommunications(filter).each_pair { |u,to|
            to.each_pair { |v,n| g.link(u,v,n) }
          }
        end
      when :max
        pages(filter).each do |p|
          p.respondcommunications(filter).each_pair { |u,to|
            to.each_pair { |v,n| g.link(u,v,n,false) }
          }
        end  
      when :page
        pages(filter).each do |p|
          p.respondcommunications(filter).each_pair { |u,to|
            to.each_pair { |v,n| g.link(u,v,1) }
          }
        end
      else
        warn "Unknown counts type '#{counts}'. No links set!"
      end
      g
    end

    # :call-seq:
    # directcommunicationgraph(filter=@filter, counts=:add) { |n| ... }
    # directcommunicationgraph(filter=@filter) { |n| ... }
    # directcommunicationgraph(counts=:add) { |n| ... }
    # directcommunicationgraph() { |n| ... }
    #
    # Luhmann communication graph. Any revision is considered as an answer
    # to the last revisions of other users before.
    #
    # _filter_:: the Filter to use.
    # _counts_:: 
    #   a Symbol indicating how links are counted:
    #   <i>:add</i>::
    #     for any pair of users _a_, _b_ the link weight of the link from
    #     _a_ to _b_ is the sum of all Page#directcommunications between
    #     _a_ and _b_ for each page.
    #   <i>:max</i>::
    #     for any pair of users _a_, _b_ the link weight of the link from
    #     _a_ to _b_ is the maximum of all Page#directcommunications between
    #     _a_ and _b_ over all pages.
    #   <i>:page</i>::
    #     for any pair of users _a_, _b_ the link weight of the link from
    #     _a_ to _b_ is the number of pages having a directcommunication 
    #     _a_ to _b_.
    # See Page#directcommunications for discussion.
    #
    # If a block is given it is passed to DotGraph::new (see there)
    def directcommunicationgraph(*params, &block)
      filter=@filter
      counts=:add
      params.each { |par|
        case par
        when Filter : filter = par
        when Symbol : counts = par
        else warn "Ignoring parameter #{par.inspect}"
        end }
      us = users(filter)
      if block
        g = DotGraph.new(us, :directed, &block)
      else
        g = DotGraph.new(us, :directed) { |n| n.name }
      end
      case counts
      when :add
        pages(filter).each do |p| 
          p.directcommunications(filter).each_pair { |u,to|
            to.each_pair { |v,n| g.link(u,v,n) }
          }
        end
      when :max
        pages(filter).each do |p|
          p.directcommunications(filter).each_pair { |u,to|
            to.each_pair { |v,n| g.link(u,v,n,false) }
          }
        end  
      when :page
        pages(filter).each do |p|
          p.directcommunications(filter).each_pair { |u,to|
            to.each_pair { |v,n| g.link(u,v,1) }
          }
        end
      else
        warn "Unknown counts type '#{counts}'. No links set!"
      end
      g
    end

    # Luhmann communication graph. Any revision is considered as an answer
    # to the last revision before (direct linking)
    #
    # This is the old implementation only kept for testing purposes
    def communicationgraph_old(filter=@filter, &block) # :nodoc:
      us = users(filter)
      if block
        g = DotGraph.new(us, :directed, &block)
      else
        g = DotGraph.new(us, :directed) { |n| n.name }
      end
      pages(filter).each do |p| 
        p.revisions(filter).inject do |a,b|
          g.link(b.user,a.user)
          b
        end
      end
      g
    end

    # Luhmann communication graph. Any revision is considered as an answer
    # to all revisions before (group linking). For each pair of users _a_,_b_
    # there is at maximum one communication _a_->_b_ and one _b_->_a_ counted
    # per page!
    #
    # See Page#groupcommunications for discussion.
    def groupcommunicationgraph(filter=@filter, &block)
      us = users(filter)
      if block
        g = DotGraph.new(us, :directed, &block)
      else
        g = DotGraph.new(us, :directed) { |n| n.name }
      end
      pages(filter).each do |p| 
        p.groupcommunications(false).each { |a,b|
          g.link(a,b)
        }
      end
      g
    end

    # Luhmann communication graph. Any revision is considered as an answer
    # to all revisions before (group linking). For each pair of users _a_,_b_
    # there is at maximum one communication _a_->_b_ and one _b_->_a_ counted
    # per page!
    #
    # This is the old implementation only kept for testing purposes
    def groupcommunicationgraph_old(filter=@filter, &block) # :nodoc:
      us = users(filter)
      if block
        g = DotGraph.new(us, :directed, &block)
      else
        g = DotGraph.new(us, :directed) { |n| n.name }
      end
      s = Set.new
      pages(filter).each do |p| 
        s.clear
        rusers = p.revisions(filter).collect { |r| r.user } 
        while b = rusers.pop
          rusers.each { |a| s << [a,b] }
        end
        s.each { |a,b| g.link(b, a) }
      end
      g
    end
  end
end
