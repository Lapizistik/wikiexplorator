#!/usr/bin/ruby -w

require 'mysql'

class Mysql
  class Result
    
    def typed_each
      convert = fetch_fields.collect do |f| 
        if !f.is_num?
          :to_s
        elsif f.decimals==0
          :to_i
        else
          :to_f
        end
      end
      each do |a|
        convert.each_index do |i| 
          a[i] = a[i].send(convert[i]) if a[i]
        end
        yield(a)
      end
    end
  end
end

