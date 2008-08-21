#!/usr/bin/ruby -w
# :title: Mediawiki Graph - Ruby Lib
# = Mediawiki graph functions

require 'util/dotgraph'    # generic dotfile graph class

# = The Mediawiki Namespace
module Mediawiki
  class Wiki
    # Pages link graph. Returns a DotGraph with pages as nodes and references
    # from one page to another as links.
    #
    # If a block is given it is passed to DotGraph::new (see there)
    def pagegraph(filter=@filter, &block)
      ps = pages(filter)
      if block
        g = DotGraph.new(ps, :directed => true, &block)
      else
        g = DotGraph.new(ps, :directed => true) { |n| n.title }
      end
      ps.each do |p|
        p.links(filter).each do |q|
          g.link(p,q)
        end
      end
      g
    end

    # :call-seq:
    # coauthorgraph(filter=@wiki.filter, params={}) { |n| ... }
    # coauthorgraph(params={}) { |n| ... }
    #
    # Coauthor graph. Returns a DotGraph with users as nodes. For each page
    # edited by user _a_ and _b_ a link from _a_ to _b_ is added (i.e. the
    # link weight corresponds to the number of pages where _a_ and _b_ are 
    # coauthors).
    #
    # _filter_:: the Filter to use.
    # _params_:: 
    #   a Hash of named parameters:
    #   <i>:type</i> => :plain ::
    #     type of graph:
    #     <i>:plain</i> :: ordinary coauthorgraph
    #     <i>:newman</i> :: 
    #       newman coauthorgraph, i.e. each page counts with 
    #       1/(nr of coautors).
    #
    # If a block is given it is passed to DotGraph::new (see there)
    def coauthorgraph(*params, &block)
      filter=@filter
      type = :plain
      params.each do |par|
        case par
        when Filter : filter = par
        when Symbol : type = par
        when Hash
          filter = par[:filter] || filter
          type = par[:type] || type
        else
          raise ArgumentError.new("Wrong argument: #{par.inspect}")
        end
      end

      newman = (type==:newman)

      us = users(filter)
      if block
        g = DotGraph.new(us, :directed => false, &block)
      else
        g = DotGraph.new(us, :directed => false) { |n| n.name }
      end
      nodes = l = n = i = j = nil
      pages(filter).each do |p| 
        nodes = p.users(filter).to_a
        l = nodes.length-1 # nr of coauthors on this page...
        nodes.each_with_index do |n,i|
          (i+1).upto(l) do |j|
            if newman
              g.link(n,nodes[j], 1.0/l)
            else
              g.link(n,nodes[j])
            end
          end
        end
      end
      g
    end
    # # Plain implementation  of inner loop of coauthorgraph (simple but slow):
    #pages(filter).each do |p| 
    #  nodes = p.users(filter)
    #  nodes.each do |n| 
    #    nodes.each do |m| 
    #      g.link(n,m) if n.uid < m.uid
    #    end
    #  end
    #end


    # Copages graph. Returns a DotGraph with pages as nodes. For each user
    # editing pages _p_ and _q_ a link from _p_ to _q_ is added (i.e. the
    # link weight corresponds to the number of users editing pages 
    # _p_ and _q_).
    #
    # If a block is given it is passed to DotGraph::new (see there)
    def copagesgraph(filter=@filter, &block)
      ps = pages(filter)
      if block
        g = DotGraph.new(ps, :directed => false, &block)
      else
        g = DotGraph.new(ps, :directed => false) { |n| n.pid }
      end
      nodes = l = n = i = j = nil
      users(filter).each do |u| 
        nodes = u.pages(filter).to_a
        l = nodes.length-1
        nodes.each_with_index do |n,i|
          (i+1).upto(l) do |j|
            g.link(n,nodes[j])
          end
        end
      end
      g
    end
    # # Plain implementation of inner loop of copagesgraph (simple but slow):
    #users(filter).each do |u| 
    #  nodes = u.pages(filter)
    #  nodes.each do |n| 
    #    nodes.each do |m| 
    #      g.link(n,m) if n.pid < m.pid
    #    end
    #  end
    #end



    # :call-seq:
    # interlockingresponsegraph(filter=@wiki.filter, params={}) { |n| ... }
    # interlockingresponsegraph(params={}) { |n| ... }
    #
    # Luhmann communication graph. Any revision is considered as an answer
    # to the last revisions of other users before.
    #
    # _filter_:: the Filter to use.
    # _params_:: 
    #   a Hash of named parameters:
    #   <i>:counts</i>:: 
    #     a Symbol indicating how links are counted:
    #     <i>:add</i>::
    #       for any pair of users _a_, _b_ the link weight of the link from
    #       _a_ to _b_ is the sum of all Page#interlockingresponses between
    #       _a_ and _b_ for each page.
    #     <i>:log</i>::
    #       for any pair of users _a_, _b_ the link weight of the link from
    #       _a_ to _b_ is 
    #       <tt>sum_{<i>p</i>\in P}\log(il_<i>p</i>(_a_->_b_)+1)</tt> with
    #       <tt>il_<i>p</i>(_a_->_b_)</tt> the interlockingresponse from
    #       _a_ to _b_ on page _p_ (see Page#interlockingresponses).
    #     <i>:squares</i>::
    #       for any pair of users _a_, _b_ the link weight of the link from
    #       _a_ to _b_ is 
    #       <tt>(sum_{<i>p</i>\in P}(il_<i>p</i>(_a_->_b_)^k))^(1/k)</tt> with
    #       <tt>il_<i>p</i>(_a_->_b_)</tt> the interlockingresponse from
    #       _a_ to _b_ on page _p_ (see Page#interlockingresponses) and
    #       _k_=2 (default), can be changed with <i>:k</i>. The function
    #       kind of reverts for _k_<1.
    #     <i>:max</i>::
    #       for any pair of users _a_, _b_ the link weight of the link from
    #       _a_ to _b_ is the maximum of all Page#interlockingresponses between
    #       _a_ and _b_ over all pages.
    #     <i>:page</i>::
    #       for any pair of users _a_, _b_ the link weight of the link from
    #       _a_ to _b_ is the number of pages having a interlockingresponse 
    #       _a_ to _b_.
    #   <i>:k</i>::
    #     exponent used for <i>:count</i> => <i>:squares</i>
    # See Page#interlockingresponses for discussion.
    #
    # If a block is given it is passed to DotGraph::new (see there)
    def interlockingresponsegraph(*params, &block)
      filter=@filter
      counts=:add
      k = 2.0
      params.each do |par|
        case par
        when Filter : filter = par
        when Symbol : counts = par
        when Numeric : k = par
        when Hash
          filter = par[:filter] || filter
          counts = par[:counts] || counts
          k = par[:k] || k
        else
          raise ArgumentError.new("Wrong argument: #{par.inspect}")
        end
      end
      us = users(filter)
      if block
        g = DotGraph.new(us, :directed => true, &block)
      else
        g = DotGraph.new(us, :directed => true) { |n| n.name }
      end
      case counts
      when :add
        pages(filter).each do |p| 
          p.interlockingresponses(filter).each_pair { |u,to|
            to.each_pair { |v,n| g.link(u,v,n) }
          }
        end
      when :log
        pages(filter).each do |p| 
          p.interlockingresponses(filter).each_pair { |u,to|
            to.each_pair { |v,n| g.link(u,v,Math.log(n+1)) }
          }
        end
      when :squares
        pages(filter).each do |p| 
          p.interlockingresponses(filter).each_pair { |u,to|
            to.each_pair { |v,n| g.link(u,v,n**k) }
          }
        end
        kk = 1.0/k
        g.links.each_value { |l| l.weight = l.weight**kk}
      when :max
        pages(filter).each do |p|
          p.interlockingresponses(filter).each_pair { |u,to|
            to.each_pair { |v,n| g.link(u,v,n,false) }
          }
        end  
      when :page
        pages(filter).each do |p|
          p.interlockingresponses(filter).each_pair { |u,to|
            to.each_pair { |v,n| g.link(u,v,1) }
          }
        end
      else
        warn "Unknown counts type '#{counts}'. No links set!"
      end
      g
    end

    # Luhmann communication graph. Any revision is considered as an answer
    # to the last revisions of other users before.
    #
    # Similar to interlockingresponses, but instead of cumulating the
    # interlocking from some Users _a_ to _b_ in one link, each "answer"
    # is represented by a new link with a timestamp. 
    #
    # Use this method for creating a SONIA file from the resulting DotGraph:
    #   wiki.timedinterlockingresponsegraph.to_sonfile('test.son')
    #
    # _filter_:: the Filter to use.
    #
    # See also Page#timedinterlockingresponses.
    #
    # If a block is given it is passed to DotGraph::new (see there)
    def timedinterlockingresponsegraph(filter=@filter, &block)
      us = users(filter)
      if block
        g = DotGraph.new(us, :directed => true, &block)
      else
        g = DotGraph.new(us, :directed => true) { |n| n.name }
      end
      pages(filter).each do |p| 
        p.timedinterlockingresponses(filter).each_pair do |s,dt|
          dt.each_pair do |r,t|
            g.timelink(s, r.user, t)
          end
        end
      end
      g
    end

    # :call-seq:
    # directresponsegraph(filter=@filter, counts=:add) { |n| ... }
    # directresponsegraph(filter=@filter) { |n| ... }
    # directresponsegraph(counts=:add) { |n| ... }
    # directresponsegraph() { |n| ... }
    #
    # Luhmann communication graph. Any revision is considered as an answer
    # to the last revisions of other users before.
    #
    # _filter_:: the Filter to use.
    # _counts_:: 
    #   a Symbol indicating how links are counted:
    #   <i>:add</i>::
    #     for any pair of users _a_, _b_ the link weight of the link from
    #     _a_ to _b_ is the sum of all Page#directresponses between
    #     _a_ and _b_ for each page.
    #   <i>:max</i>::
    #     for any pair of users _a_, _b_ the link weight of the link from
    #     _a_ to _b_ is the maximum of all Page#directresponses between
    #     _a_ and _b_ over all pages.
    #   <i>:page</i>::
    #     for any pair of users _a_, _b_ the link weight of the link from
    #     _a_ to _b_ is the number of pages having a directresponse 
    #     _a_ to _b_.
    # See Page#directresponses for discussion.
    #
    # If a block is given it is passed to DotGraph::new (see there)
    def directresponsegraph(*params, &block)
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
        g = DotGraph.new(us, :directed => true, &block)
      else
        g = DotGraph.new(us, :directed => true) { |n| n.name }
      end
      case counts
      when :add
        pages(filter).each do |p| 
          p.directresponses(filter).each_pair { |u,to|
            to.each_pair { |v,n| g.link(u,v,n) }
          }
        end
      when :max
        pages(filter).each do |p|
          p.directresponses(filter).each_pair { |u,to|
            to.each_pair { |v,n| g.link(u,v,n,false) }
          }
        end  
      when :page
        pages(filter).each do |p|
          p.directresponses(filter).each_pair { |u,to|
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
        g = DotGraph.new(us, :directed => true, &block)
      else
        g = DotGraph.new(us, :directed => true) { |n| n.name }
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
    # See Page#groupresponses for discussion.
    def groupresponsegraph(filter=@filter, &block)
      us = users(filter)
      if block
        g = DotGraph.new(us, :directed => true, &block)
      else
        g = DotGraph.new(us, :directed => true) { |n| n.name }
      end
      pages(filter).each do |p| 
        p.groupresponses(false).each { |a,b|
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
        g = DotGraph.new(us, :directed => true, &block)
      else
        g = DotGraph.new(us, :directed => true) { |n| n.name }
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
