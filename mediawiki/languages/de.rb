#!/usr/bin/ruby -w
# :title: Mediawiki - Ruby Lib german Language File
#
# To add additional languages simply drop a corresponding ruby mapping file
# into mediawiki/languages/. 
#
# For a language with identifier _de_ create the file 
# <tt>mediawiki/languages/de.rb</tt>. Set <tt>Mediawiki::NS_Mappings['de']</tt>
# to a Hash with the identifier Strings in language _de_ as keys and
# the corresponding namespace numbers as values (use the constants defined in
# <tt>mediawiki.rb</tt> for this). Add the key <i>:project_talk</i> with a 
# template String representing talk/discussion pages in this language as 
# value (a % sign in the String will get replaced by the wiki name, so
# use e.g.: <tt>:project_talk => '%_Diskussion'</tt> for german (you _could_
# additionally specify <tt>:project</tt> the same way, but here the default
# should be suficcient for nearly any language so don't bother).
#
# You can get the needed mappings by looking into your mediawiki installation
# (e.g. <tt>/var/lib/mediawiki1.7/languages/LanguageDe.php</tt>, for newer 
# versions things may be different and the mapping is found in the file 
# <tt>MessagesDe.php</tt>), or online at 
# http://svn.wikimedia.org/doc/group__Language.html.
#
# Search for <tt>$wgNamespaceNames</tt> or <tt>$namespaceNames</tt>. 
# Use the mappings found there to create the Hash by switching keys and 
# values. <tt>NS_PROJECT</tt> and <tt>NS_PROJECT_TALK</tt> normaly point to 
# some variables, in the german file for mediawiki 1.7 this is
#   NS_PROJECT          => $wgMetaNamespace,
#   NS_PROJECT_TALK     => $wgMetaNamespace . '_Diskussion',
# in mediawiki 1.12 it is
#   NS_PROJECT_TALK     => '$1_Diskussion',
# You can savely ignore the <tt>NS_PROJECT</tt> line and from the 
# <tt>NS_PROJECT_TALK</tt> line create a template where 
# <tt>$wgMetaNamespace</tt> is represented by %, so we get for german:
#   :project_talk => '%_Diskussion'
# 
# Please send me new language files as patch if done!

module Mediawiki
  NS_Mappings['de'] = {
    :project_talk => '%_Diskussion',  # % will get replaced by wiki name.
    'Media' => NS_MEDIA,
    'Spezial' => NS_SPECIAL,
    'Diskussion' => NS_TALK,
    'Benutzer' => NS_USER,
    'Benutzer_Diskussion' => NS_USER_TALK,
    'Bild' => NS_IMAGE,
    'Bild_Diskussion' => NS_IMAGE_TALK,
    'MediaWiki' => NS_MEDIAWIKI,
    'MediaWiki_Diskussion' => NS_MEDIAWIKI_TALK,
    'Vorlage' => NS_TEMPLATE,
    'Vorlage_Diskussion' => NS_TEMPLATE_TALK,
    'Hilfe' => NS_HELP,
    'Hilfe_Diskussion' => NS_HELP_TALK,
    'Kategorie' => NS_CATEGORY,
    'Kategorie_Diskussion' => NS_CATEGORY_TALK 
  }
end
