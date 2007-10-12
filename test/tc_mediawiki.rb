#!/usr/bin/ruby -w

require 'test/unit'

require 'testdb'

$:.unshift File.join(File.dirname(__FILE__), '..') # here is the code

require 'mediawiki'

class TestMediawiki < Test::Unit::TestCase
  
  def setup
    @testdb = TestDB.default
    @wiki = Mediawiki::Wiki.new(@testdb)
  end

  def test_wiki_consistency
    # There is one user more in the wiki than in the user table as
    # the system user is not in the table:
    assert(@wiki.users.size == @testdb.usertable.size+1, 'Lost some user?')
    assert(@wiki.pages.size == @testdb.pagetable.size, 'Lost some page?')
    assert(@wiki.revisions.size == @testdb.revtable.size,'Lost some revision?')
    
    @testdb.usertable.each do |i, n,|
      assert(@wiki.user_by_id(i).name==n, "Wrong user by id #{i}: #{n}")
      assert(@wiki.user_by_name(n).uid==i, 'Wrong user by name #{n}: #{i}')
    end
  end
end
