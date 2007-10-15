#!/usr/bin/ruby -w
# :title: Mediawiki Graph - Ruby Lib
# = Mediawiki graph functions

require 'dotgraph'    # generic dotfile graph class

# = The Mediawiki Namespace
module Mediawiki
  class Wiki
    def pagegraph(filter=@filter, &block)
      ps = pages(filter)
      if block
        g = DotGraph.new(ps, :directed, block)
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
        g = DotGraph.new(us, :undirected, block)
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

    # Luhmann communication graph. Any revision is considered as an answer
    # to the last revision before (direct linking)
    def communicationgraph(filter=@filter, &block)
      us = users(filter)
      if block
        g = DotGraph.new(us, :directed, block)
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
    # to all revisions before (group linking)
    def groupcommunicationgraph(filter=@filter, &block)
      us = users(filter)
      if block
        g = DotGraph.new(us, :directed, block)
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
