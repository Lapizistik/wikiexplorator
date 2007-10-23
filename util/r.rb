#! /usr/bin/ruby -w
#
# = RSRuby DotGraph library
#
# This library extends the DotGraph class with methods for social network 
# analysis. It is based on the RSRuby library, which uses the statistical
# computing environment R <http://www.r-project.org> and on the R library sna. 
# 
# Please require explicitly (for R, sna, RSRuby  may not be installed 
# everywhere).
#
# == Usage
# 
#  require 'util/r' 
#  g = wiki.coauthorgraph  # get a DotGraph object
#  g.pp_betweenness(:sortby => :value, :rescale =>true) # compute betweenness
#  g.pp_closeness          # and closeness (not useful on disconnected graphs)
#
# You may also use R directly:
#
#  r = RSRuby.instance     # get the R interpreter
#  adjm = g.to_r_matrix    # get the adjacency matrix as R object
#  puts r.efficiency(adjm) # compute the efficiency using R
#  r.gplot(adjm)           # and plot the graph using R


r_home = `R cmd BATCH RHOME`.chomp
if $?.exitstatus>0
  warn 'R not found! rsruby will _not_ work!'
else
  ENV['R_HOME']=r_home
  require 'rsruby'
  RSRuby.instance.library('sna')
  
  class DotGraph
    # create an R matrix object representing the adjacency matrix of this graph
    def to_r_matrix
      r = RSRuby.instance
      c = r.array.conversion
      r.array.conversion = RSRuby::NO_CONVERSION
      m = r.array(adjacencymatrix.flatten,[@nodes.length]*2)
      r.array.conversion = c
      m
    end

    # computes the betweenness for all nodes using R.
    #
    # _params_:: 
    #     Hash with all named parameters for R::betweenness.
    #     Try e.g.:
    #       g.betweenness(:rescale => true) 
    #     or
    #       g.betweenness(:cmode => 'undirected', :rescale => true)
    #     By default <i>:gmode</i> and <i>:cmode</i> are set automatically.
    def betweenness(*params)
      params = params.first || {}
      r = RSRuby.instance
      params[:gmode] = (@directed ? 'digraph' : 'graph') unless params[:gmode]
      params[:cmode] = (@directed ? 'directed' : 'undirected') unless params[:cmode]
      b = r.betweenness(to_r_matrix, params) 
      h = Hash.new
      @nodes.each_with_index { |n,i| h[n] = b[i] }
      h
    end
    
    # :call-seq:
    # pp_betweenness(:sortby => 0, :up => true, ...)
    # pp_betweenness(:sortby => 0, :up => true, ...) { |n| ... }
    #
    # Pretty print the betweenness of all nodes (using R).
    #
    # _params_ is a Hash of named parameters:
    # <i>:up</i>, <i>:sortby</i> and the block (if given) are passed to 
    # #pp_key_value (see there), all other params are passed to 
    # #betweenness (see there).
    def pp_betweenness(*params, &block)
      params = params.first || {}
      sortby = params.delete(:sortby) || 0
      up = params.delete(:up) || true
      pp_key_value(betweenness(params), sortby, up, &block)
    end

    # computes the closeness for all nodes using R.
    #
    # _params_:: 
    #     Hash with all named parameters for R::closeness.
    #     Try e.g.:
    #       g.closeness(:rescale => true) 
    #     or
    #       g.closeness(:cmode => 'undirected', :rescale => true)
    #     By default <i>:gmode</i> and <i>:cmode</i> are set automatically.
    def closeness(*params)
      params = params.first || {}
      r = RSRuby.instance
      params[:gmode] = (@directed ? 'digraph' : 'graph') unless params[:gmode]
      params[:cmode] = (@directed ? 'directed' : 'undirected') unless params[:cmode]
      b = r.closeness(to_r_matrix, params)
      h = Hash.new
      @nodes.each_with_index { |n,i| h[n] = b[i] }
      h
    end

    # :call-seq:
    # pp_closeness(:sortby => 0, :up => true, ...)
    # pp_closeness(:sortby => 0, :up => true, ...) { |n| ... }
    #
    # Pretty print the closeness of all nodes (using R).
    #
    # _params_ is a Hash of named parameters:
    # <i>:up</i>, <i>:sortby</i> and the block (if given) are passed to 
    # #pp_key_value (see there), all other params are passed to 
    # #closeness (see there).
    def pp_closeness(*params, &block)
      params = params.first || {}
      sortby = params.delete(:sortby) || 0
      up = params.delete(:up) || true
      pp_key_value(closeness(params), sortby, up, &block)
    end


    # computes the prestige for all nodes using R.
    #
    # _params_:: 
    #     Hash with all named parameters for R::prestige.
    #     Try e.g.:
    #       g.prestige(:rescale => true) 
    #     or
    #       g.prestige(:cmode => 'indegree', :rescale => true)
    #     By default <i>:gmode</i> is set automatically.
    def prestige(*params)
      params = params.first || {}
      r = RSRuby.instance
      params[:gmode] = (@directed ? 'digraph' : 'graph') unless params[:gmode]
      b = r.prestige(to_r_matrix, params)
      h = Hash.new
      @nodes.each_with_index { |n,i| h[n] = b[i] }
      h
    end

    # :call-seq:
    # pp_prestige(:sortby => 0, :up => true, ...)
    # pp_prestige(:sortby => 0, :up => true, ...) { |n| ... }
    #
    # Pretty print the prestige of all nodes (using R).
    #
    # _params_ is a Hash of named parameters:
    # <i>:up</i>, <i>:sortby</i> and the block (if given) are passed to 
    # #pp_key_value (see there), all other params are passed to 
    # #prestige (see there).
    def pp_prestige(*params, &block)
      params = params.first || {}
      sortby = params.delete(:sortby) || 0
      up = params.delete(:up) || true
      pp_key_value(prestige(params), sortby, up, &block)
    end


    SORTBY = { :node=>0, :value=>1} # :nodoc:
    # Pretty print a hash with nodes as keys.
    # _h_:: the Hash to be printed (keys are the nodes)
    # _sortby_:: 
    #    by which column the output should be sorted
    #    0 or :node   :: by node
    #    1 or :value  :: by betweenness
    # _up_:: _true_ for ascending, _false_ for descending sort.
    # <i>&block</i>:: 
    #   if a block is given it is called with each node and its 
    #   return value (preferable a String) is used for printing the node.
    #   Otherwise the block given while creating the graph or the default
    #   block is used, respectively.
    def pp_key_value(h, sortby=0, up=true, &block)
      sortby = SORTBY[sortby] if sortby.kind_of?(Symbol)
      lproc = block || @lproc
      puts "%-30s: %20s" % ["Node","betweenness"]
      d = h.collect { |n,v|
        [lproc.call(n), v]
      }
      d = if up
            d.sort { |a,b| sorter_nan(a[sortby],b[sortby]) }
          else
            d.sort { |b,a| sorter_nan(a[sortby],b[sortby]) }
          end
      d.each { |a| puts("%-30s: %20.10f" % a) }
      nil
    end
    
    private
    def sorter_nan(a,b)
      a=0 if a.respond_to?(:nan?) && a.nan?
      b=0 if b.respond_to?(:nan?) && b.nan?
      a<=>b
    end
  end
end
