require 'util/trytoreq'

try_to_require 'rubygems', :silent

require 'mediawiki'

with_r = try_to_require('util/r',
    'The methods based on R will not work.',
    'Make sure RSRuby and all R packages needed are installed properly')
require 'util/latex'
require 'util/ngnuplot'
require 'util/enumstat'
require 'util/gp-extras'

# The following library is not included by default because of an 
# graphviz bug (reported to graphviz, fixed 2009-8-7). Feel free to include
# the library on your own if the bug does not hurt you.

# try_to_require('util/dotgraph-gv', 'gv library missing', 'Fallback to graphviz')

require 'util/pixvis/dotgraphdataset' if with_r

try_to_require('util/pixvis/jbridge/visualizer',
               'The Java based Visualizer will not work.')
try_to_require('util/pixvis/visualizer-svg',
               'The SVG based Visualizer will not work.')
