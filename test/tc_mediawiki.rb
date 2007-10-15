#!/usr/bin/ruby -w

require 'test/unit'

$:.unshift File.join(File.dirname(__FILE__), '..') # here is the code

require File.join(File.dirname(__FILE__), 'testdb') # and the test...
require 'mediawiki'

class TestMediawiki < Test::Unit::TestCase
  
  def setup
    @testdb = TestDB.default
    @wiki = Mediawiki::Wiki.new(@testdb)
  end

  def test_core

    # There is one user more in the wiki than in the user table as
    # the system user is not in the table:
    assert_equal(@wiki.users.size, @testdb.usertable.size+1)
    assert_equal(@wiki.pages.size, @testdb.pagetable.size)
    assert_equal(@wiki.revisions.size, @testdb.revtable.size)



    @testdb.usertable.each do |i, n,|
      assert_equal(@wiki.user_by_id(i).name, n)
      assert_equal(@wiki.user_by_name(n).uid, i)
    end

    ### Filter
    f1 = @wiki.filter
    # Defaults:
    assert_equal(f1.namespaces,[0].to_set)
    assert(f1.denied_users.empty?)
    assert_equal(f1.redirects, :keep)
    assert(f1.minor_edits)
    # testing deep cloning
    f2 = f1.clone
    f2.include_namespace(4)
    f2.deny_user(0)
    f2.redirects = :filter
    f2.minor_edits = false
    assert_not_same(f1.namespaces, f2.namespaces)
    assert(!f1.namespaces.include?(4))
    assert(f2.namespaces.include?(4))
    assert_not_same(f1.denied_users, f2.denied_users)
    assert_not_same(f1.denied_users, f2.denied_users)
    assert_not_same(f1.redirects, f2.redirects)
    assert_not_same(f1.minor_edits, f2.minor_edits)

    ### Genres
    assert(@wiki.page_by_id(1).has_genre?('portal'))
    assert(!@wiki.page_by_id(2).has_genre?('portal'))
    assert(@wiki.page_by_id(1).has_genre?(/^port/))
    f2.genregexp = /^port/
    assert(@wiki.pages(f2).include?(@wiki.page_by_id(1)))
    assert(!@wiki.pages(f2).include?(@wiki.page_by_id(2)))
    f2.genreinclude = false
    assert(!@wiki.pages(f2).include?(@wiki.page_by_id(1)))
    assert(@wiki.pages(f2).include?(@wiki.page_by_id(2)))

    ### Roles
    assert(@wiki.user_by_id(1).has_role?('DAU'))
    f2.roleregexp = /^Master/
    assert(@wiki.users(f2).include?(@wiki.user_by_id(1)))
    assert(!@wiki.users(f2).include?(@wiki.user_by_id(2)))
    f2.roleinclude = false
    assert(!@wiki.users(f2).include?(@wiki.user_by_id(1)))
    assert(@wiki.users(f2).include?(@wiki.user_by_id(2)))
  end

end
