#!/usr/bin/ruby -w
# :title: Dot Graph - Ruby Lib
# =  Dot Graph Library

require 'set'

# = Graph class for creating dot-Files
class DotGraph
  # the objects representing the nodes of the graph. This is subject to change 
  # as we will introduce special DotGraph::Node objects in future when needed
  attr_reader :nodes
  # the Link objects representing the links between the nodes. 
  # Currently this is a Hash with the key being a Link object and the value 
  # being a number representing the nr. of times the link was set 
  # (subject to change)
  attr_reader :links
  # boolean indicating whether the link count should be included in the output
  attr_reader :linkcount
  # boolean indicating whether this is a directed graph 
  # (currently this has to be set at graph creation time. May change later).
  attr_reader :directed

  # _nodes_:: any Enumerable object giving the nodes of the graph
  # _attrs_:: any number of flags:
  #           <em>:directed</em> :: give this Symbol if the graph is directed
  #           <em>:linkcount</em> :: the dotfile should show the link count
  #           <em>:nolinkcount</em> :: the dotfile should show the link count
  def initialize(nodes, *attrs, &lproc)
    @nodes = nodes.to_a
    @lproc = lproc || lambda { |n| n.label }
    @links = Hash.new(0)
    @linkcount = true
    attrs.each do |attr|
      case attr
      when :directed : @directed = true
      when :linkcount : @linkcount = true
      when :nolinkcount : @linkcount = false
      end
    end
  end
  def link(src, dest, *attrs)
    src, dest = dest, src  if !@directed && (src.object_id > dest.object_id)
    # the following line is not very efficient: 
    # a lot of Link objects may be generated which are never used again later.
    # 
    @links[Link.new(self, src, dest, *attrs)] += 1
  end
  
  def to_dot(*attrs)
    d = "#{'di' if @directed}graph G {\n"
    d << attrs.collect { |a| "  #{a};\n"}.join
    @nodes.each { |n| 
      d << "  \"#{nid(n)}\" [label=\"#{@lproc.call(n).tr('"',"'")}\"];\n" }
    @links.each { |l,count| d << l.to_dot(count) }
    d << "}\n"
  end
  
  def to_dotfile(filename, *attrs)
    File.open(filename,'w') { |file| file << to_dot(*attrs) }
  end
  
  def DotGraph::nid(o)
    if o.respond_to?(:node_id)
      o.node_id
    else
      "n%x" % o.object_id
    end
  end
  
  def nid(o)
    DotGraph::nid(o)
  end
  
  class Link
    attr_reader :src, :dest, :attr
    def initialize(graph, src, dest, *attrs)
      @graph = graph
      @src = src
      @dest = dest
      @attrs = attrs
    end
    def to_dot(count)
      s = "  \"#{nid(@src)}\" #{edgesymbol} \"#{nid(@dest)}\" "
      s << "[#{@attrs.join(',')}]" unless @attrs.empty?
      s << weightlabel(count) if linkcount
      s << ";\n"
      s
    end
    
    def edgesymbol
      directed ? '->' : '--'
    end
    
    def nid(o)
      DotGraph::nid(o)
    end
    
    def linkcount
      @graph.linkcount
    end
    
    def directed
      @graph.directed
    end
    
    def weightlabel(count)
      "[weight=#{count},taillabel=\"#{count}\",fontcolor=\"grey\",fontsize=5,labelangle=0]"
    end
    
    def eql?(other)
      (@src==other.src) && (@dest==other.dest) && (@attr==other.attr)
    end
    def hash
      @src.hash ^ @dest.hash
    end
  end
end
