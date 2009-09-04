#!/usr/bin/ruby -w
# :title: Dot Graph gv Extension - Ruby Lib
# =  Dot Graph Library - gv Extension
#
# if this library is required it silently replaces Dotgraph#to_graphviz and
# Dotgraph#render_graphviz with gv library calls. If there is any need to
# explicitly use the external program or the library use:
# external :: Dotgraph#to_graphviz_cmd, Dotgraph#render_graphviz_cmd
# internal :: Dotgraph#to_gv, Dotgraph#render_gv

require 'util/dotgraph'
require 'gv'

module Gv

  class Enumerator # :nodoc:
    include Enumerable

    def initialize(graph, getfirst, getnext)
      @g = graph
      @gf = getfirst
      @gn = getnext
    end

    def each
      item = Gv.send(@gf, @g)
      while item
        yield item
        item = Gv.send(@gn, @g, item)
      end
    end
  end

  def Gv.nodeiterator(g)
    Enumerator.new(g, :firstnode, :nextnode)
  end

end

class DotGraph
  
  # Renders the graph.
  #
  # _engine_  :: rendering engine to use
  #
  # All other attributes are propagated to #to_dot.
  #
  # Returns a hash of node names (as returned by #nid) and node positions
  # as strings.
  #
  # Usage:
  #  gp = g.render_graphviz(:twopi, "overlap=scale", "splines=true", ...) # Render the graph with the layout wanted
  #  g.nodeblock { |n| ["pos=\"#{gp[g.nid(n)]}\"", 'pin'] } # Set a new node attributes block. pos gives the positions, pin tells to stick on them.
  #  g.to_graphviz('graph1.pdf', :nop, :pdf) # output the resulting graph (the :nop render engine respects node positions)
  #  # change the graph, e.g. remove links etc...
  #  g.to_graphviz('graph2.pdf', :nop, :pdf) # output the changed graph with identical node positions
  def render_gv(engine, *attrs, &block)
    g = Gv.readstring(to_dot(*attrs, &block)) # stupid, but simple
    Gv.layout(g, engine.to_s)
    Gv.render(g) # now each node and edge has a pos attribute
    ni = Gv.nodeiterator(g)
    node_pos = Hash.new
    ni.each do |node|
      node_pos[ Gv.nameof(node)] = Gv.getv(node, 'pos')
    end
    return node_pos
  end

  # Calls the graphviz engine given in _engine_.
  #
  # _filename_ :: name of the file to be created
  # _engine_  :: command line to be executed.
  # _lang_ :: the output format (given as String or Symbol).
  #
  # All other attributes are propagated to #to_dot.
  #
  # Example:
  #   g.to_graphviz('graph.svg', 'twopi', :svg, "outputorder=edgesfirst", "node [ shape=point, style=filled ]" ])
  def to_gv(filename, engine, lang, *attrs, &block)
    g = Gv.readstring(to_dot(*attrs, &block)) # stupid, but simple
    Gv.layout(g, engine.to_s)

    if lang == :pspdf
      epstopdf(filename) do |tmpfile|
        Gv.render(g, 'ps', tmpfile)
      end
    else
      Gv.render(g, lang.to_s, filename)
    end
  end

  # see util/dotgraph-gv.rb
  alias to_graphviz to_gv

  # see util/dotgraph-gv.rb
  alias render_graphviz render_gv

end

