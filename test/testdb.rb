#!/usr/bin/ruby -w

class TestDB
  attr_reader :usertable, :ugtable, :texttable, :pagetable
  attr_reader :revtable, :genretable
  
  def initialize(name,
                 usertable, ugtable, texttable, pagetable, 
                 revtable, genretable, roletable)
    (@name, @usertable, @ugtable, @texttable, @pagetable, 
     @revtable, @genretable, @roletable) = 
      [name, usertable, ugtable, texttable, pagetable, 
       revtable, genretable, roletable]
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
  def roles(&block)
    @roletable.each(&block)
  end

  class << self # class methods

    # The default database for testing
    def default
      ta = Time.gm(2000)
      tz = Time.now
      # id, name, real_name, email, options, touched, email_authenticated, 
      # email_token_expires, registration, newpass_time, editcount
      usertable = (1..30).collect { |uid|
        [uid, "login#{uid}", "name#{uid}","#{uid}@mail", '', tz, nil,
         nil, ta+uid*40000, nil, nil]
      }

      # user, group
      ugtable = []
      
      # id, text, flags
      texttable = (10000..10100).collect { |tid|
        [tid, "Text Nr #{tid}.",'']
      }
      # [10000, 'Text mit internem Link auf [[Dangling]], [[Working]]', '']

      ############################################################
      ## TODO: Adding Text links, pagetable, revtable ############
      ############################################################

      
      # id, namespace, title, restrictions, counter, 
      # is_redirect, is_new, random, touched, latest, len
      pagetable = 
        [[1, 0, 'Main Page', nil, 0, 0, 1, 4711, '20060101000001', 507,
          texttable[0][1].length],
         [2, 0, 'Working', nil, 0, 0, 0, 23, '20060202000002', 502,
          texttable[2][1].length],
         [3, 6, 'Testing', nil, 0, 0, 0, 23, '20060909000009', 508,
          texttable[2][1].length]]
      
      # id, page, text_id, comment, user, user_text, timestamp, 
      # minor_edit, deleted, len, parent_id
      revtable =
        [[500, 1, 10000, '', 0, 'system', '20060101000001',
          0, nil, nil, nil],
         [501, 2, 10001, '', 1, usertable.assoc(1)[2], '20060202000001',
          0, nil, nil, nil],
         [502, 2, 10002, '', 1, usertable.assoc(1)[2], '20060202000002',
          0, nil, nil, nil],
         [503, 3, 10003, '', 1, usertable.assoc(1)[2], '20060909000009',
          0, nil, nil, nil],
         [504, 1, 10004, 'ip', 0, '127.0.0.1', '20060909000010',
          0, nil, nil, nil],
         [505, 1, 10005, 'ip', 0, '::2', '20060909000011',
          0, nil, nil, nil],
         [506, 1, 10006, 'ip', 0, '127.0.0.1', '20060909000012',
          0, nil, nil, nil],
         [507, 1, 10007, 'ip', 0, '168.0.0.1', '20060909000022',
          0, nil, nil, nil],
         [508, 3, 10003, '', 99, 'bla', '20060909000009',
          0, nil, nil, nil],
        ]
      
      # pid, genres
      genretable = [[1, 'portal, xxx']]
      
      roletable = [[1,'MasterOfTheUniverse, DAU']]
      
      new("TestDB::default", usertable, ugtable, texttable, pagetable, 
          revtable, genretable, roletable)
    end
  end
end

