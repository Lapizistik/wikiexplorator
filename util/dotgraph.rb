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
  #           <i>:directed</i> :: give this Symbol if the graph is directed
  #           <i>:linkcount</i> :: the dotfile should show the link count
  #           <i>:nolinkcount</i> :: the dotfile should show the link count
  # <i>&lproc</i> :: 
  #   if a block is given it is called for each node to generate the node 
  #   labels on output.
  def initialize(nodes, *attrs, &lproc)
    @nodes = nodes.to_a
    @lproc = lproc || lambda { |n| n.node_id }
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

  # add a link to this graph.
  #
  # _src_, _dest_ ::
  #   source and destination of this link. If the graph is undirected
  #   they are sorted in canonical order (using #object_id).
  # _w_ :: weight of this link
  # _add_ :: 
  #   * if _true_ and the link (same _src_ and _dest_) already exists,
  #     _w_ is added to the link weight.
  #   * if _false_, the maximum of the old link weight and _w_ is used.
  def link(src, dest, w=1, add=true)
    src, dest = dest, src  if !@directed && (src.object_id > dest.object_id)
    # the following lines are not very efficient: 
    # a lot of Link objects may be generated which are never used again later.
    # 
    if add
      @links[Link.new(self, src, dest)] += w
    else
      l = Link.new(self, src, dest)
      @links[l] = w if w>@links[l]
    end
  end

  # remove all links from this graph.
  #
  # if _w_ is given, only links with weight smaller _w_ are deleted.
  #
  # For convenience this method returns self (i.e. the DotGraph object).
  def del_links(w=nil)
    w = w || (1.0/0)
    @links.delete_if { |k,v| v<w }
    self
  end
  
  # Computes in- and out-degrees of all nodes. Returns a hash with the
  # nodes as keys and arrays _a_ with outdegree (<i>a[0]</i>) and
  # indegree (<i>a[1]</i>) as values.
  #
  # Take care: if the graph is _indirected_, it is random whether a link
  # counts as in- or outlink so only the sum of both is valid.
  def degrees
    h = Hash.new { |h,k| h[k] = [0,0] }
    @nodes.each { |n| h[n] = [0,0] }   # prefill
    @links.each { |l,c|
      h[l.src][0]  += c
      h[l.dest][1] += c
    }
    h
  end

  DEGREESSORTNR = { :node=>0, :degree=>1, :out=>2, :in=>3} # :nodoc:
  # Pretty print the degrees of all nodes.
  # _sortnr_:: 
  #    by which column the output should be sorted
  #    0 or :node   :: by node
  #    1 or :degree :: by degree
  #    2 or :out    :: by outdegree
  #    3 or :in     :: by indegree
  # _up_:: _true_ for ascending, _false_ for descending sort.
  # <i>&block</i>:: 
  #   if a block is given it is called with each node and its 
  #   return value (preferable a String) is used for printing the node.
  #   Otherwise the block given while creating the graph or the default
  #   block is used, respectively.
  def pp_degrees(sortnr=0, up=true, &block)
    sortnr = DEGREESSORTNR[sortnr] if sortnr.kind_of?(Symbol)
    lproc = block || @lproc
    if @directed
      fmt = "%-30s: %4s %4s %4s"
    else
      fmt = "%-30s: %4s"
    end
    puts fmt % ["Node","deg","out","in"]
    d = degrees.collect { |n,a|
      [lproc.call(n), a[0]+a[1]]+a
    }
    d = if up
          d.sort { |a,b| a[sortnr]<=>b[sortnr] }
        else
          d.sort { |b,a| a[sortnr]<=>b[sortnr] }
        end
    d.each { |a| puts(fmt % a) }
    nil
  end

  def adjacencymatrix(inf=0)
    # prepare matrix
    ni = Hash.new
    @nodes.each_with_index { |n,i| ni[n]=i }
    matrix = Array.new(@nodes.length) { Array.new(@nodes.size, inf) }
    matrix.each_with_index { |a,i| a[i]=0 }
    @links.each_key { |l| 
      i = ni[l.src]
      j = ni[l.dest]
      if i!=j
        matrix[i][j] = 1 
        matrix[j][i] = 1 unless @directed
      end
    }
    matrix
  end

  # compute distance matrix for all nodes
  # this is native ruby and therefor slow on large datasets
  def distances_native(debug=false)
    matrix = adjacencymatrix
    # compute pathes (Floyd)
    matrix.each_index do |k|
      print '.' if debug
      matrix.each_index do |i|
        matrix.each_index do |j|
          d = matrix[i][k]+matrix[k][j]
          matrix[i][j] = d if d<matrix[i][j]
        end
      end
    end
  end

  # returns a String representing the whole graph in +dot+-Syntax 
  # (see GraphViz http://www.graphviz.org/ for description).
  #
  # Any attributes given are included as graph attributes 
  # (e.g. <tt>"overflow=scale"</tt>).
  def to_dot(*attrs)
    d = "#{'di' if @directed}graph G {\n"
    d << attrs.collect { |a| "  #{a};\n"}.join
    @nodes.each { |n| 
      d << "  \"#{nid(n)}\" [label=\"#{@lproc.call(n).to_s.tr('"',"'")}\"];\n"}
    @links.each { |l,count| d << l.to_dot(count) }
    d << "}\n"
  end
  
  # Writes graph to dotfile. See #to_dot.
  def to_dotfile(filename, *attrs)
    File.open(filename,'w') { |file| file << to_dot(*attrs) }
  end
  
  def DotGraph::nid(o) # :nodoc:
    if o.respond_to?(:node_id)
      o.node_id
    else
      "n%x" % o.object_id
    end
  end
  
  def nid(o) # :nodoc:
    DotGraph::nid(o)
  end
  
  class Link # :nodoc:
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
