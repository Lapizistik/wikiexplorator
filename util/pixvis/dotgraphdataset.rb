#!/usr/bin/ruby -w
# :title: Dot Graph Cube
# =  Dot Graph Cube Library Extension

require 'util/dotgraph'
require 'util/pixvis/visualizer'

RSRuby.instance.library('WGCNA')

class DotGraph
  # returns a Visualizer::Cube representation of this DotGraph rastered
  # by _timeraster_. Only useful if the links are time-annotated.
  #
  # Try e.g.
  #   dotgraph.to_cube(raster).visualize
  #
  # ToDo: document options
  def to_cube_old(timeraster, params={})
    directed = params[:directed] || @directed
    maxvalue = params[:maxvalue] || 1.0/0
    
    names = params[:namefkt] || lambda { |n| nid(n) }

    nodes = params[:nodes] ||
      case filterby = params[:filterby]
      when Proc
        @nodes.select(&filterby)
      when Integer
        @nodes.select { |n| n_degree(n, true) >= filterby }
      else
        @nodes
      end

    # to be redone!
    case sortby = params[:sortby]
    when Proc
      nodes = nodes.sort_by(&sortby)
    when :degree
      nodes = nodes.sort_by { |n| -n_degree(n, true) }
    end

    nn = nodes.collect(&names)

    t0 = timeraster.first
    timeraster = timeraster[1..-1] || [] # all but first

    sort_times unless params[:times_sorted]

    cube = Visualizer::Cube.new(nn, nn, timeraster.collect { |t| t.to_s })

    nodes.each_with_index do |x, ix|
      nodes.each_with_index do |y, iy|
        if !directed && (x.object_id > y.object_id)
          key = [y,x]
        else
          key = [x,y]
        end
        if (link = @links[key]) && (linktimes = link.timeline)
#warn "loop #{ddd+=1}"
          il = 0   # pointer for the link array
          # throw away what is before starttime:
          il += 1   while (lt = linktimes[il]) && (lt < t0)
#warn "il=#{il}, linktimes.length=#{linktimes.length}, timeraster.length=#{timeraster.length}"
          zz = timeraster.collect do |t|
            c = 0
            while (lt = linktimes[il]) && (lt < t)
#warn "inner loop: c=#{c}, il=#{il}"
              c += 1
              il += 1
            end
            c > maxvalue ? maxvalue : c
          end
#warn zz.inspect          
          cube[ix][iy] = zz
        end
      end
    end
#warn "ddd = #{ddd}, @links.length = #{@links.length}"
    cube
  end

  # new method :-) will replace the old one!
  def to_cube(timeraster, params={})
    
    names = params[:namefkt] || lambda { |n| nid(n) }

    nodes = params[:nodes] ||
      case filterby = params[:filterby]
      when Proc
        @nodes.select(&filterby)
      when Integer
        @nodes.select { |n| n_degree(n, true) >= filterby }
      else
        @nodes
      end

    sort_times unless params[:times_sorted]

    nparams = params.merge(:nodes => nodes)

    data = params[:rasterdata] || rasteredcube(timeraster, nparams)

    nodes, data = sort_nodes_data_by_rastered_cube(data, nparams)

    return nodes if params[:nodesonly] # subject to change, dirty hack!
    # but we have to addapt our pixvis script also!

    nn = nodes.collect(&names)

    Visualizer::Cube.new(nn, nn, 
                         (timeraster[1..-1] || []).collect { |t| t.to_s },
                         data)
  end

  # returns a Visualizer::Table representation of the node linking
  # activity of this DotGraph rastered
  # by _timeraster_. Only useful if the links are time-annotated.
  #
  # Try e.g.
  #   dotgraph.to_nodelinktable(raster).visualize
  #
  # ToDo: document options
  def to_nodelinktable(timeraster, params={})
    
    names = params[:namefkt] || lambda { |n| nid(n) }

    nodes = params[:nodes] || 
      case filterby = params[:filterby]
      when Proc
        @nodes.select(&filterby)
      when Integer
        @nodes.select { |n| n_degree(n, true) >= filterby }
      else
        @nodes
      end

    sort_times unless params[:times_sorted]

    nparams = params.merge(:nodes => nodes)

    data = params[:rasterdata] || rasteredlinks(timeraster, nparams)
    
    nodes = sort_nodes_by_rastered_links(data, nparams)

    return nodes if params[:nodesonly] # subject to change, dirty hack!
    # but we have to addapt our pixvis script also!

    nn = nodes.collect(&names)
    dd = nodes.collect { |n| data[n] }

    Visualizer::Table.new(nn, 
                          (timeraster[1..-1] || []).collect { |t| t.to_s }, dd)


  end


  # TODO: refactoring, reorganization.
  # helper for to_cube
  def rasteredcube(timeraster, params={})
    directed = params[:directed] || @directed
    maxvalue = params[:maxvalue] || 1.0/0
    nodes = params[:nodes] || @nodes


    t0 = timeraster.first
    timeraster = timeraster[1..-1] || [] # all but first

    l = nodes.length
    data = Array.new(l) { Array.new(l) { Array.new(timeraster.length,0)}}

    nodes.each_with_index do |x, ix|
      nodes.each_with_index do |y, iy|
        if !directed && (x.object_id > y.object_id)
          key = [y,x]
        else
          key = [x,y]
        end
        if (link = @links[key]) && (linktimes = link.timeline)
#warn "loop #{ddd+=1}"
          il = 0   # pointer for the link array
          # throw away what is before starttime:
          il += 1   while (lt = linktimes[il]) && (lt < t0)
#warn "il=#{il}, linktimes.length=#{linktimes.length}, timeraster.length=#{timeraster.length}"
          zz = timeraster.collect do |t|
            c = 0
            while (lt = linktimes[il]) && (lt < t)
#warn "inner loop: c=#{c}, il=#{il}"
              c += 1
              il += 1
            end
            c > maxvalue ? maxvalue : c
          end
#warn zz.inspect          
          data[ix][iy] = zz
        end
      end
    end
#warn "ddd = #{ddd}, @links.length = #{@links.length}"
    data
  end

  # TODO: refactoring, reorganization.
  # helper for to_cube
  def sort_nodes_data_by_rastered_cube(data, params={})
warn 'sorting...'
warn 'Params: ' + params.inspect
    nodes = params[:nodes] || @nodes
    
    positions = {}
    nodes.each_with_index do |n,i|
      positions[n] = i
    end 

    p = params[:p] || 0.5 # minkowski p.
    d0 = params[:d0] || 2 # empty data line distance for cosine measure

    ln = nodes.length

    case sortby = params[:sortby]
    when Proc
      nodes = nodes.sort_by(&sortby)
    when :degree
      direction = params[:direction] || -1
      nodes = nodes.sort_by { |n| direction * n_degree(n, true) }
    when :glyphsim
warn 'sorting glyphsim'
      blur = params[:blurdist] || 2
      flatten = params[:flatten]
      direction = params[:direction] || -1
      method = params[:distmethod] || 'euclid'
      
      l = data.first.first.length

      sdata = data.collect { |col| # is it col or row?
        col.collect { |d|
          # lets blur:
          if blur > 0
            sd = Array.new(l,0)
            d.each_with_index do |v,i|
              lb = i - blur
              lb = 0       if lb < 0
              rb = i + blur
              rb = l - 1   if rb >= l
              lb.upto(rb) do |k|
                sd[k] += v
              end
            end
          else
            sd = d.dup
          end
          # lets flatten:
          case flatten
          when :full
            sd.collect! { |v| v==0 ? 0 : 1 }
          when Numeric # use small values!
            sd.collect! { |v| v**flatten }
          end
          sd
        }
      }
      # now sdata holds the blurred and flattened data matrix
      cd = R.dist.conversion
      ca = R.array.conversion
      R.dist.conversion = RSRuby::NO_CONVERSION
      R.array.conversion = RSRuby::NO_CONVERSION
      
      distsum = Array.new(ln*ln, 0)

      sdata.each do |a|

        ra = if method == 'cosine'
               # the following line is a hack to deal with empty data lines
               # in a special way. It ensures that two empty data lines get
               # similarity 1 (== distance 0) and that comparing a not empty 
               # with an empty data line gives negative similarity 
               # (this is used later to handle this case specially).
               a = a.collect { |aa| aa.uniq == [0] ? [-1]*aa.length : aa }
               R.array(a.flatten,[l,a.length])
             else
               R.array(a.transpose.flatten,[a.length,l])
             end

        dd = case method
             when 'cosine'
               R.cor(ra, 'cosine' => true)
             else
               R.dist(ra,method, :p => p)
             end

        mm = R.as_matrix(dd).flatten

        if method == 'cosine' # similarity to distance conversion
          # this also handles compairson with empty timelines
          mm.collect! { |v| v<0 ? d0 : 1-v }
        end

        mm.each_with_index do |v,i|
          distsum[i] += v
        end
      end

      ma = R.array(distsum,[ln,ln])
      order = R.cmdscale(ma, 1)

      nvals = Hash.new
      
      nodes.each_with_index do |n,i|
        nvals[n] = direction * order[i].first
      end
      nodes = nodes.sort_by { |n| nvals[n] }
      
    end


    # sorting data
    data = nodes.collect { |n| 
      bb = data[positions[n]]
      nodes.collect { |m| 
        bb[positions[m]]
      }
    }

    return nodes, data
  end

  # TODO: refactoring, reorganization.
  # helper for to_nodelinktable
  def rasteredlinks(timeraster, params={})
    directed = params[:directed] || @directed
    maxvalue = params[:maxvalue] || 1.0/0
    nodes = params[:nodes] || @nodes

    if directed 
      which = params[:which] || :source
    else
      which = :both
    end

    t0 = timeraster.first
    timeraster = timeraster[1..-1] || [] # all but first


    nh = Hash.new { |h,k| h[k] = [] }

    @links.each do |(s,d),l|
      case which
      when :source
        nh[s] << l
      when :dest
        nh[d] << l
      when :both
        nh[s] << l
        nh[d] << l unless s == d
      end
    end

    tlength = timeraster.length

    data = Hash.new

    nodes.each do |n|
      zz = Array.new(tlength,0) # collect numbers...
      nh[n].each do |l|
        if (linktimes = l.timeline)
          il = 0   # pointer for the link array
          # throw away what is before starttime:
          il += 1   while (lt = linktimes[il]) && (lt < t0)
#warn "il=#{il}, linktimes.length=#{linktimes.length}, timeraster.length=#{timeraster.length}"

          timeraster.each_with_index do |t,ti|
            c = 0
            while (lt = linktimes[il]) && (lt < t)
#warn "inner loop: c=#{c}, il=#{il}"
              c += 1
              il += 1
            end
            zz[ti] += c
          end
        end
      end
      data[n] = zz.collect { |c| c > maxvalue ? maxvalue : c }
    end
    data
  end

  # TODO: refactoring, reorganization.
  # helper for to_nodelinktable
  def sort_nodes_by_rastered_links(data, params={})
    nodes = params[:nodes] || @nodes
    
    case sortby = params[:sortby]
    when Proc
      nodes = nodes.sort_by(&sortby)
    when :degree
      direction = params[:direction] || -1
      nodes = nodes.sort_by { |n| direction * n_degree(n, true) }
    when :similarity
      # we have to rewrite this part, put it in an own file etc (r usage)
      # there will be some refractoring necessary
      blur = params[:blurdist] || 2
      flatten = params[:flatten]
      direction = params[:direction] || -1
      method = params[:distmethod] || 'euclidean'
      
      l = data[nodes.first].length
      
      sdata = []

      nodes.each do |n|
        d = data[n]
        # lets blur:
        if blur > 0
          sd = Array.new(l, 0)
          d.each_with_index do |v,i|
            lb = i - blur
            lb = 0       if lb < 0
            rb = i + blur
            rb = l - 1   if rb >= l
            lb.upto(rb) do |k|
              sd[k] += v
            end
          end
        else
          sd = d.dup
        end
        # lets flatten:
        case flatten
        when :full
          sd.collect! { |v| v==0 ? 0 : 1 }
        when Numeric # use small values!
          sd.collect! { |v| v**flatten }
        end
        sdata << sd
      end
      # now sdata holds the sorting data in an array of arrays
      # in the same order as nodes
      
      # we now use R to compute the distances
      cd = R.dist.conversion
      ca = R.array.conversion
      R.dist.conversion = RSRuby::NO_CONVERSION
      R.array.conversion = RSRuby::NO_CONVERSION
      
      ra = R.array(sdata.transpose.flatten,[sdata.length,l])
      dists = R.dist(ra, method) #, 'manhattan')
      
      R.dist.conversion = cd
      R.array.conversion = ca
      order = R.cmdscale(dists, 1)
      
      nvals = Hash.new
      
      nodes.each_with_index do |n,i|
        nvals[n] = direction * order[i].first
      end
      nodes = nodes.sort_by { |n| nvals[n] }
      
      
    end
    
    nodes
  end
  
end
