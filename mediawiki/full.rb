require 'util/trytoreq'

try_to_require 'rubygems', :silent

require 'mediawiki'

try_to_require('util/r',
    'The methods based on R will not work.',
    'Make sure RSRuby and all R packages needed are installed properly')
require 'util/latex'
require 'util/ngnuplot'
require 'util/enumstat'
require 'util/gp-extras'
try_to_require('util/jbridge/dotgraphdataset',
               'The Java based Visualizer will not work.')
