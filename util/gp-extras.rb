#!/usr/bin/ruby -w

require 'util/enumstat'

class Gnuplot
  # :call-seq:
  # Gnuplot.plot_lorenz(a1, ..., an, :title => 'Lorenz Curve', :xlabel => 'items', :ylabel => 'cumulative values', ...)
  # Gnuplot.plot_lorenz(a)
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
  # All parameters are forwarded to Gnuplot#plot.
  def Gnuplot.plot_lorenz(*args)
    params = {
      :title => "Lorenz Curve",
      :xlabel => "items",
      :ylabel => "cumulative values",
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
      gp.add([[0,0],[1,1]], :with => "lines 1", 
             :title => params[:equality]) if params[:equality]
      gp.add([[0.8,0.2]], :with => "points 1", 
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
      gp.plot(params) if params[:plot]
    end
  end
end

module Enumerable
  
  # Plot the Lorenz curve of the Enumerable. See Gnuplot.plot_lorenz
  # for details.
  def gp_plot_lorenz(params={})
    Gnuplot.plot_lorenz(self, params)
  end
end
