#!/usr/bin/ruby -w

require 'test/unit'
require 'set'
require 'pp'


$:.unshift File.join(File.dirname(__FILE__), '..') # here is the code

require 'util/dotgraph'


class DotGraph::Link
  def pretty_print(pp)
    pp.text "#<[#{@src}->#{@dest}], weight=#{@weight}>"
  end
end

class TestDotGraph < Test::Unit::TestCase
  
  def setup
  end

  def graph_degree_asserts(g, nodes, hlinks)

    indegrees = Hash.new(0)
    outdegrees = Hash.new(0)
    windegrees = Hash.new(0)
    woutdegrees = Hash.new(0)
    selfdegrees = Hash.new(0) # 0 or 1
    wselfdegrees = Hash.new(0)
    hlinks.each do |k,w| 
      s,d = *k
      indegrees[d] += 1
      windegrees[d] += w
      outdegrees[s] += 1
      woutdegrees[s] += w
      if s==d
        selfdegrees[s] += 1
        wselfdegrees[s] += w
      end
    end
    assert(selfdegrees.values.to_set.subset?([1,2].to_set)) # test the test

    if g.directed
      g.nodes.each do |n|
        assert_equal(indegrees[n], g.n_indegree(n),
                     "node #{n} indegree")
        assert_equal(outdegrees[n], g.n_outdegree(n),
                     "node #{n} outdegree")
        assert_equal(windegrees[n], g.n_indegree(n, true),
                     "node #{n} weighted indegree")
        assert_equal(woutdegrees[n], g.n_outdegree(n, true),
                     "node #{n} weighted outdegree")
      end
    else
      g.nodes.each do |n|
        assert_equal(indegrees[n]+outdegrees[n]-selfdegrees[n], 
                     g.n_degree(n),
                     "node #{n} degree (should be #{indegrees[n]}+#{outdegrees[n]}-#{selfdegrees[n]})")
        assert_equal(windegrees[n]+woutdegrees[n]-wselfdegrees[n], 
                     g.n_degree(n, true),
                     "node #{n} weighted degree (should be #{windegrees[n]}+#{woutdegrees[n]}-#{wselfdegrees[n]})")
      end
    end
  end

  def graph_asserts(nodes, links, directed=false, addmode=true)
    g = DotGraph.new(nodes)                             # create graph
    links.each { |s,d,w| g.link(s,d,w, addmode) }       # add links

    # ok, multilinks:
    hlinks = Hash.new(0)
    directed = g.directed
    links.each do |s,d,w| 
      s,d = d,s if !directed && (s.object_id>d.object_id)
      k = [s,d]
      if addmode
        hlinks[k] +=w
      else
        hlinks[k] = [w,hlinks[k]].max
      end
    end

    assert_equal(g.nodes.length, nodes.length) # thats obvious
    assert_equal(g.links.length, hlinks.length) # not _so_ clear

    graph_degree_asserts(g, nodes, hlinks)

    # test link removal
    ln = g.links.length                        # current nr of links
    sln = links.select { |s,d,w| s==d }.collect { |s,d,w| s }.uniq.length
    g.remove_self_links
    hlinks = hlinks.delete_if { |k,w| k[0]==k[1] }
    assert_equal(ln - sln, g.links.length)

    lnodes = links.collect { |s,d,w| [s,d] }.flatten.uniq

    g.remove_lonely_nodes

    assert_equal(lnodes.sort, g.nodes.sort)
    
    nodes = g.nodes.dup

    graph_degree_asserts(g, nodes, hlinks)

    g.remove_nodes

    

  end
  
  def graph_1
    nodes = (1..11).to_a
    links = [[1,2,4],
             [1,4,3],
             [1,5,2],
             [3,2,1]]
    (4..10).each { |s| (6..8).each { |d| links << [s,d,2] }}
    [nodes, links]
  end
  
  def graph_inc
    nodes = (1..20).to_a
    links = []
    (1..20).each { |s| ((s+1)..20).each { |d| links << [s,d,21-s] }}
    [nodes, links]
  end
  
  def graphs
    g1 = graph_1
    graph_asserts(*g1)
    graph_asserts(g1[0], g1[1], true)
  end

  def test_core
    graphs
  end

end
