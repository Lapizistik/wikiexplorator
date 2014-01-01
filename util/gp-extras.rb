#!/usr/bin/ruby -w

require 'util/enumstat'
require 'util/ngnuplot'
require 'set'

class Gnuplot
  # :call-seq:
  # plot_lorenz(a1, ..., an, :title => 'Lorenz Curve', :xlabel => 'items', :ylabel => 'cumulative values', ...) { |gp| ... }
  # plot_lorenz(a1, ..., an, :title => 'Lorenz Curve', :xlabel => 'items', :ylabel => 'cumulative values', ...)
  # plot_lorenz(a)
  #
  # Creates a Gnuplot object containing the Lorenz curve of any number of 
  # Enumerables and plots it (unless <tt>:plot</tt>=>false). Returns the
  # generated Gnuplot object.
  #
  # <i>a1</i> to <i>an</i> is any number of enumerables, followed by any 
  # number of named parameters including:
  # <tt>:title => "Lorenz Curve"</tt>:: the plot title
  # <tt>:xlabel => "items"</tt>:: x axis label
  # <tt>:ylabel => "cumulative values"</tt>:: y axis label
  # <tt>:key => "top left box"</tt>:: 
  #   key position. You can use <tt>:key => "off"</tt> for no key.
  # <tt>:titles => "G_i=%.2f"</tt>:: 
  #   an Array of title Strings for the given plots. The String is used
  #   as a format specification and applied to the Gini coefficient of the
  #   curve.
  # <tt>:equality => "equality"</tt>::
  #   description for the equality diagonal. If nil or false, the diagonal
  #   is not plotted.
  # <tt>:pareto => "80:20 (pareto)"</tt>:: 
  #   description for the 80:20 pareto point. If nil or false, it is not 
  #   plotted.
  # <tt>:plot => true</tt>::
  #   Boolean indicating whether the generated Gnuplot object should be
  #   plotted. Use this if you want to add further settings or other plots
  #   to the Gnuplot object returned.
  #   
  # If a block is given it is yielded with the Gnuplot object as parameter
  # before the plot command. This allows "last minute" customization.
  #
  # All parameters are forwarded to Gnuplot#plot.
  def Gnuplot.plot_lorenz(*args)
    params = {
      :title => "Lorenz Curve",
      :xlabel => "items",
      :ylabel => "cumulative values",
      :size => :square,
      :key => "top left box",
      :equality => "equality",
      :pareto => "80:20 (pareto)",
      :plot => true
    }

    if (ps=args.last).kind_of?(Hash)
      params.merge!(ps)
      args.pop
    end

    titles = (params[:titles] ||
              (1..args.length).collect { |i| "G_#{i} = %.2f" })
    Gnuplot.new do |gp|
      gp.add([[0,0],[1,1]], :with => "lines linetype 1", 
             :title => params[:equality]) if params[:equality]
      gp.add([[0.8,0.2]], :with => "points pointtype 1", 
             :title => params[:pareto]) if params[:pareto]
      args.each_with_index do |a,i|
        al=a.stat_lorenz
        gp.add(al,
               :with => "lines", 
               :title => (titles[i] % al.stat_gini))
      end
      gp.set('key', params[:key])
      gp.set('title', params[:title], true)
      gp.set('xlabel', params[:xlabel], true)
      gp.set('xtics', '0.1')
      gp.set('ylabel', params[:ylabel], true)
      gp.set('ytics', '0.1')
      gp.set('xrange','[0:1]')
      gp.set('yrange','[0:1]')
      gp.set('size', 'ratio -1')
      yield(gp) if block_given?
      gp.plot(params) if params[:plot]
    end
  end
  
  # :call-seq:
  # plot_lorenz_3d(ars, :title => 'Lorenz Curves', ...) { |gp| ... }
  # plot_lorenz_3d(ars, :title => 'Lorenz Curves', ...)
  # plot_lorenz_3d(ars)
  #
  # Creates a Gnuplot object containing a 3d representation of successing 
  # Lorenz curves and plots it (unless <tt>:plot</tt>=>false). Returns the
  # generated Gnuplot object.
  # 
  # _ars_ is an array of the form 
  # [[<i>x1</i>, [<i>z1</i>, <i>z2</i>, <i>z3</i>]], 
  # [<i>x2</i>, [<i>z4</i>, <i>z5</i>, <i>z6</i>, <i>z7</i>]], ...].
  # The lorenz curve of arrays [<i>z1</i>, <i>z2</i>, <i>z3</i>], ... 
  # is computed and displayed side by side, where <i>xi</i> gives the 
  # x-values.
  #
  # The following named parameters are recogniced:
  # <tt>:title => "Lorenz Curves"</tt>:: the plot title
  # <tt>:xlabel => "curves"</tt>:: x axis label
  # <tt>:ylabel => "curve"</tt>:: y axis label
  # <tt>:zlabel => "cumulative values"</tt>:: z axis label
  # <tt>:key => "off"</tt>:: key position. <tt>"off"</tt> means no key
  # <tt>:pm3d => true</tt>:: 
  #   whether to use <tt>pm3d</tt> coloring. The following parameters
  #   (besides <tt>:plot</tt>) are only used if this is true.
  # <tt>:interpolate => '1,1'</tt>:: number of interpolation steps
  # <tt>:palette => 'grey'</tt>:: palette to be used
  # <tt>:maxcolors => false</tt>:: 
  #   if a number is given it denotes the number of different colors to use 
  # <tt>:gamma => 2</tt>:: gamma correction
  # <tt>:plot => true</tt>:: 
  #   Boolean indicating whether the generated Gnuplot object should be
  #   plotted. Use this if you want to add further settings or other plots
  #   to the Gnuplot object returned.
  #
  # This allows e.g. to show changes in time.
  # Example:
  #   tr = wiki.timeraster(:step => :week, :zero => :week)
  #   r1 = tr.collect { |t| wiki.filter.endtime=t
  #     [t,wiki.users.collect {|u| u.revisions.length}.reject {|rr| rr==0 }] }
  #   Gnuplot.plot_lorenz_3d(r1, :view => 'map')
  #
  # If a block is given it is yielded with the Gnuplot object as parameter
  # before the plot command. This allows "last minute" customization.
  #
  # All parameters are forwarded to Gnuplot#plot.
  def Gnuplot.plot_lorenz_3d(ars, ps={})
    params = {
      :title => "Lorenz Curves",
      :xlabel => "curves",
      :ylabel => "curve",
      :zlabel => "cumulative values",
      :key => "off",
      :pm3d => true,
      :interpolate => '1,1',
      :palette => 'grey',
      :maxcolors => false,
      :gamma => 2,
      :plot => true
    }

    params.merge!(ps)

    
    xx = Set.new

    ars = ars.collect do |t,a|
      puts t.inspect, a.inspect
      a = a.stat_lorenz
      a.each { |x,y| xx << x }
      [t, a]
    end

    xx = xx.sort

    data = []
    ars.each do |t, a|
      Gnuplot.interpolate(a,xx).each do |y, z|
        data << [t,y,z]
      end
      data << []
    end

#    return data

    Gnuplot.new do |gp|
      if view = params[:view]
        gp.set('view', view)
      end
      gp.set('title', params[:title], true)
      gp.set('key', params[:key])
      gp.set('xlabel', params[:xlabel], true)
      gp.set('ylabel', params[:ylabel], true)
      gp.set('zlabel', params[:zlabel], true)
      gp.set('ytics', '0.1')
      gp.set('yrange','[0:1]')
      gp.set('zrange','[0:1]')
#      gp.set('size', 'ratio -1')
      if params[:pm3d]
        gp.set('pm3d')
        gp.set('pm3d',"interpolate #{params[:interpolate]} flush center ftriangles")
        maxcolors = params[:maxcolors]
        gp.set('palette', "maxcolors #{maxcolors}") if maxcolors
        gp.set('palette',params[:palette])
        gamma = params[:gamma]
        if gamma<0
          gp.set('palette','negative')
          gamma = -gamma
        end
        gp.set('palette', "gamma #{gamma}")
        gp.set('style', 'data pm3d')
      end
      if view = params[:view]
        gp.set('view', view)
      end

      gp.add(data,params)
      yield(gp) if block_given?
      gp.splot(params) if params[:plot]
    end

  end

  private
  # the following will only work for the specially prepaired arrays 
  # from plot_lorenz_3d (they are guaranteed to contain [0,0] and [1,1])
  def Gnuplot.interpolate(a,xx)
    i=0
    aa = []
    a.each_cons(2) do |l,r|
      aa << l
      lx, ly = *l
      rx, ry = *r
      i+=1 while (x=xx[i]) && (x <= lx)  # search for first coord within slice.
      while (x=xx[i]) && (x < rx)
        aa << [x, (x-lx)*(ry-ly)/(rx-lx)+ly]
        i+=1
      end
    end
    aa << a.last
  end
end

module Enumerable
  
  # Plot the Lorenz curve of the Enumerable. See Gnuplot.plot_lorenz
  # for details.
  def gp_plot_lorenz(params={})
    Gnuplot.plot_lorenz(self, params)
  end
end
