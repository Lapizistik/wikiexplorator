#!/usr/bin/ruby -w
# :title: Dot Graph Cube
# =  Dot Graph Cube Library Extension

require 'util/dotgraph'
require 'util/jbridge/visualizer'

class DotGraph
  def to_cube(timeraster, params={})
    directed = params[:directed] || @directed
    nn = @nodes.collect { |n| nid(n) }

    t0 = timeraster.first
    timeraster = timeraster[1..-1] # all but first

    sort_times unless params[:times_sorted]

    cube = Visualizer::Cube.new(nn, nn, timeraster.collect { |t| t.to_s })

ddd=0

    @nodes.each_with_index do |x, ix|
      @nodes.each_with_index do |y, iy|
        if !directed && (src.object_id > dest.object_id)
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
            c
          end
#warn zz.inspect          
          cube[ix][iy] = zz
        end
      end
    end
#warn "ddd = #{ddd}, @links.length = #{@links.length}"
    cube
  end
end
