#!/usr/bin/ruby -w

class TestDB
  attr_reader :usertable, :ugtable, :texttable, :pagetable
  attr_reader :revtable, :genretable
  
  def initialize(name,
                 usertable, ugtable, texttable, pagetable, 
                 revtable, genretable)
    (@name, @usertable, @ugtable, @texttable, @pagetable, 
     @revtable, @genretable) = 
      [name, usertable, ugtable, texttable, pagetable, 
       revtable, genretable]
  end
  def connect
    yield(self)
  end
  def to_s
    @name
  end

  def users(&block)
    @usertable.each(&block)
  end
  def usergroups(&block)
    @ugtable.each(&block)
  end
  def texts(&block)
    @texttable.each(&block)
  end
  def pages(&block)
    @pagetable.each(&block)
  end
  def revisions(&block)
    @revtable.each(&block)
  end
  def genres(&block)
    @genretable.each(&block)
  end

  # The default database for testing
  def TestDB.default
    # id, name, real_name, email, options, touched, email_authenticated, 
    # email_token_expires, registration, newpass_time, editcount
    usertable = 
      [[1, 'dau', 'Dau User', 'dau@nil', '', '20070808080808', nil,
        nil, '20060808000000', nil, 5]]

    # user, group
    ugtable = []

    # id, text, flags
    texttable =
      [[10000, 'Text mit internem Link auf [[Dangling]], [[Working]]', ''],
       [10001, 'Zweiter Text mit Linkkk auf [[Bla|blubb]], [[Working]]', ''],
       [10002, 'Zweiter Text mit Link auf [[Bla|blubb]], [[Working]]', '']]

    # id, namespace, title, restrictions, counter, 
    # is_redirect, is_new, random, touched, latest, len
    pagetable = 
      [[0, 0, 'Main Page', nil, 0, 0, 1, 4711, '20060101000001', 500,
        texttable[0][1].length],
       [1, 0, 'Working', nil, 0, 0, 0, 23, '20060202000002', 502,
        texttable[2][1].length]]

    # id, page, text_id, comment, user, user_text, timestamp, 
    # minor_edit, deleted, len, parent_id
    revtable =
      [[500, 0, 10000, '', 0, 'system', '20060101000001',
        0, nil, nil, nil],
       [501, 1, 10001, '', 1, usertable.assoc(1)[2], '20060202000001',
        0, nil, nil, nil],
       [502, 1, 10002, '', 1, usertable.assoc(1)[2], '20060202000002',
        0, nil, nil, nil]]

    # pid, genres
    genretable = [[0, 'portal, xxx']]

    TestDB.new("TestDB::default", usertable, ugtable, texttable, pagetable, 
               revtable, genretable)
  end
end

