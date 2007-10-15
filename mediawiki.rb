#!/usr/bin/ruby -w
# :title: Mediawiki - Ruby Lib
# = Mediawiki Library
#
# This library allows for directly reading a mediawiki database 
# and presenting it as a set of ruby objects.
#
# It provides functions to query the database with statistical measures
# and to visualize it as dot-file.


require 'mediawiki/core'
require 'mediawiki/db'
require 'mediawiki/functions'
require 'mediawiki/graph'

module Mediawiki
  Version = 0.5
end

