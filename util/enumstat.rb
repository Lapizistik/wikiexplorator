#!/usr/bin/ruby -w
# :title: Enumerable statistic extensions
# = Enumerable Statistic Extensions
# This file extends the Enumerable module with a set of statistic methods.
# To keep the namespace-pollution small they all start with prefix 
# <tt>stat_</tt>.

require 'enumerator'

module Enumerable
  # computes the lorenz distribution of the Enumerable.
  # Returns an Array of Arrays representing x and y coordinates of the curve
  # scaled to [0,1][0,1]
  def stat_lorenz
    sum = 0
    a = sort.collect { |s| sum += s }
    xmax = a.length.to_f
    ymax = a.last.to_f
    data = [[0,0]]
    a.each_with_index { |c,i| data << [(i+1)/xmax, c/ymax] }
    data
  end

  # computes the gini-coefficient of the Enumerable.
  # If the Enumerable contains Arrays their first and second entry are
  # assumed to be the x and y coordinates of the lorenz curve.
  # Otherwise the contents are assumed to be numbers and #stat_lorenz
  # is used to compute the lorenz curve.
  #
  # So the following holds:
  #    [1,3,2,4].stat_gini == [[0, 0], [0.25, 0.1], [0.5, 0.3], [0.75, 0.6], [1.0, 1.0]].stat_gini ==0.25
  # If the coordinate form is used it is _not_ checked whether the coordinates
  # are in the right order, the user is responsible for providing a correct
  # lorenz curve (but it is ok, if the curve is not scaled, so
  #   [[0,0],[2,1],[4,4]].stat_gini == [[0,0],[0.5,0.25],[1,1]].stat_gini == [[0,0],[1,0.25],[2,1]].stat_gini == 0.25
  # holds). 
  def stat_gini
    if first.kind_of?(Array)
      if length>1
        al = 0
        each_cons(2) do |left,right|
          w = right[0] - left[0]
          h = (left[1] + right[1])/2.0
          al += w*h
        end
        at = (last[0]*last[1])/2.0
        1-al/at
      else
        nil
      end
    else
      stat_lorenz.stat_gini
    end
  end
end

