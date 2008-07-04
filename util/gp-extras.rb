#!/usr/bin/ruby -w

require 'util/enumstat'

class Gnuplot
  # :call-seq:
  # Gnuplot.plot_lorenz(a1, ..., an, :title => 'Lorenz Curve', :xlabel => 'items', :ylabel => 'cumulative values', ...)
  # Gnuplot.plot_lorenz(a)
  #
  # plots the Lorenz curve of any number of Enumerables.
  #
  # <i>a1</i> to <i>an</i> is any number of enumerables, followed by any number of
  # named parameters including:
  # <tt>:title</tt>:: the plot title
  # <tt>:xlabel</tt>:: x axis label
  # <tt>:ylabel</tt>:: y axis label
  # <tt>:key</tt>:: 
  #   key position. You can use <tt>:key => 'off'</tt> for no key.
  # <tt>:titles</tt>:: 
  #   an Array of title Strings for the given plots. The String is used
  #   as a format specification and applied to the Gini coefficient of the
  #   curve. Defaults to <tt>'G_i=%.2f'</tt>
  #   
  # All parameters are forwarded to Gnuplot#plot.
  def Gnuplot.plot_lorenz(*args)
    params = {
      :title => "Lorenz Curve",
      :xlabel => "items",
      :ylabel => "cumulative values",
      :key => "top left box"
    }
    if ps=args.last.kind_of?(Hash)
      params.merge(ps)
      args.pop
    end
    
    titles = (params[:titles] ||
              (1..args.length).collect { |i| "G_#{i}=%.2f" })
    Gnuplot.new do |gp|
      gp.add([[0,0],[1,1]], :with => "lines", :title => "equality")
      args.each_with_index do |a,i|
        al=a.stat_lorenz
        gp.add(al,
               :with => "lines", 
               :title => (titles[i] % al.stat_gini))
      end
      gp.set('key', params[:key])
      gp.set("title \"#{params[:title]}\"")
      gp.set("xlabel \"#{params[:xlabel]}\"")
      gp.set('xtics 0.1')
      gp.set("ylabel \"#{params[:ylabel]}\"")
      gp.set('ytics 0.1')
      gp.set('arrow from 0.0,0.0 to 1.0,1.0 nohead')
      gp.plot(params)
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
