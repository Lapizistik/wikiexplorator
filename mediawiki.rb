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

require 'util/trytoreq'
require 'mediawiki/core'
# require 'mediawiki/db'
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

