#! /usr/bin/ruby -w

include Math

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

def logn(x,n)
  log(x)/log(n)
end

def measures(aaa)
  fkts = [:sum, :maxx, :logg, :log1, 
          :exp_log, :log_exp, :square_roots, :root_squares]
  s = '#resp on pages  ||  '
  s << fkts.collect { |f| Enumerable.d[f] }.join('  ||  ')
  s << "\n"
  s << '='*155
  s << "\n"

  aaa.each { |aa|
    s << aa.collect { |a| "%2i" % a }.join(', ')
    s << '  ||  '
    s << fkts.collect { |f| Enumerable.f[f] % aa.send(f) }.join('  ||  ')
    s << "\n"
  }
  s
end

module Enumerable
  @@desc = {}
  @@format = {}
  def Enumerable.d
    @@desc
  end
  def Enumerable.f
    @@format
  end


  ## Funktionen:

  d[:sum] = 'sum'
  f[:sum] = '%3i'
  def sum
    inject(0) { |s,i| s+i }
  end

  d[:maxx] = 'max'
  f[:maxx] = '%3i'
  def maxx
    [max, 'max']
  end

  d[:logg] = ' log(i) '
  f[:logg] = '%8.3f'
  def logg
    inject(0) { |s,i| s+log(i+0.000000000000000001) }
  end

  d[:log1] = 'log(i+1)'
  f[:log1] = '%8.3f'
  def log1
    inject(0) { |s,i| s+log(i+1) }
  end

# Verwirrend:
#  def explog
#    [log(inject(0) { |s,i| s+exp(i) }), 'log(exp(i))']
#  end
#
#  def logexp
#    [log(inject(0) { |s,i| s+exp(i+1) }), 'log(exp(i+1))']
#  end


  d[:exp_log] = 'e^(sum(log(i+1))-1)'
  f[:exp_log] = '%14.3f     '
  def exp_log
    exp(inject(0) { |s,i| s+log(i+1) }-1)
  end

  d[:log_exp] = 'log(sum(e^(i-1))+1)'
  f[:log_exp] = '%14.3f     '
  def log_exp
    log(inject(0) { |s,i| s+exp(i-1) }+1)
  end

  d[:geom] = 'sqrt(prod(i+1)-1)'
  f[:geom] = '%14.3f   '
  def geom
    (inject(1) { |s,i| s*(i+1) }-1)**0.5
  end

  d[:square_roots] = "(sum(i^0.5))^2"
  f[:square_roots] = "%10.3f    "
  def square_roots
    sr(0.5)
  end

  d[:root_squares] = "(sum(i^2))^0.5"
  f[:root_squares] = "%10.3f    "
  def root_squares
    sr(2)
  end

  def sr(k=2)
    kk = 1.0/k
    (inject(0) { |s,i| s+i**k })**kk
  end
end


puts measures(A)
