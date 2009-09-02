#! /usr/bin/ruby -w
#
# = RSRuby DotGraph library
#
# This library extends the DotGraph class with methods for social network 
# analysis. It is based on the RSRuby library which uses the statistical
# computing environment R <http://www.r-project.org> and on the R library sna. 
# 
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
#
# The main methods for direct R access are <tt>to_r_matrix</tt> and
# <tt>to_r_network</tt>, which both return R objects. How to deal with them
# from within ruby is described in the RSRuby manual 
# (http://rubyforge.org/docman/?group_id=1909).
#
# Alternatively you can dump an R network to disk using 
# <tt>to_r_network_file</tt> and load it in R afterwards.

begin # let's see whether R is there and available
  # Unfortunately windows and unices (Linux, MacOS, ...) differ here:
  # While windows throws an exception if the command is missing,
  # unix just reports by exitstatus.
  r_home = `R cmd BATCH RHOME`.chomp     # Windows: Errno::ENOENT if missing
  raise Errno::ENOENT if $?.exitstatus>0 # Unix: we raise the error by hand
rescue 
  raise LoadError, 'R not found! rsruby will _not_ work!'
else
  ENV['R_HOME']=r_home
  require 'rsruby'
  RSRuby.instance.library('sna')
  RSRuby.instance.library('network')
  RSRuby.instance.library('ergm')
  
  class DotGraph
    R = RSRuby.instance
    # create an R matrix object representing the adjacency matrix of this graph
    def to_r_matrix
      c = R.array.conversion
      R.array.conversion = RSRuby::NO_CONVERSION
      m = R.array(adjacencymatrix.flatten,[@nodes.length]*2)
      R.array.conversion = c
      m
    end

    # create an R matrix object representing this graph
    #
    # r_params is a Hash of parameters (attributes) for R network generation.
    # If a block is given it is evaluated on each node and the result is 
    # assigned to the special R vertex attribute "attr".
    def to_r_network(r_params={})
      r_params = r_params.dup # we use a copy, as we wanna modify it.
      r_params[:directed] = !!@directed  unless r_params.has_key?(:directed)
      r_params[:loops] = false           unless r_params.has_key?(:loops)
      r_params[:matrix_type] = 'edgelist'
      c = R.array.conversion
      R.array.conversion = RSRuby::NO_CONVERSION
      nc = R.network.conversion
      R.network.conversion = RSRuby::NO_CONVERSION
      el = flat_edgelist
#      n = R.network(to_r_matrix, :loops => true, :directed => !!@directed)
      n = R.network(R.array(el, [el.length/2, 2]), r_params)
      R.add_vertices(n, @nodes.size-R.network_size(n)) # correct Nr of nodes.
      R.network_vertex_names__(n, @nodes.collect { |k| nodelabel(k) })

      # We believe in all nodes being the same class:
      if (node = @nodes.first).respond_to?(:network_attributes)
        node.network_attributes.each do |name, method|
          R.set_vertex_attribute(n, name, @nodes.collect { |k| k.send(method)})
        end
      end
      if block_given?
        R.set_vertex_attribute(n, "attr", @nodes.collect { |k| yield(k) })
      end

      R.set_edge_attribute(n, "weight", @links.collect { |k,l| l.weight })

      R.network.conversion = nc
      R.array.conversion = c
      n
    end

    # computes the betweenness for all nodes using R/sna.
    #
    # _params_:: 
    #     Hash with all named parameters for R::betweenness.
    #     Try e.g.:
    #       g.betweenness(:rescale => true) 
    #     or
    #       g.betweenness(:cmode => 'undirected', :rescale => true)
    #     By default <i>:gmode</i> and <i>:cmode</i> are set automatically.
    def betweenness(params={})
      params[:gmode] ||= (@directed ? 'digraph' : 'graph')
      params[:cmode] ||= (@directed ? 'directed' : 'undirected')
      b = R.betweenness(to_r_matrix, params) 
      h = Hash.new
      @nodes.each_with_index { |n,i| h[n] = b[i] }
      h
    end
    
    # :call-seq:
    # pp_betweenness(:sortby => 0, :up => false, ...)
    # pp_betweenness(:sortby => 0, :up => false, ...) { |n| ... }
    #
    # Pretty print the betweenness of all nodes (using R/sna).
    #
    # _params_ is a Hash of named parameters:
    # <i>:up</i>, <i>:sortby</i> and the block (if given) are passed to 
    # #pp_key_value (see there), all other params are passed to 
    # #betweenness (see there).
    def pp_betweenness(params={}, &block)
      sortby = params.delete(:sortby) || 0
      up = params.delete(:up)
      puts "%-30s: %20s" % ["Node","betweenness"]
      pp_key_value(betweenness(params), sortby, up, &block)
    end

    # computes the closeness for all nodes using R/sna.
    #
    # _params_:: 
    #     Hash with all named parameters for R::closeness.
    #     Try e.g.:
    #       g.closeness(:rescale => true) 
    #     or
    #       g.closeness(:cmode => 'undirected', :rescale => true)
    #     By default <i>:gmode</i> and <i>:cmode</i> are set automatically.
    def closeness(params={})
      params[:gmode] ||= (@directed ? 'digraph' : 'graph')
      params[:cmode] ||= (@directed ? 'directed' : 'undirected') 
      b = R.closeness(to_r_matrix, params)
      h = Hash.new
      @nodes.each_with_index { |n,i| h[n] = b[i] }
      h
    end

    # :call-seq:
    # pp_closeness(:sortby => 0, :up => false, ...)
    # pp_closeness(:sortby => 0, :up => false, ...) { |n| ... }
    #
    # Pretty print the closeness of all nodes (using R).
    #
    # _params_ is a Hash of named parameters:
    # <i>:up</i>, <i>:sortby</i> and the block (if given) are passed to 
    # #pp_key_value (see there), all other params are passed to 
    # #closeness (see there).
    def pp_closeness(params={}, &block)
      sortby = params.delete(:sortby) || 0
      up = params.delete(:up)
      puts "%-30s: %20s" % ["Node","closeness"]
      pp_key_value(closeness(params), sortby, up, &block)
    end

    # computes the stress centrality scores for all nodes using R/sna.
    #
    # _params_:: 
    #     Hash with all named parameters for R::stresscent.
    #     Try e.g.:
    #       g.stresscent(:rescale => true) 
    #     or
    #       g.stresscent(:cmode => 'undirected', :rescale => true)
    #     By default <i>:gmode</i> and <i>:cmode</i> are set automatically.
    def stresscent(params={})
      params[:gmode] ||= (@directed ? 'digraph' : 'graph') 
      params[:cmode] ||= (@directed ? 'directed' : 'undirected')
      b = R.stresscent(to_r_matrix, params)
      h = Hash.new
      @nodes.each_with_index { |n,i| h[n] = b[i] }
      h
    end

    # :call-seq:
    # pp_stresscent(:sortby => 0, :up => false, ...)
    # pp_stresscent(:sortby => 0, :up => false, ...) { |n| ... }
    #
    # Pretty print the stress centrality scores of all nodes (using R/sna).
    #
    # _params_ is a Hash of named parameters:
    # <i>:up</i>, <i>:sortby</i> and the block (if given) are passed to 
    # #pp_key_value (see there), all other params are passed to 
    # #stresscent (see there).
    def pp_stresscent(params={}, &block)
      sortby = params.delete(:sortby) || 0
      up = params.delete(:up)
      puts "%-30s: %20s" % ["Node","stresscent"]
      pp_key_value(stresscent(params), sortby, up, &block)
    end



    # computes the prestige for all nodes using R/sna.
    #
    # _params_:: 
    #     Hash with all named parameters for R::prestige.
    #     Try e.g.:
    #       g.prestige(:rescale => true) 
    #     or
    #       g.prestige(:cmode => 'indegree', :rescale => true)
    #     By default <i>:gmode</i> is set automatically.
    def prestige(params={})
      params[:gmode] ||= (@directed ? 'digraph' : 'graph') 
      b = R.prestige(to_r_matrix, params)
      h = Hash.new
      @nodes.each_with_index { |n,i| h[n] = b[i] }
      h
    end

    # :call-seq:
    # pp_prestige(:sortby => 0, :up => false, ...)
    # pp_prestige(:sortby => 0, :up => false, ...) { |n| ... }
    #
    # Pretty print the prestige of all nodes (using R/sna).
    #
    # _params_ is a Hash of named parameters:
    # <i>:up</i>, <i>:sortby</i> and the block (if given) are passed to 
    # #pp_key_value (see there), all other params are passed to 
    # #prestige (see there).
    def pp_prestige(params={}, &block)
      sortby = params.delete(:sortby) || 0
      up = params.delete(:up)
      puts "%-30s: %20s" % ["Node","prestige"]
      pp_key_value(prestige(params), sortby, up, '%20i', &block)
    end


    # Use R to plot the DotGraph.
    #
    # If the param :filename is set to a filename with extension
    # .ps, .pdf, .png we plot to this file in the corresponding mode.
    #
    # The parameters are passed to the R network command.
    #
    # If any parameter points to a proc object (lambda expression), the
    # block is called with the network object as first and the R instance as
    # second parameter and is replaced by the result of this call. 
    #
    # Finally all params are passed to the R plot command.
    #
    # If a block is given, it is passed to #to_r_network.
    #
    # So try e.g.
    #
    #   g.r_plot(:label => lambda { |nw,r| r.get_vertex_attribute(nw, "attr")},
    #            :displaylabels => TRUE) { |u| u.name }
    #
    # If the plot device is not a file it stays open and its device
    # number _i_ is returned. It can be savely closed using 
    # r_plot_close(_i_) or DotGraph::r_plot_close(_i_).
    def r_plot(r_params={}, &r_attr)
      in_file = true
      fn = r_params[:filename] || ''
      case File.extname(fn)
      when '.ps'  : R.postscript(fn)
      when '.pdf' : R.pdf(fn)
      when '.png' : R.png(fn)
      else in_file = false;  R.eval_R('dev.new()')
      end

      nw = to_r_network(r_params.reject { |key, val| val.kind_of?(Proc) }, 
                        &r_attr)

      r_params.each_pair do |key, val|
        r_params[key] = val.call(nw, R)  if val.kind_of?(Proc)
      end

      R.plot_network(nw, r_params)
      
      if in_file
        R.eval_R('dev.off()') 
      else
        R.eval_R('dev.cur()').values.first # get the number of the device
      end
    end

    # Close the R plot device number _i_.
    def r_plot_close(i)
      R.dev_off(i)
    end

    # Close the R plot device number _i_.
    def DotGraph.r_plot_close(i)
      R.dev_off(i)
    end

    # Save R network representation of the graph to file _filename_.
    # All params are passed to <i>to_r_network</i>.
    #
    # The R name of the saved object is set to _nw_.
    def to_r_network_file(filename, r_params={})
      R.assign('nw', to_r_network(r_params))
      R.eval_R("save(nw, file='#{filename}')")
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
    def pp_key_value(h, sortby=0, up=true, fmt='%20.10f', &block)
      sortby = SORTBY[sortby] if sortby.kind_of?(Symbol)
      lproc = block || @lproc
      puts '='*60
      d = h.collect { |n,v|
        [lproc.call(n), v]
      }
      d = if up
            d.sort { |a,b| sorter_nan(a[sortby],b[sortby]) }
          else
            d.sort { |b,a| sorter_nan(a[sortby],b[sortby]) }
          end
      d.each { |a| puts("%-30s: #{fmt}" % a) }
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
