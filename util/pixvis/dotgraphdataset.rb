#!/usr/bin/ruby -w
# :title: Dot Graph Cube
# =  Dot Graph Cube Library Extension

require 'util/dotgraph'
require 'util/pixvis/visualizer'

class DotGraph
  # returns a Visualizer::Cube representation of this DotGraph rastered
  # by _timeraster_. Only useful if the links are time-annotated.
  #
  # Try e.g.
  #   dotgraph.to_cube(raster).visualize
  #
  # ToDo: document options
  def to_cube(timeraster, params={})
    directed = params[:directed] || @directed
    maxvalue = params[:maxvalue] || 1.0/0
    
    names = params[:namefkt] || lambda { |n| nid(n) }

    nodes = @nodes

    case filterby = params[:filterby]
    when Proc
      nodes = nodes.select(&filterby)
    when Integer
      nodes = nodes.select { |n| n_degree(n, true) >= filterby }
    end

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

  # returns a Visualizer::Table representation of the node linking
  # activity of this DotGraph rastered
  # by _timeraster_. Only useful if the links are time-annotated.
  #
  # Try e.g.
  #   dotgraph.to_cube(raster).visualize
  #
  # ToDo: document options
  def to_nodelinktable(timeraster, params={})
    directed = params[:directed] || @directed
    maxvalue = params[:maxvalue] || 1.0/0

    if directed 
      which = params[:which] || :source
    else
      which = :both
    end
    
    names = params[:namefkt] || lambda { |n| nid(n) }

    nodes = @nodes

    case filterby = params[:filterby]
    when Proc
      nodes = nodes.select(&filterby)
    when Integer
      nodes = nodes.select { |n| n_degree(n, true) >= filterby }
    end

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

    data = nodes.collect do |n|
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
      zz.collect { |c| c > maxvalue ? maxvalue : c }
    end
    Visualizer::Table.new(nn, timeraster.collect { |t| t.to_s }, data)
  end
end
