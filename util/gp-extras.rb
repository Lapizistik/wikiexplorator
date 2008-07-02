#!/usr/bin/ruby -w

require 'util/enumstat'

class Gnuplot
  # plots the Lorenz curve of an Enumerable a.
  def Gnuplot.plot_lorenz(a, params={})
    params = {
      :title => "Cummulative Distribution Function",
      :xlabel => "items",
      :ylabel => "cumulative values"
    }.merge(params)
    
    data = a.stat_lorenz

    Gnuplot.new do |gp|
      gp.add(data, :with => "lines")
      gp.add([[0,0],[1,1]], :with => "lines")
      gp.set('nokey')
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
  
  # Plot the CDF (Lorenz curve) of the Enumerable. See Gnuplot.plot_cdf
  # for details.
  def gp_plot_lorenz(params={})
    Gnuplot.plot_lorenz(self, params)
  end
end
