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
    return [[0,0],[1,1]] if empty? # or should we throw an error?
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
  #    [1,3,2,4].stat_gini == [[0, 0], [0.25, 0.1], [0.5, 0.3], [0.75, 0.6], [1.0, 1.0]].stat_gini == 0.25
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
        0
      end
    else
      stat_lorenz.stat_gini
    end
  end

  # computes the Hoover index (aka Robin Hood index) of the Enumerable using 
  # its normalized distribution (see stat_normalized_distribution).
  def stat_hoover_index
    0.5*stat_normalized_distribution.inject(0) { |s,ae| s+(ae[1]-ae[0]).abs }
  end

  # computes the symmetric Theil index of the Enumerable using its normalized
  # distribution (see stat_normalized_distribution).
  def stat_theil_s_index
    0.5*stat_normalized_distribution.inject(0) { |s,ae| 
      e=ae[1]; a=ae[0].to_f; s + Math.log(e/a)*(e-a) }
  end

  # computes the Theil T index of the Enumerable using its normalized
  # distribution (see stat_normalized_distribution).
  def stat_theil_t_index
    stat_normalized_distribution.inject(0) { |s,ae| 
      e=ae[1].to_f; a=ae[0].to_f; s + e*Math.log(e/a) }
  end

  # computes the Theil L index of the Enumerable using its normalized
  # distribution (see stat_normalized_distribution).
  def stat_theil_l_index
    stat_normalized_distribution.inject(0) { |s,ae| 
      e=ae[1].to_f; a=ae[0].to_f; s + a*Math.log(a/e) }
  end

  # computes the Theil L index of the Enumerable using its normalized
  # distribution (see stat_normalized_distribution).
  def stat_atkinson_index
    1 - Math.exp(-stat_theil_t_index)
  end

  # computes a normalized distribution of the values of the Enumerable.
  # If the enumerable contains Arrays their first and second entry are assumed
  # to be the width and height [Ai, Ei] of the distribution entries.
  # Otherwise the contents are assumed to be numbers representing the Ei
  # With all Ai being of same width.
  #
  # It returns an Array of Arrays of the form 
  # <tt>[[A1, E1], [A2, E2], ..., [An, En]]</tt> with <tt>\sum Ai = 1</tt> and
  # <tt>\sum Ei = 1</tt>.
  def stat_normalized_distribution
    if first.kind_of?(Array)
      asum = esum = 0.0
      each { |a,e| asum += a; esum +=e }
      collect { |a,e| [a/asum, e/esum] }
    else
      a = 1.0/length
      esum = stat_sum.to_f
      collect { |e| [a,e/esum] }
    end
  end

  # computes the concentration coefficient:
  #   n / (n-1) * Gini
  def stat_concentration
    l = length.to_f
    stat_gini*l/(l-1)
  end

  # computes the entropy:
  #   H = p_i \ln(p_i)      with p_i = x_i/X;  X = \sum x_i
  def stat_entropy
    sum = stat_sum.to_f
    -inject(0) { |s,p| p=p/sum; s + p*Math.log(p) }
  end

  # computes the normalized entropy:
  #   H_0 = H/\ln(n)
  # see stat_entropy.
  def stat_entropy_normalized
    if (l=length.to_f) > 0
      stat_entropy/Math.log(l)
    else
      0.0
    end
  end

  # computes the exponential index:
  #   e^{-H} = \prod p_i^{p_i}    with p_i = x_i/X;  X = \sum x_i
  def stat_exponential_index
    sum = stat_sum.to_f
    inject(1) { |s,p| p=p/sum; s*(p**p) }
  end

  # computes the Herfindahl measure:
  #   H_e = \sum p_i^2     with p_i = x_i/X;  X = \sum x_i
  def stat_herfindahl
    sum = stat_sum.to_f
    inject(0) { |s,p| p=p/sum; s + p*p }    
  end

  # computes the normalized Herfindahl measure:
  #   H_e^* = (H_e - 1/n) / (1 - 1/n)
  # (see stat_herfindahl).
  def stat_herfindahl_normalized
    k = 1.0/length
    (stat_herfindahl-k)/(1-k)
  end

  # computes the sum of all values.
  def stat_sum
    inject(0) { |a,b| a+b }
  end

  # computes the average of all values.
  def stat_avg
    stat_sum.to_f/length
  end

  # computes the variance of the values. Please note that this uses
  # 1/n and not 1/(n-1) 
  def stat_variance
    (inject(0.0) { |s,x| s+x*x })/length - stat_avg**2
  end

  # computes the standard deviation of the values. Please note that this uses
  # 1/n and not 1/(n-1) 
  def stat_standard_deviation
    stat_variance**0.5
  end

  # computes a histogram of the values in this collection
  def stat_histogram
    h = Hash.new(0)
    each { |v| h[v] +=1 }
    h
  end
end

