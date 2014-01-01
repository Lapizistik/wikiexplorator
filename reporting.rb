#!/usr/bin/ruby -w

### Settings ###

### Include ###

# Cummulative Revisions per Month (CRPM)
crpm = wiki.timeraster(:zero => :month, :step => :month).collect { |t| wiki.filter.endtime = t; [ t.strftime('%m%y'), wiki.revisions.length ] }
Gnuplot.new do |gp|
	gp.add(crpm, :using => "1:2 with linespoints" )
	gp.set('nokey')
	gp.set('xdata time')
	gp.set('timefmt "%m%y"')
	gp.set('format x "%b %y"')
	gp.set('xtics rotate')
	gp.set('title "Cummulative Revisions per Month"')
	gp.set('xlabel "month" 0,-2')
	gp.set('ylabel "number of revisions"')
	gp.plot(:png => 'crpm.png')
end

# Revisions per Month (RPM)
require 'enumerator'
rpm = wiki.timeraster(:zero => :month, :step => :month).enum_cons(2).collect { |t| wiki.filter.starttime = t.first; wiki.filter.endtime = t.last; [ t.last.strftime('%m%y'), wiki.revisions.length ] }
Gnuplot.new do |gp|
	gp.add(rpm, :using => "1:2 with linespoints" )
	gp.set('nokey')
	gp.set('xdata time')
	gp.set('timefmt "%m%y"')
	gp.set('format x "%b %y"')
	gp.set('xtics rotate')
	gp.set('title "Revisions per Month"')
	gp.set('xlabel "month" 0,-2')
	gp.set('ylabel "number of revisions"')
	gp.plot(:png => 'rpm.png')
end

# Cummulative Distribution Function
require 'enumerator'
nr = wiki.revisions.length.to_f # number of revisions
nu = wiki.users.length.to_f # number of users
cdf = [] # array for cummulative distribution function
x = 1 # index variable for user (1..nu)
wiki.users.collect { |u| u.revisions.length/nr*100 }.sort { |i,j| i <=> j }.inject { |s,v| cdf << [x/nu*100,s]; x += 1; s += v }
cdf << [100,100]

Gnuplot.new do |gp|
	gp.add(cdf,:with => "lines")
	gp.set('nokey')
	gp.set('title "Cummulative Distribution Function"')
	gp.set('xlabel "percentage of users"')
	gp.set('xtics 0, 10, 100')
	gp.set('ylabel "percentage of revisions"')
	gp.set('ytics 0, 10, 100')
	gp.set('arrow from 0,0 to 100,100 nohead')
	gp.plot(:png => 'cdf.png')
end

### Exclude ###

# Cummulative Revisions per Week (CRPW)
crpw = wiki.timeraster(:zero => :week, :step => :week).collect { |t| wiki.filter.endtime = t; [ t, wiki.revisions.length ] }

# Revisions per Month (RPM)
# require 'enumerator'
rpw = wiki.timeraster(:zero => :week, :step => :week).enum_cons(2).collect { |a| wiki.filter.starttime = a.first; wiki.filter.endtime = a.last; [ a.last, wiki.revisions.length ] }
