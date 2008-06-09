#!/usr/bin/ruby -w
# :title: Mediawiki - Ruby Lib
# = Mediawiki Library
#
# This library allows for directly reading a mediawiki database 
# and presenting it as a set of ruby objects.
#
# It provides functions to query the database with statistical measures
# and to visualize it as dot-file.
#
# == ToDo
# 
# === Deleted pages
#
# The archive table is currently not parsed, i.e. deleted pages are
# not handled correctly by the time-based filters and are not counted
# for computing user collaboration.
#
# To fix this Page and Revision objects must get a deletion time
# instance variable. The deleted pages and revisions must be
# reconstructed from the archive table, deletion time from the logging table 
# (between lot of other stuff).
#
# This implies to modify the timespan filters (we may need a second timespan) 
# to get what we want (I may not want deleted pages to show up if timespan
# is set to whole range).
#
# Additionally the dangling links may point to deleted pages and have to
# be handled specially.

require 'mediawiki/core'
require 'mediawiki/db'
require 'mediawiki/functions'
require 'mediawiki/graph'

module Mediawiki
  Version = 0.5
end

