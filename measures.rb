#! /usr/bin/ruby -w

A = [
     [8,0,0,0],
     [7,1,0,0],
     [6,2,0,0],
     [6,1,1,0],
     [5,3,0,0],
     [5,2,1,0],
     [5,1,1,1],
     [4,4,0,0],
     [4,3,1,0],
     [4,2,2,0],
     [4,2,1,1],
     [3,3,2,0],
     [3,3,1,1],
     [3,2,2,1],
     [2,2,2,2],
     [0,0,0,0],
     [4,4,4,4],
     [4,2,2,2],
     [4,4,0,0],
     [4,0,0,0],
     [1,1,1,1]
    ]

def measures(aaa)
  s = ''
  aaa.each { |aa|
    s << aa.collect { |a| "%2i" % a }.join(', ')
    s << ' :: '
    s << "%7.3f (%s),  " % aa.sum
    s << "%7.3f (%s),  " % aa.maxx
    s << "%7.3f (%s),  " % aa.log1
    s << " [[ "
    s << "%7.3f (%s),  " % aa.log_exp
    s << "%7.3f (%s),  " % aa.exp_log
    s << " ]] [[ "
    s << "%7.3f (%s),  " % aa.square_root
    s << "%7.3f (%s),  " % aa.square_root(0.5)
    s << " ]] "
#    s << "%7.3f (%s),  " % aa.geom
    s << "\n"
  }
  s
end

module Enumerable
  def sum
    [inject(0) { |s,i| s+i }, 'sum']
  end

  def maxx
    [max, 'max']
  end

  def log
    [inject(0) { |s,i| s+Math.log(i+0.0000000001) }, 'log(i)']
  end

  def log1
    [inject(0) { |s,i| s+Math.log(i+1) }, 'log(i+1)']
  end

  def exp_log
    [Math.exp(inject(0) { |s,i| s+Math.log(i+1) }-1), 'e^(log(i+1)-1)']
  end

  def log_exp
    [Math.log(inject(0) { |s,i| s+Math.exp(i)-1 }+1), 'log(prod(e^i-1)+1)']
  end

  def geom
    [(inject(1) { |s,i| s*(i+1) }-1)**0.5, 'sqrt(prod(i+1)-1)']
  end

  def square_root(k=2)
    kk = 1.0/k
    [(inject(0) { |s,i| s+i**k })**kk, "(sum(i^(#{k})))^#{"%.2f" % kk}"]
  end
end


puts measures(A)
