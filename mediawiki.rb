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
#
# === Other DB-Engines
#
# Currently only the Mysql DB engine is fully supported. For others some
# (minor) changes are necessary, as e.g. for PostgreSQL some tables have
# different names.
#
# === Additional Languages
#
# Currently only english and german Mediawiki installations are supported,
# i.e. internal links to pages of other namespaces than Main will not
# be associated correctly in other languages.
#
# To add other languages simply create a language file in 
# <tt>mediawiki/languages/</tt>. See <tt>mediawiki/languages/de.rb</tt>
# for description/example.
#
# Please send me new language files as patch if done!

require 'util/trytoreq'
require 'mediawiki/core'
require 'mediawiki/functions'
require 'mediawiki/graph'
require 'mediawiki/report'

module Mediawiki
  Version = 0.8
  
  NS_MEDIA = -2
  NS_SPECIAL = -1
  NS_MAIN = 0
  NS_TALK = 1
  NS_USER = 2
  NS_USER_TALK = 3
  NS_PROJECT = 4
  NS_PROJECT_TALK = 5
  NS_IMAGE = 6
  NS_IMAGE_TALK = 7
  NS_MEDIAWIKI = 8
  NS_MEDIAWIKI_TALK = 9
  NS_TEMPLATE = 10
  NS_TEMPLATE_TALK = 11
  NS_HELP = 12
  NS_HELP_TALK = 13
  NS_CATEGORY = 14
  NS_CATEGORY_TALK = 15

  # The default mappings from identifiers to numeric namespaces.
  # 
  # <i>:project</i> and <i>:project_talk</i> are pointers to templates.
  # The <tt>%</tt> will get replaced by the wiki name. 
  NS_Default_Mapping = {
    :project => '%',
    :project_talk => '%_talk',
    'Media' => NS_MEDIA,
    'Special' => NS_SPECIAL,
    '' => NS_MAIN,
    'Talk' => NS_TALK,
    'User' => NS_USER,
    'User_talk' => NS_USER_TALK,
    'Project' => NS_PROJECT,
    'Project_talk' => NS_PROJECT_TALK,
    'Image' => NS_IMAGE,
    'Image_talk' => NS_IMAGE_TALK,
    'MediaWiki' => NS_MEDIAWIKI,
    'MediaWiki_talk' => NS_MEDIAWIKI_TALK,
    'Template' => NS_TEMPLATE,
    'Template_talk' => NS_TEMPLATE_TALK,
    'Help' => NS_HELP,
    'Help_talk' => NS_HELP_TALK,
    'Category' => NS_CATEGORY,
    'Category_talk' => NS_CATEGORY_TALK
  }

  # The language specific mappings from identifiers to numeric namespaces.
  # Will be filled by the definitions in mediawiki/languages/, autoloaded
  # in Mediawiki::Wiki.new
  NS_Mappings = {}
end

