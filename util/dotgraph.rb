#!/usr/bin/ruby -w
# :title: Dot Graph - Ruby Lib
# =  Dot Graph Library

require 'set'

# = Graph class for creating dot-Files
#
# This class was created as a quick shot to create dot-files for graphviz.
#
# More and more stuff was added and now it's nearly a full featured graph
# class with support for R and others and some drawbacks due to its history.
class DotGraph
  # the objects representing the nodes of the graph. This is subject to change 
  # as we will introduce special DotGraph::Node objects in future when needed
  attr_reader :nodes
  # the Link objects representing the links between the nodes. 
  # Currently this is a Hash with the key being an Array with the source Node
  # as first and the destination node as second (last) component, and the
  # value being the Link object itself.
  attr_reader :links
  # boolean indicating whether the link count should be included in the output
  attr_accessor :linkcount
  # boolean indicating whether this is a directed graph 
  # (currently this has to be set at graph creation time. May change later).
  attr_reader :directed

  # _nodes_:: any Enumerable object giving the nodes of the graph
  # _attrs_:: 
  #   additional parameters:
  #   <i>:directed  => true</i> :: the graph is directed
  #   <i>:linkcount => true</i> :: the dotfile should show the link count
  # <i>&lproc</i> :: 
  #   if a block is given it is called for each node to generate the node 
  #   labels on output. If the block returns a string it is used as the
  #   label. Otherwise it should return an Enumerable whose elements are
  #   used as node parameters. E.g.:
  #    DotGraph.new(nodes) { |n| ["label=#{n.name}", 'style=filled', "fillcolor=#{n.size}"] }
  def initialize(nodes, attrs={}, &lproc)
    if nodes.kind_of?(Array)
      @nodes = nodes.dup
    else
      @nodes = nodes.to_a
    end
    @links = Hash.new

    # we collect links for each node. The following two Hashes
    # will be filled with nodes as keys and sets of Links as values.
    @sourcelinks = Hash.new { |h,k| h[k] = Set.new } # sourcenode as key
    @destlinks = Hash.new { |h,k| h[k] = Set.new }   # destnode as key

    @lproc = lproc || lambda { |n| n.node_id }
    @directed = attrs[:directed]
    if attrs.has_key?(:linkcount)
      @linkcount = attrs[:linkcount]
    else
      @linkcount = true
    end
  end
  
  # sets the block to be used to generate note labels for each node.
  # If no block is given it just returns the current block.
  def nodeblock(&nodeblock)
    @lproc = nodeblock    if nodeblock
    @lproc
  end

  # add a link to this graph.
  #
  # _src_, _dest_ ::
  #   source and destination of this link. If the graph is undirected
  #   they are sorted in canonical order (using #object_id).
  # _w_ :: weight of this link
  # _add_ :: addmode. 
  #   * if _true_ and the link (same _src_ and _dest_) already exists, _w_ 
  #     is added to the link weight.
  #   * if _false_, the maximum of the old link weight and _w_ is used.
  def link(src, dest, w=1, add=true)
    src, dest = dest, src  if (!@directed) && (src.object_id > dest.object_id)
    key = [src,dest]
    unless l = @links[key]
      l = Link.new(self, src, dest)
      @sourcelinks[src] << l
      @destlinks[dest] << l
      @links[key] = l
    end

    if add
      l.addweight(w)
    else
      l.maxweight(w)
    end
    l
  end

  # add a link with a timestamp
  def timelink(src, dest, time, w=1, add=true)
    l = link(src, dest, w, add)
    l << time
  end

  # sort timestamps of all links
  def sort_times
    @links.each_value { |l| l.sort_times }
  end

  # remove all links from this graph.
  #
  # if _w_ is given, only links with weight smaller _w_ are deleted.
  #
  # For convenience this method returns self (i.e. the DotGraph object).
  def remove_links(w=nil)
    w = w || (1.0/0)
    @links.delete_if { |k,l| 
      (l.weight<w) && 
      @sourcelinks[l.src].delete(l) &&   # always true
      @destlinks[l.dest].delete(l)       # always true
    }
    self
  end

  # remove all links from this graph, where source and destination are equal.
  #
  # For convenience this method returns self (i.e. the DotGraph object).
  def remove_self_links
    @links.delete_if { |k,l| 
      (l.src==l.dest) &&
      @sourcelinks[l.src].delete(l) &&   # always true
      @destlinks[l.dest].delete(l)       # always true
    }
    self
  end

  # remove nodes which have _no_ (incoming or outgoing) links.
  #
  # For convenience this method returns self (i.e. the DotGraph object).
  def remove_lonely_nodes
    @nodes.delete_if { |n| (@sourcelinks[n].empty? && @destlinks[n].empty?) }
    self
  end

  # remove nodes based on degree.
  #
  # _attrs_:
  # <i>:treshold</i> => 2 :: delete all nodes with smaller degree.
  # <i>:weight</i> => false :: 
  #   indicates how the degree is counted:
  #   _true_, <i>:add</i>:: sum of link weights
  #   _false_, <i>:count</i>:: number of links
  #   <i>:log</i>:: sum of log(linkweight+1)
  #   <i>:hirsch</i>::
  #     the Hirsch-index of the node. A node has Hirsch-index _k_ if there are
  #     at least _k_ links with weight _k_.
  # <i>:dir</i> => :all ::
  #   indicates which links are used for degree:
  #   <i>:in</i> :: only incoming links (node is dest)
  #   <i>:out</i> :: only outgoing links (node is src)
  #   <i>:all</i> :: both
  #   <i>:max</i> :: use the higher value of in and out
  #   <i>:min</i> :: use the lower value of in and out
  # <i>:type</i> => :plain ::
  #   indicates the algorithm to be used:
  #   <i>:plain</i> :: 
  #     Nodes are removed based on their degree in the current graph.
  #     Through the deletion process some nodes will loose links, so the
  #     new graph may contain nodes with degree lower than the treshold.
  #   <i>:full</i>, <i>:core</i> ::
  #     Nodes are recursively removed from the graph until all degrees keep
  #     the given treshold. This is equivalent to calling <i>:plain</i>
  #     several times (until a fixpoint is reached, i.e. the graph does not 
  #     longer change).
  # <i>:hirsch</i> ::
  #   setting this attribute to some number _b_ automatically sets
  #   <i>:weight</i> => <i>:hirsch</i>. Additionally _b_ is used as
  #   balancing factor (see #hirsch?).
  #   
  # <b>Caution:</b> For indirected graphs it is random whether a link is 
  # outgoing or incoming, so here only <i>:dir</i> => :all is useful!
  #
  # Example:
  #   g.remove_nodes(:treshold => 3, :weight => false, :dir => :out)
  # will remove all nodes which have currently less than 3 outgoing links.
  #   g.remove_nodes(:treshold => 3, :weight => false, :dir => :out, :type => :full)
  # will repeatedly remove all nodes with less than 3 outgoing links until
  # only nodes with at least 3 outgoing links are left.
  #
  # For convenience this method returns self (i.e. the DotGraph object).
  def remove_nodes(attr={})
    attr = {
      :treshold => 2,
      :weight => false,
      :dir => :all,
      :type => :plain
    }.merge(attr)

    treshold = attr[:treshold]
    weight = attr[:weight]
    dir = attr[:dir]
    plain = (attr[:type] == :plain)

    if b=attr[:hirsch]
      weight = :hirsch
    else
      b = 1
    end

    testnodes = @nodes
    delnodes = Set.new
    dellinks = Set.new

    finished = false

    while(!finished)
      ndels = if weight==:hirsch
                case attr[:dir]
                when :in
                  testnodes.select { |n| !hirsch?(@destlinks[n],treshold,b) }
                when :out
                  testnodes.select { |n| !hirsch?(@sourcelinks[n],treshold,b) }
                when :max
                  testnodes.select { |n| !(hirsch?(@destlinks[n],treshold,b) ||
                                     hirsch?(@sourcelinks[n],treshold,b)) }
                when :min
                  testnodes.select { |n| (!hirsch?(@destlinks[n],treshold,b) ||
                                    !hirsch?(@sourcelinks[n],treshold,b)) }
                else
                  testnodes.select { |n| !hirsch?(@destlinks[n] + 
                                            @sourcelinks[n],
                                            treshold,b) }
                end
              else
                case attr[:dir]
                when :in
                  testnodes.select { |n| n_indegree(n,weight) < treshold }
                when :out
                  testnodes.select { |n| n_outdegree(n,weight) < treshold }
                when :max
                  testnodes.select { |n| ((n_outdegree(n,weight) < treshold) &&
                                          (n_indegree(n,weight) < treshold)) }
                when :min
                  testnodes.select { |n| ((n_outdegree(n,weight) < treshold) ||
                                          (n_indegree(n,weight) < treshold)) }
                else
                  testnodes.select { |n| n_degree(n,weight) < treshold }
                end
              end
      
      delnodes.merge(ndels)
      testnodes = Set.new
   
      ndels.each do |n|
        @sourcelinks[n].each { |l| 
          d = l.dest 
          testnodes << d
          @destlinks[d].delete(l)
          dellinks << [n,d]
        }
        @destlinks[n].each { |l| 
          s = l.src
          testnodes << s
          @sourcelinks[s].delete(l)
          dellinks << [s,n]
        }
      end

      testnodes.subtract(delnodes)  # is this faster?

      finished = plain || testnodes.empty?

    end      

    dellinks.each { |a| @links.delete(a) }

    @nodes -= delnodes.to_a

    self
  end

  # returns true if the Enumerable links contains at least _treshold_ links
  # with at least weight _treshold_ (this is faster than computing the
  # real Hirsch-index).
  #
  # Each link-weight is multiplied with _balance_, this allows for rescaling.
  def hirsch?(links, treshold, balance=1)
    c=0
    links.each do |l| 
      c+=1 if l.weight*balance >= treshold
      return true if c >= treshold
    end
    return false
  end

  # Computes in-, out- and degrees of all nodes. Returns a hash with the
  # nodes as keys and arrays _a_ with outdegree (<i>a[0]</i>),
  # indegree (<i>a[1]</i>) and degree (<i>a[2]</i>) as values.
  # (please note: if the graph contains self-links, degree may be smaller
  # than indegree+outdegree)
  #
  # Take care: if the graph is _indirected_, it is random whether a link
  # counts as in- or outlink so only the degree is valid.
  #
  # _weight_:: indicates how the degree is counted:
  #            _true_, <i>:add</i>:: sum of link weights
  #            _false_, <i>:count</i>:: number of links
  #            <i>:log</i>:: sum of log(linkweight+1)
  def degrees(weight=false)
    
    h = Hash.new { |h,k| h[k] = [0,0,0] }
    @nodes.each { |n|
      h[n] = [n_outdegree(n,weight), n_indegree(n,weight), n_degree(n,weight)]
    }
    h
  end

  # computes the (weighted) out-degree of node n
  #
  # _weight_:: indicates how the degree is counted:
  #            _true_, <i>:add</i>:: sum of link weights
  #            _false_, <i>:count</i>:: number of links
  #            <i>:log</i>:: sum of log(linkweight+1)
  def n_outdegree(node, weight=false)
    case weight
    when true, :add
      @sourcelinks[node].inject(0) { |s,l| s+l.weight }
    when false, nil, :count
      @sourcelinks[node].length
    when :log
      @sourcelinks[node].inject(0) { |s,l| s+Math.log(l.weight+1) }
    end
  end

  # computes the (weighted) in-degree of node n
  #
  # _weight_:: indicates how the degree is counted:
  #            _true_, <i>:add</i>:: sum of link weights
  #            _false_, <i>:count</i>:: number of links
  #            <i>:log</i>:: sum of log(linkweight+1)
  def n_indegree(node, weight=false)
    case weight
    when true, :add
      @destlinks[node].inject(0) { |s,l| s+l.weight }
    when false, nil, :count
      @destlinks[node].length
    when :log
      @destlinks[node].inject(0) { |s,l| s+Math.log(l.weight+1) }
    end
  end

  # computes the (weighted) degree of node n
  #
  # (please note: if the graph contains self-links, degree may be smaller
  # than indegree+outdegree)
  #
  # _weight_:: indicates how the degree is counted:
  #            _true_, <i>:add</i>:: sum of link weights
  #            _false_, <i>:count</i>:: number of links
  #            <i>:log</i>:: sum of log(linkweight+1)
  def n_degree(node, weight=false)
    d = n_indegree(node,weight) + n_outdegree(node,weight)
    if l=@links[[node,node]]
      if weight 
        d -= l.weight
      else
        d -= 1
      end
    end
    d
  end



  DEGREESSORTNR = { :node=>0, :degree=>1, :out=>2, :in=>3} # :nodoc:
  # :call-seq:
  # pp_degrees(:sortby => 0, :up => true, ...)
  # pp_degrees(:sortby => 0, :up => true, ...) { |n| ... }
  # Pretty print the degrees of all nodes.
  # <i>:sortnr</i>:: 
  #    by which column the output should be sorted
  #    0 or :node   :: by node
  #    1 or :degree :: by degree
  #    2 or :out    :: by outdegree
  #    3 or :in     :: by indegree
  # <i>:up</i>:: _true_ for ascending, _false_ for descending sort.
  # <i>:weight</i>:: see degrees.
  # <i>&block</i>:: 
  #   if a block is given it is called with each node and its 
  #   return value (preferable a String) is used for printing the node.
  #   Otherwise the block given while creating the graph or the default
  #   block is used, respectively.
  def pp_degrees(params={}, &block)
    sortnr = params[:sortby] || 0
    up = params[:up] || true
    weight = params[:weight] || :count
    sortnr = DEGREESSORTNR[sortnr] if sortnr.kind_of?(Symbol)
    lproc = block || @lproc
    if @directed
      fmt = "%-30s: %4s %4s %4s"
    else
      fmt = "%-30s: %4s"
    end
    puts fmt % ["Node","deg","out","in"]
    d = degrees(weight).collect { |n,a|
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

  # Compute the adjacency matrix (Array of Arrays) of this graph.
  def adjacencymatrix(inf=0)
    # prepare matrix
    ni = Hash.new
    @nodes.each_with_index { |n,i| ni[n]=i }
    matrix = Array.new(@nodes.length) { Array.new(@nodes.size, inf) }
    matrix.each_with_index { |a,i| a[i]=0 }
    @links.each_key do |s,d| 
      i = ni[s]
      j = ni[d]
      if i!=j
        matrix[i][j] += 1 
        matrix[j][i] += 1 unless @directed
      end
    end
    matrix
  end

  # Compute a flat edge list representation of this graph (useful for R).
  def flat_edgelist
    ni = Hash.new
    @nodes.each_with_index { |n,i| ni[n]=i+1 }
    sa = []
    da = []
    @links.each_value do |l| 
      sa << ni[l.src]
      da << ni[l.dest]
    end
    sa + da
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
  # Any strings given as attributes are included as graph attributes 
  # (e.g. <tt>"overflow=scale"</tt> or 
  # <tt>"node [shape=circle,fixedsize=true,width=0.1"</tt>).
  #
  # if a block is given it is called with the link count of each link 
  # and the return value is used as link attribute (don't forget the []).
  def to_dot(*attrs, &block)
    d = "#{'di' if @directed}graph G {\n"
    d << attrs.collect { |a| "  #{a};\n"}.join
    @nodes.each { |n| 
      d << "  \"#{nid(n)}\" [#{nodeparams(n)}];\n"}
    @links.sort_by { |k,l| l.weight }.each { |k,l| d << l.to_dot(&block) }
    d << "}\n"
  end
  
  # Writes graph to dotfile. See #to_dot.
  def to_dotfile(filename, *attrs, &block)
    File.open(filename,'w') { |file| file << to_dot(*attrs, &block) }
  end

  # Creates a LaTeX String representing   
  #
  # The following named parameters can be given 
  # (see <tt>dot2tex</tt>-documentation, pgfmanual and GraphViz-documentation
  # for details):
  # :alg:: the graphviz algorithm to be used (Strings and Symbols allowed): 
  #        "dot" (default), "neato", "twopi", "circo", "fdp".
  # :fmt:: the output format: "pgf" (default), "pst", "tikz".
  # :figonly:: if true (default) only the LaTeX/tikz-code for the graph iself
  #            is generated, for inclusion in a larger LaTeX-file. If false
  #            a stand-alone LaTeX-document is generated.
  # :figpreamble:: any LaTeX-code to be included at picture start. Try e.g.
  #                <i>:figpreamble => '\scriptsize'</i> for smaller font.
  # :graphstyle:: any parameter to the corresponding picture-environment
  #               (+tikzpicture+ or +pspicture+, respectively). Try e.g. 
  #               <i>:graphstyle => 'scale=0.5'</i> to scale down the graph
  #               to 50%.
  # :texmode:: dot2tex text mode: 
  #            "raw":: (default) any string is subject to LaTeX interpretation
  #            "math":: as above but in TeX math mode
  #            "verbatim":: all special characters escaped, no interpretation.
  # :exp:: if no block is given the (Float) value of this parameter is used
  #        to darken/lighten the link color (default is 0.2).
  # :comment:: this parameter is added as a TeX comment at the beginning of 
  #            TeX output. So if you look in the TeX file later you may be
  #            to find out what kind of graph it contains.
  # :dtpars:: a String directly passed to the <tt>dot2tex</tt> command
  #           line to be able to use more exotic switches like <tt>-w</tt>.
  # :attrs:: a String or an array of Strings which's values are passed as 
  #          parameters to #to_dot. (defaults to []). 
  #
  # Try
  #  g.to_tex(:alg => :neato, attrs => 'overlap=scale')
  #
  # or
  #  wiki.coauthorgraph { |u| 
  #    "#{(u.role||'')[0..0]}_{#{u.uid}}"  # role with uid as index
  #  }.to_texfile('coauthorrolegraph.tex', 
  #    :alg=>:neato,                  # neato algorithm
  #    :graphstyle=>'scale=0.3',      # downscale to fit on paper
  #    :texmode=>"math",              # the nodes need math mode 
  #    :figpreamble=>'\scriptsize',   # smaller font needed
  #    :comment=>'Example',           # so we see what this is
  #    :attrs=>['overlap=scale',      # looks best
  #             'sep=0.01',           # nodes may be set close
  #             'node [style="fill=white"]']) # and filled white
  #
  # This method depends on 
  # <tt>dot2tex</tt> (http://www.fauskes.net/code/dot2tex/),
  # (pdf)latex (with package pgf) and certainly graphviz to be installed.
  def to_tex(params={})
    params = { :alg => 'dot', :fmt => 'pgf', :figonly => true,
      :exp => 0.2, :texmode => 'raw',
      :attrs => [] }.merge(params)
    exp = params[:exp]
    maxc = @links.collect { |k,l| l.weight }.max
    if block_given?
      srcdot = to_dot(*params[:attrs])
    else
      srcdot = to_dot(*params[:attrs]) { |c|
        "[style=\"color=black!#{(c.to_f/maxc)**exp*100}\"]" }
    end
    return srcdot if params[:dotonly] # for debugging
    p=""
    cmd = "dot2tex --prog=#{params[:alg]} "
    cmd << '--figonly ' if params[:figonly]
    cmd << "--graphstyle=\"#{p}\" " if (p = params[:graphstyle])
    cmd << "--figpreamble=\"#{p}\" " if (p = params[:figpreamble])
    cmd << "-t#{p}" if p = params[:texmode]
    cmd << "#{params[:dtpars]}"
    tex = "% Graph generated from dotgraph.rb with heavy help from dot2tex.\n"
    tex << "DotGraph#to_tex(#{params.inspect[1..-2]})\n".gsub(/^/,'% ')
    tex << "% This called:\n"
    tex << "#{cmd}\n".gsub(/^/,'% ')
    tex << "#{params[:comment]}\n".gsub(/^/,'% ')
    tex << "%\n% You may need the following packages:\n"
    tex << "% \\usepackage[x11names, rgb]{xcolor}\n"
    tex << "% \\usepackage{tikz}\n"
    tex << "% \\usetikzlibrary{snakes,arrows,shapes}\n"
    tex << IO.popen(cmd,'r+') do |dot2tex|
      dot2tex << srcdot
      dot2tex.close_write
      dot2tex.read
    end
    tex
  end
  
  # Writes graph to texfile. See #to_tex.
  def to_texfile(filename, params={})
    File.open(filename,'w') { |file| file << to_tex(params) }
  end

  SONIA_DURATION = 7*24*60*60
  SONIA_SPEEDUP = 24*60*60
  # Creates a String representing the DotGraph in +son+ format as used
  # by SONIA (http://www.stanford.edu/group/sonia/)
  #
  # To get a useful SONIA file you need links with timestamps (e.g.
  # created by timelink), as given by 
  # Mediawiki::Wiki.timedinterlockingresponsegraph:
  #   wiki.timedinterlockingresponsegraph.to_son
  #
  # _options_::
  #   a hash with further options:
  #   <i>:duration</i>=><tt>SONIA_DURATION</tt>:: 
  #     time (in seconds) a link should last.
  #   <i>:speedup</i>=><tt>SONIA_SPEEDUP</tt>:: 
  #     time (in seconds) is divided by this factor.
  #   <i>:node_colors</i>::
  #     the color of the node in its 5 phases (see description below for
  #     details). Defaults to
  #       :node_colors => ['White','LightGray','DarkGray','LightGray','White']
  #     Instead of using named colors you may give an Array of
  #     three floats between 0 and 1 representing RGB:
  #       :node_colors => [[1,1,1], [0.6,0.4,0.4], ...]
  #     Due to the restrictions of the SONIA file format you may not mix
  #     RGB and named colors, sorry.
  #   <i>:node_LabelColor</i>:: 
  #     the color of the node label in its 5 phases (see description below
  #     for details). Due to the restrictions of the SONIA file format only
  #     named colors are allowed, sorry.
  #   <i>:node_BorderColor</i>:: 
  #     the color of the node border in its 5 phases (see description below
  #     for details). Due to the restrictions of the SONIA file format only
  #     named colors are allowed, sorry.
  #   <i>:node_BorderWidth</i>:: 
  #     the width of the node border in its 5 phases (see description below
  #     for details) as float array.
  #   <i>:node_Size</i>:: 
  #     the size of the node in its 5 phases (see description below
  #     for details) as float array.
  #   <i>:node_Shape</i>:: 
  #     the shape of the node in its 5 phases (see description below
  #     for details) as String Array. 
  #     SONIA only understands 'ellipse' or 'rect'.
  #   <i>:node_IconURL</i>:: 
  #     the icon of the node in its 5 phases (see description below
  #     for details) as Array of URLS pointing to jpegs. 
  #     SONIA only understands 'ellipse' or 'rect'.
  #
  # Each node may change through 5 phases:
  # * Time before creation
  # * Creation time to time of first event
  # * Time from first to last event
  # * Time from last event to deletion
  # * Time past deletion
  # These times are archieved by calling the methods 
  # <tt>time_of_creation</tt>, <tt>time_of_first_event</tt>, 
  # <tt>time_of_last_event</tt>, <tt>time_of_deletion</tt> on each node.
  # If node does not respond to one of these methods, returns _nil_ or
  # the resulting timespan is not greater 0, the timespan is skipped.
  # You may give different colors, borders, shapes,sizes and even icons for 
  # each phase (some of them are subject to change for we may use node size
  # later to present additional information).
  #
  # SONIA understands the following color names: 
  # <tt>Black DarkGray LightGray White Cyan Green Magenta Orange Pink 
  # Red Yellow Blue</tt>.
  #
  # Not all phases may be present for all nodes:
  # As e.g. users do not have a deletion time the values at position four
  # of the corresponding Arrays are not used, users without edits may
  # only show two phases at all and so on.
  #
  # We decided to keep each node for the whole timespan including the time
  # before creation and after deletion, because the resulting SONIA layouts
  # are less confusing if all nodes are present all the time. To hide them
  # simply set their colors to white or there sizes to zero. We may add a 
  # flag later changing this behaviour.
  #
  # If a block is given, it is used for node labeling.
  def to_son(options={}, &block)
    duration = options[:duration] || SONIA_DURATION
    spu = options[:speedup] || SONIA_SPEEDUP
    nodeRGB = nil
    nodecolornames = options[:node_colors] || 
      ['White','LightGray','DarkGray','LightGray','White']
    if nodecolornames.first.kind_of?(Array)
      nodeRGB = nodecolornames
      nodecolornames = nil
    end
    nodelabelcolors = options[:node_LabelColor]
    nodebordercolors = options[:node_BorderColor]
    nodeborderwidths = options[:node_BorderWidth]
    nodesizes = options[:node_Size]
    nodeshapes = options[:node_Shape]
    nodeURLs = options[:node_IconURL]

    son = "// SONIA Graph File\n"
    son << "// Created by DotNet/mediawikiparser\n"
    lproc = block || @lproc
    # First the links to get min and max time.
    mintime = 1.0/0   # +Infinity
    maxtime = -1.0/0  # -Infinity
    sonlinks = "FromId\tToId\tStartTime\tEndTime\n"
    @links.each_value do |l|
      l.timeline.each do |t| 
        t = t.to_f/spu
        sonlinks << "#{nid(l.src)}\t#{nid(l.dest)}\t#{t}\t#{t}\n"
        mintime = t if t<mintime
        maxtime = t if t>maxtime
      end
    end
    maxtime += (duration.to_f/spu)
    # now the nodes

    @nodes.each do |n|
      next unless n.respond_to?(:time_of_first_event)
      # We do not use time_of_creation here as this is not reliable.
      # E.g. the system user has a default creation time.
      # We could change this but it's not worth the efford.
      if t = n.time_of_first_event
        t = t.to_f/spu
        mintime = t if t<mintime
      end
    end
    son << "AlphaId\tLabel"
    son << "\tColorName"                   if nodecolornames
    son << "\tRedRGB\tGreenRGB\tBlueRGB"   if nodeRGB
    son << "\tLabelColor"                  if nodelabelcolors
    son << "\tBorderColor"                 if nodebordercolors
    son << "\tBorderWidth"                 if nodeborderwidths
    son << "\tNodeSize"                    if nodesizes
    son << "\tNodeShape"                   if nodeshapes
    son << "\tIconURL"                     if nodeURLs
    son << "\tStartTime\tEndTime\n"
    tt = [mintime, nil, nil, nil, nil, maxtime]
    @nodes.each do |n|
      tt[1] = tt[2] = tt[3] = tt[4] = nil
      tt[1] = n.time_of_creation.to_f/spu if n.respond_to?(:time_of_creation)
      tt[2] = n.time_of_first_event.to_f/spu if n.respond_to?(:time_of_first_event)
      tt[3] = n.time_of_last_event.to_f/spu if n.respond_to?(:time_of_last_event)
      tt[4] = n.time_of_deletion.to_f/spu if n.respond_to?(:time_of_deletion)
## mintime -> creationtime -> first action -> last action -> deletion -> maxtime...
      i=0
      tt.inject do |ta, tb|
        if tb
          if (ta<tb)
            son << "#{nid(n)}\t#{lproc.call(n)}"
            son << "\t#{nodecolornames[i]}"       if nodecolornames
            son << "\t#{nodeRGB[i][0]}\t#{nodeRGB[i][1]}\t#{nodeRGB[i][1]}" if nodeRGB
            son << "\t#{nodelabelcolors[i]}"      if nodelabelcolors
            son << "\t#{nodebordercolors[i]}"     if nodebordercolors
            son << "\t#{nodeborderwidths[i]}"     if nodeborderwidths
            son << "\t#{nodesizes[i]}"            if nodesizes
            son << "\t#{nodeshapes[i]}"           if nodeshapes
            son << "\t#{nodeURLs[i]}"             if nodeURLs
            son << "\t#{ta}\t#{tb}\n"
          end
          i += 1
          tb
        else
          i += 1
          ta
        end         
      end
#      son << "#{nid(n)}\t#{lproc.call(n)}\t#{mintime}\t#{maxtime}\n"
    end
    son << sonlinks # add the links
    son
  end

  # Writes graph to sonfile. See #to_son.
  #
  # Example usage:
  #   wiki.timedinterlockingresponsegraph.to_sonfile('test.son')
  #
  # _filename_ :: name of the son file.
  def to_sonfile(filename, options={}, &block)
    File.open(filename,'w') { |file| file << to_son(options, &block) }
  end

  def nodeparams(node) # :nodoc:
    np = @lproc.call(node)
    case np
    when String, Symbol : "label=\"#{np.tr('"',"'")}\""
    when Enumerable : np.join(', ')
    end
  end

  # maybe we can use this for R plots:
  def nodelabel(node) # :nodoc:
    np = @lproc.call(node)
    case np
    when String, Symbol : np.to_s
    when Enumerable : (np.find { |s| s =~ /label="(.*?)"/ } && $1)
    end    
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

  class Link
    # the source Node of this Link
    attr_reader :src
    # the destination Node of this Link
    attr_reader :dest
    # the weight of this Link
    attr_accessor :weight
    # anny further Link attributes
    attr_reader :attrs
    # the Link timeline (used e.g. for Sonia)
    attr_reader :timeline

    # creates a new Link object. Do not use this directly but use 
    # Dotgraph#link or Dotgraph#timelink
    def initialize(graph, src, dest, attrs={})
      @graph = graph
      @src = src
      @dest = dest
      @weight = 0
      @attrs = attrs
      @timeline = []
    end

    # increases link weight by _w_.
    def addweight(w)
      @weight += w
    end

    # sets link weight to the maximum of the old link weight and _w_.
    def maxweight(w)
      @weight = w if w>@weight
    end

    # String representation of this Link in dotfile syntax.
    def to_dot
      s = "  \"#{nid(@src)}\" #{edgesymbol} \"#{nid(@dest)}\" "
      s << "[#{@attrs.join(',')}]" unless @attrs.empty?
      if block_given?
        s << yield(@weight)
      else
        s << weightlabel(@weight) if linkcount
      end
      s << ";\n"
      s
    end
    
    # link symbol in dotfile syntax.
    def edgesymbol
      directed ? '->' : '--'
    end
    
    def nid(o) # :nodoc:
      DotGraph::nid(o)
    end
    
    def linkcount # :nodoc:
      @graph.linkcount
    end
    
    def directed # :nodoc:
      @graph.directed
    end
    
    # weight label of this Link in dotfile syntax
    def weightlabel(count)
    "[weight=#{count},taillabel=\"#{count}\",fontcolor=\"grey\",fontsize=5,labelangle=0]"
    end

    # add a time to the timeline
    def <<(t)
      @timeline << t
    end

    # sort the timeline
    def sort_times
      @timeline.sort!
    end
  end
end
